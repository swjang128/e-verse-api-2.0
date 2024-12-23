package atemos.everse.api.batch.tasklet;

import atemos.everse.api.domain.AlarmPriority;
import atemos.everse.api.domain.AlarmType;
import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.entity.Alarm;
import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Energy;
import atemos.everse.api.repository.*;
import atemos.everse.api.service.EnergyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * AlarmTasklet는 업체별 에너지 사용량을 모니터링하고,
 * 이상이 발생한 경우 알람을 생성하는 작업을 수행합니다.
 * 이 Tasklet은 Spring Batch에서 사용되어 주기적으로 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmTasklet implements Tasklet {
    private final EnergyRepository energyRepository;
    private final CompanyRepository companyRepository;
    private final AnomalyRepository anomalyRepository;
    private final AlarmRepository alarmRepository;
    private final IotRepository iotRepository;
    private final AIForecastEnergyRepository aiForecastEnergyRepository;
    private final EnergyRateRepository energyRateRepository;
    private final EnergyService energyService;

    /**
     * execute 메서드는 Tasklet이 실행될 때 호출되며,
     * 각 업체의 에너지 사용량을 확인하고 이상이 발생할 경우 알람을 생성합니다.
     *
     * @param contribution StepContribution 객체 (Batch 메타데이터 업데이트를 담당)
     * @param chunkContext ChunkContext 객체 (Chunk 관련 정보를 담고 있음)
     * @return RepeatStatus.FINISHED (작업 완료 상태 반환)
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // 이전 한 시간 동안의 에너지 사용량을 체크하기 위해 시간 범위를 설정 (UTC 기준)
        var startOfPreviousHour = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        var endOfPreviousHour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        // 모든 업체에 대해 에너지 사용량을 체크
        companyRepository.findAll().forEach(company -> {
            // 업체의 타임존을 가져와 각 업체별로 알람을 생성할 때 로컬 시간을 적용
            var companyZoneId = company.getCountry().getZoneId();
            var localStartOfPreviousHour = startOfPreviousHour.atZone(ZoneId.of("UTC")).withZoneSameInstant(companyZoneId).toLocalDateTime();
            var localEndOfPreviousHour = endOfPreviousHour.atZone(ZoneId.of("UTC")).withZoneSameInstant(companyZoneId).toLocalDateTime();
            // IoT 장비 목록을 가져와 사용량 계산
            var iotList = iotRepository.findByCompanyId(company.getId());
            // 지정된 시간 범위 내에서 업체의 총 에너지 사용량을 계산
            var totalUsage = energyRepository.findByIotInAndReferenceTimeBetween(iotList, localStartOfPreviousHour, localEndOfPreviousHour)
                    .stream()
                    .map(Energy::getFacilityUsage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // NORMAL 상태의 IoT 장비 개수를 기준으로 임계값 조정
            long normalIotCount = iotRepository.countByCompanyAndStatus(company, IotStatus.NORMAL);
            // 업체에 설정된 이상 탐지 임계값을 가져옴
            anomalyRepository.findByCompanyAndAvailable(company, true).ifPresent(anomaly -> {
                // NORMAL 상태의 IoT 장비 수를 곱하여 임계값을 조정
                var adjustedMinThreshold = anomaly.getLowestHourlyEnergyUsage().multiply(BigDecimal.valueOf(normalIotCount));
                var adjustedMaxThreshold = anomaly.getHighestHourlyEnergyUsage().multiply(BigDecimal.valueOf(normalIotCount));
                // 최소/최대 에너지 사용량 임계값을 초과하는지 확인하고 알람을 저장
                var anomalyMessageMinimumThreshold = "Energy consumption is below the minimum threshold.";
                var anomalyMessageMaximumThreshold = "Energy consumption exceeds the maximum threshold.";
                // 업체가 속한 국가가 한국일 때만 한국어 알람 메시지로 생성
                if ("ko-KR".equals(company.getCountry().getLanguageCode())) {
                    anomalyMessageMinimumThreshold = "에너지 사용량이 최소 임계값보다 적습니다.";
                    anomalyMessageMaximumThreshold = "에너지 사용량이 최대 임계값을 초과했습니다.";
                }
                checkAndSaveAlarm(company, totalUsage, adjustedMinThreshold, AlarmType.MINIMUM_ENERGY_USAGE, anomalyMessageMinimumThreshold);
                checkAndSaveAlarm(company, totalUsage, adjustedMaxThreshold, AlarmType.MAXIMUM_ENERGY_USAGE, anomalyMessageMaximumThreshold);
            });
            // AI 예측 에너지 사용량과 비교하여 알람 생성
            checkAndSaveAIPredictionAlarm(company, localStartOfPreviousHour, totalUsage);
        });
        return RepeatStatus.FINISHED;
    }

    /**
     * AI 예측 에너지 사용량과 실제 에너지 사용량을 비교하여
     * 예상 요금보다 높은 경우 알람을 생성합니다.
     *
     * @param company        알람을 생성할 업체
     * @param localDateTime  에너지 사용량을 체크할 시간대의 로컬 시작 시각 (업체의 타임존 기준)
     * @param totalUsage     실제 에너지 사용량
     */
    private void checkAndSaveAIPredictionAlarm(Company company, LocalDateTime localDateTime, BigDecimal totalUsage) {
        // 업체의 국가에 대한 에너지 요금 정보를 가져옴
        var energyRate = energyRateRepository.findByCountry(company.getCountry())
                .orElseThrow(() -> new EntityNotFoundException("No energy rate found for this country."));
        var hourOfDay = localDateTime.getHour();
        // 현재 시간대가 피크 또는 경피크 시간대인지 확인
        if (energyRate.getPeakHours().contains(hourOfDay) || energyRate.getMidPeakHours().contains(hourOfDay)) {
            // 예측 데이터가 존재하는 경우
            var forecastOpt = aiForecastEnergyRepository.findByCompanyAndForecastTime(company, localDateTime);
            if (forecastOpt.isPresent()) {
                var forecastUsage = forecastOpt.get().getForecastUsage();
                // 시간대별 요금을 getRate 메서드를 사용하여 계산
                var ratePerUnit = energyService.getRate(company, energyRate, hourOfDay).setScale(4, RoundingMode.HALF_UP);
                var actualCost = totalUsage.multiply(ratePerUnit).setScale(4, RoundingMode.HALF_UP);
                var forecastCost = forecastUsage.multiply(ratePerUnit).setScale(4, RoundingMode.HALF_UP);
                // 실제 요금이 예측 요금을 초과하는 경우 알람 생성(업체가 속한 국가가 한국일 때만 한국어 알람 메시지로 생성)
                if (actualCost.compareTo(forecastCost) > 0) {
                    var percentageDifference = actualCost.subtract(forecastCost).divide(forecastCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    var message = String.format("Energy bill is %s%% higher than the AI forecasted bill.", percentageDifference);
                    // 업체가 속한 국가가 한국일 때만 한국어 알람 메시지로 생성
                    if ("ko-KR".equals(company.getCountry().getLanguageCode())) {
                        message = String.format("AI 예측 요금보다 실제 요금이 %s%% 높습니다.", percentageDifference);
                    }
                    log.info("**** [ALARM GENERATED] Actual cost exceeded the forecasted amount during peak or mid-peak hours.");
                    var alarm = Alarm.builder()
                            .company(company)
                            .type(AlarmType.AI_PREDICTION_BILL_EXCEEDED)
                            .notify(true)
                            .isRead(false)
                            .priority(AlarmPriority.HIGH)
                            .message(message)
                            .expirationDate(Instant.now().plus(7, ChronoUnit.DAYS))
                            .build();
                    alarmRepository.save(alarm);
                }
            }
        }
    }

    /**
     * 에너지 사용량이 임계값을 초과하거나 미달할 경우 알람을 생성하여 DB에 저장합니다.
     *
     * @param company    알람을 생성할 업체
     * @param totalUsage 측정된 총 에너지 사용량
     * @param threshold  임계값 (최대 또는 최소 에너지 사용량)
     * @param type       알람의 유형 (최대 또는 최소 에너지 사용량)
     * @param message    알람 메시지
     */
    private void checkAndSaveAlarm(Company company, BigDecimal totalUsage, BigDecimal threshold, AlarmType type, String message) {
        if ((type == AlarmType.MINIMUM_ENERGY_USAGE && totalUsage.compareTo(threshold) < 0) ||
                (type == AlarmType.MAXIMUM_ENERGY_USAGE && totalUsage.compareTo(threshold) > 0)) {
            log.info("**** [ALARM GENERATED] Energy usage has exceeded or fallen below the configured threshold.");
            var alarm = Alarm.builder()
                    .company(company)
                    .type(type)
                    .notify(true)
                    .isRead(false)
                    .priority(AlarmPriority.HIGH)
                    .message(message)
                    .expirationDate(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();
            alarmRepository.save(alarm);
        }
    }
}