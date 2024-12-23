package atemos.everse.api.batch.tasklet;

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataRemoveTasklet은 오래된 데이터를 주기적으로 삭제하는 작업을 처리하는 Tasklet입니다.
 * - 이 Tasklet은 Energy, Alarm, IotStatusHistory, BlacklistedToken, AIForecastEnergy, TwoFactorAuth 테이블에서 오래된 데이터를 삭제합니다.
 * - 기본적으로 1년 이상 지난 데이터를 삭제하고, BlacklistedToken의 경우 1개월 이상, TwoFactorAuth의 경우 1일 이상 지난 데이터를 삭제합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataRemoveTasklet implements Tasklet {
    private final CompanyRepository companyRepository;
    private final EnergyRepository energyRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final AIForecastEnergyRepository aiForecastEnergyRepository;
    private final AlarmRepository alarmRepository;
    private final IotStatusHistoryRepository iotStatusHistoryRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;

    /**
     * 오래된 데이터를 삭제하는 메서드입니다.
     * 이 메서드는 Spring Batch의 Tasklet에서 호출되어 배치 작업을 처리합니다.
     * - Energy, Alarm, IotStatusHistory, BlacklistedToken, AIForecastEnergy, TwoFactorAuth 테이블에서 오래된 데이터를 삭제합니다.
     * - 각 업체의 타임존을 기준으로 삭제 작업을 수행합니다.
     *
     * @param contribution Step의 기여도 정보
     * @param chunkContext Chunk 관련 컨텍스트 정보
     * @return 작업 상태를 나타내는 RepeatStatus (FINISHED 반환 시 작업 완료)
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // 모든 업체와 해당 업체의 타임존을 가져와서 타임존 별로 그룹화합니다.
        Map<ZoneId, List<Company>> companiesByZone = companyRepository.findAll().stream()
                .collect(Collectors.groupingBy(company -> company.getCountry().getZoneId()));
        // 각 타임존별로 삭제 작업을 수행합니다.
        companiesByZone.forEach((zoneId, companiesInZone) -> {
            // 삭제 기준 시간을 해당 타임존에 맞춰 계산합니다.
            var oneYearAgo = ZonedDateTime.now(zoneId).minusYears(1).toInstant();
            var oneMonthAgo = ZonedDateTime.now(zoneId).minusMonths(1).toInstant();
            var oneDayAgo = ZonedDateTime.now(zoneId).minusDays(1).toInstant();
            // LocalDateTime으로 변환 (삭제 시점 기준)
            var oneYearAgoLocal = ZonedDateTime.now(zoneId).minusYears(1).toLocalDateTime();
            // 업체 ID 목록을 추출합니다.
            var companyIds = companiesInZone.stream()
                    .map(Company::getId)
                    .collect(Collectors.toList());
            // 오래된 데이터 삭제 작업을 수행하고 몇 건이 삭제되었는지 알림
            logDeletion(energyRepository.deleteByReferenceTimeBeforeAndIot_Company_IdIn(oneYearAgoLocal, companyIds), "Energy", oneYearAgo);
            logDeletion(aiForecastEnergyRepository.deleteByForecastTimeBeforeAndCompany_IdIn(oneYearAgoLocal, companyIds), "AI Forecast Energy", oneYearAgo);
            logDeletion(iotStatusHistoryRepository.deleteByCreatedDateBeforeAndIot_Company_IdIn(oneYearAgo, companyIds), "IoT Status History", oneYearAgo);
            logDeletion(alarmRepository.deleteByCreatedDateBeforeAndCompany_IdIn(oneYearAgo, companyIds), "Alarm", oneYearAgo);
            logDeletion(blacklistedTokenRepository.deleteByCreatedDateBeforeOrCreatedDateIsNull(oneMonthAgo), "Blacklisted Tokens", oneMonthAgo);
            logDeletion(twoFactorAuthRepository.deleteByCreatedDateBefore(oneDayAgo), "Two-Factor Authentication", oneDayAgo);
        });
        return RepeatStatus.FINISHED;
    }

    /**
     * 삭제된 레코드 수가 0보다 큰 경우 로그를 출력하는 헬퍼 메서드입니다.
     *
     * @param count        삭제된 레코드 수
     * @param recordType   삭제된 레코드의 유형 (예: "energy", "AI forecast energy")
     * @param timeFrame    삭제 기준 기간
     */
    private void logDeletion(long count, String recordType, Instant timeFrame) {
        if (count > 0) {
            log.info("Deleted {} {} records older than {}.", count, recordType, timeFrame);
        }
    }
}