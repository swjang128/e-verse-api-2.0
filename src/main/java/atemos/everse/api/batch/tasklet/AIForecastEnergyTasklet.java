package atemos.everse.api.batch.tasklet;

import atemos.everse.api.entity.AIForecastEnergy;
import atemos.everse.api.entity.Company;
import atemos.everse.api.repository.*;
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
import java.util.HashMap;
import java.util.Map;

/**
 * AIForecastEnergyTasklet은 각 업체의 에너지 사용량을 예측하여 AIForecastEnergy 테이블에 저장하는 역할을 합니다.
 * 각 업체별로 타임존을 고려하여 예측 작업을 수행하고, 3개월간의 과거 데이터를 바탕으로 예측을 생성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIForecastEnergyTasklet implements Tasklet {
    private final AIForecastEnergyRepository aiForecastEnergyRepository;
    private final CompanyRepository companyRepository;
    private final IotRepository iotRepository;
    private final EnergyRepository energyRepository;
    private final EnergyUsageForecastModelRepository energyUsageForecastModelRepository;

    /**
     * 각 업체의 타임존에 맞춰 시간 범위를 계산하고, 예측 작업을 병렬로 처리합니다.
     * TimeZoneConverter를 사용하여 UTC에서 각 회사의 타임존으로 시간 변환을 수행합니다.
     *
     * @param contribution 배치 작업의 기여도 정보
     * @param chunkContext 배치 작업의 컨텍스트 정보
     * @return 작업 완료 상태
     * @throws Exception 예외 발생 시 처리
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        // 업체별 시간 범위 계산
        Map<Company, LocalDateTime[]> companyDateRanges = new HashMap<>();
        companyRepository.findAll().forEach(company -> {
            ZoneId companyZoneId = company.getCountry().getZoneId();
            var now = LocalDateTime.ofInstant(Instant.now(), companyZoneId);
            var startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            var endOfLastMonth = startOfLastMonth.plusMonths(1).minusMinutes(1);
            var startOfThisMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            var endOfThisMonth = startOfThisMonth.plusMonths(1).minusMinutes(1);
            var startOfNextMonth = startOfThisMonth.plusMonths(1);
            var endOfNextMonth = startOfNextMonth.plusMonths(1).minusMinutes(1);
            companyDateRanges.put(company, new LocalDateTime[]{startOfLastMonth, endOfLastMonth, startOfThisMonth, endOfThisMonth, startOfNextMonth, endOfNextMonth});
        });
        // 스레드를 사용한 병렬 처리
        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            companyDateRanges.forEach((company, dateRanges) -> executor.submit(() -> {
                processForecast(company, dateRanges[0], dateRanges[1]); // 저번 달 예측
                processForecast(company, dateRanges[2], dateRanges[3]); // 이번 달 예측
                processForecast(company, dateRanges[4], dateRanges[5]); // 다음 달 예측
            }));
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * 주어진 월의 예측 데이터를 처리하는 메서드입니다.
     *
     * @param company 업체 정보
     * @param startOfMonth 해당 월의 시작 시간
     * @param endOfMonth 해당 월의 종료 시간
     */
    private void processForecast(Company company, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        var isForecastExists = aiForecastEnergyRepository.existsByCompanyIdAndForecastTimeBetween(company.getId(), startOfMonth, endOfMonth);
        if (!isForecastExists) {
            generateForecastForMonth(company, startOfMonth, endOfMonth);
        }
    }

    /**
     * 주어진 월의 예측 데이터를 생성하는 메서드입니다.
     * HeatWave 데이터나 과거 데이터를 바탕으로 예측을 수행합니다.
     *
     * @param company 업체 정보
     * @param startOfMonth 해당 월의 시작 시간
     * @param endOfMonth 해당 월의 종료 시간
     */
    @Transactional
    private void generateForecastForMonth(Company company, LocalDateTime startOfMonth, LocalDateTime endOfMonth) {
        var totalForecast = BigDecimal.ZERO;
        while (!startOfMonth.isAfter(endOfMonth)) {
            var hourlyForecast = getHeatWaveOrPastUsage(company, startOfMonth);
            saveForecast(company, hourlyForecast, startOfMonth);
            totalForecast = totalForecast.add(hourlyForecast);
            startOfMonth = startOfMonth.plusHours(1); // 1시간씩 증가
        }
    }

    /**
     * HeatWave 데이터를 사용하거나 과거 데이터를 바탕으로 에너지 사용량을 예측하는 메서드입니다.
     *
     * @param company 업체 정보
     * @param forecastTime 예측할 시간
     * @return 예측된 에너지 사용량
     */
    private BigDecimal getHeatWaveOrPastUsage(Company company, LocalDateTime forecastTime) {
        return energyUsageForecastModelRepository.findByCompanyIdAndForecastTime(company.getId(), forecastTime)
                .map(heatWaveData -> {
                    log.info("Generated forecast for company {} at {} using HeatWave data.", company.getId(), forecastTime);
                    return heatWaveData.getForecastUsage();
                })
                .orElseGet(() -> predictHourlyEnergyUsageFromPastThreeMonths(company, forecastTime));
    }

    /**
     * 과거 3개월 데이터를 바탕으로 에너지 사용량을 예측하는 메서드입니다.
     *
     * @param company 업체 정보
     * @param targetDateTime 예측할 시간
     * @return 예측된 에너지 사용량
     */
    private BigDecimal predictHourlyEnergyUsageFromPastThreeMonths(Company company, LocalDateTime targetDateTime) {
        var hourlyUsage = BigDecimal.ZERO;
        var iotList = iotRepository.findByCompanyId(company.getId());
        for (var iot : iotList) {
            for (var monthOffset = 1; monthOffset <= 3; monthOffset++) {
                var pastDateTime = targetDateTime.minusMonths(monthOffset);
                var pastUsage = energyRepository.findHourlyUsageByIotAndTime(iot.getId(), pastDateTime);
                if (pastUsage != null) {
                    hourlyUsage = hourlyUsage.add(pastUsage);
                }
            }
        }
        return hourlyUsage.divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
    }

    /**
     * 예측된 데이터를 AIForecastEnergy 테이블에 저장하는 메서드입니다.
     *
     * @param company 업체 정보
     * @param totalPredictedUsage 예측된 총 에너지 사용량
     * @param forecastTime 예측할 시간
     */
    private void saveForecast(Company company, BigDecimal totalPredictedUsage, LocalDateTime forecastTime) {
        if (!aiForecastEnergyRepository.existsByCompanyIdAndForecastTime(company.getId(), forecastTime)) {
            var forecastEnergy = AIForecastEnergy.builder().company(company).forecastUsage(totalPredictedUsage).forecastTime(forecastTime).build();
            aiForecastEnergyRepository.save(forecastEnergy);
            log.info("No HeatWave data available, generated forecast using past data for {}. Forecast for company {} saved: {}", forecastTime, company.getId(), totalPredictedUsage);
        }
    }
}