package atemos.everse.api.batch.tasklet;

import atemos.everse.api.dto.ApiCallLogDto;
import atemos.everse.api.entity.MeteredUsage;
import atemos.everse.api.repository.ApiCallLogRepository;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.IotStatusHistoryRepository;
import atemos.everse.api.repository.MeteredUsageRepository;
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
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 각 업체의 등록일부터 현재까지의 서비스 사용량(MeteredUsage)을 생성 또는 수정하는 Spring Batch Tasklet 클래스입니다.
 * 각 업체별로 API 호출 로그와 IoT 설치 데이터를 수집하여, MeteredUsage 엔티티를 생성하거나 수정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaveMeteredUsageTasklet implements Tasklet {
    private final ApiCallLogRepository apiCallLogRepository;
    private final CompanyRepository companyRepository;
    private final IotStatusHistoryRepository iotStatusHistoryRepository;
    private final MeteredUsageRepository meteredUsageRepository;

    /**
     * 각 업체의 등록일부터 현재까지의 MeteredUsage 데이터를 생성하거나 수정합니다.
     *
     * @param contribution 현재 스텝의 기여도 정보를 담고 있는 객체입니다.
     * @param chunkContext 청크 처리 시의 컨텍스트 정보를 담고 있는 객체입니다.
     * @return 작업 완료 상태를 반환합니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        companyRepository.findAll().forEach(company -> {
            var zoneId = company.getCountry().getZoneId();
            // 각 업체의 현재 날짜
            var companyLocalDate = Instant.now().atZone(zoneId).toLocalDate();
            // 각 업체의 서비스 시작일
            var companyStartDate = company.getCreatedDate().atZone(zoneId).toLocalDate();
            // 회사 시작일부터 현재까지의 날짜들을 스트림으로 생성
            Stream.iterate(companyStartDate, date -> !date.isAfter(companyLocalDate), date -> date.plusDays(1))
                    .forEach(date -> {
                        // 로그 데이터 및 IoT 데이터를 해당 날짜 범위로 조회
                        var logUsageData = apiCallLogRepository.findLogUsageDataByCompanyIdsAndDateRange(
                                Collections.singletonList(company.getId()),
                                date.atStartOfDay(zoneId).toInstant(),
                                date.atTime(23, 59, 59).atZone(zoneId).toInstant());
                        //log.info("Fetched {} API Call Logs for Company ID: {}, Date: {}", logUsageData.size(), company.getId(), date);
                        // API 호출 로그를 날짜별로 그룹화
                        var apiCallLogMap = logUsageData.stream()
                                .collect(Collectors.groupingBy(
                                        ApiCallLogDto.LogUsageData::getCompanyId,
                                        Collectors.groupingBy(
                                                logData -> logData.getCreatedDateAsLocalDate(zoneId),
                                                Collectors.summingLong(ApiCallLogDto.LogUsageData::getCount)
                                        )
                                ));
                        // IoT 설치 개수 가져오기 (iot_status_history 테이블에서 해당 날짜에 등록된 IoT 장비 개수)
                        var iotInstallationCount = iotStatusHistoryRepository.countIotInstallationsByCompanyAndDateRange(
                                company.getId(),
                                date.atStartOfDay(zoneId).toInstant(),
                                date.atTime(23, 59, 59).atZone(zoneId).toInstant());
                        //log.info("Fetched IoT Installation Count: {} for Company ID: {}, Date: {}", iotInstallationCount, company.getId(), date);
                        // MeteredUsage 데이터를 조회하여 존재 여부 확인
                        var meteredUsage = meteredUsageRepository.findByCompanyAndUsageDate(company, date).orElse(null);
                        if (meteredUsage == null) {
                            // MeteredUsage가 존재하지 않으면 생성
                            meteredUsage = MeteredUsage.builder()
                                    .company(company)
                                    .usageDate(date)
                                    .apiCallCount(apiCallLogMap.getOrDefault(company.getId(), Collections.emptyMap()).getOrDefault(date, 0L))
                                    .iotInstallationCount(iotInstallationCount.intValue())
                                    .build();
                            meteredUsageRepository.save(meteredUsage);
                            //log.info("Created MeteredUsage for Company ID: {}, Date: {}", company.getId(), date);
                        } else {
                            // MeteredUsage가 존재하면 수정
                            meteredUsage.setApiCallCount(apiCallLogMap.getOrDefault(company.getId(), Collections.emptyMap()).getOrDefault(date, 0L));
                            meteredUsage.setIotInstallationCount(iotInstallationCount.intValue());
                            meteredUsageRepository.save(meteredUsage);
                            //log.info("Updated MeteredUsage for Company ID: {}, Date: {}", company.getId(), date);
                        }
                    });
        });
        return RepeatStatus.FINISHED;
    }
}