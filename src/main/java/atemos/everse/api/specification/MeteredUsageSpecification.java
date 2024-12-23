package atemos.everse.api.specification;

import atemos.everse.api.dto.MeteredUsageDto;
import atemos.everse.api.entity.MeteredUsage;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * MeteredUsage 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class MeteredUsageSpecification {
    /**
     * 주어진 MeteredUsageDto.ReadMeteredUsageRequest를 기반으로 MeteredUsage 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readMeteredUsageRequestDto MeteredUsage 조회 조건을 포함하는 데이터 전송 객체
     * @param zoneId                     사용자 또는 회사의 타임존 정보
     * @return 조건에 맞는 MeteredUsage 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<MeteredUsage> findWith(MeteredUsageDto.ReadMeteredUsageRequest readMeteredUsageRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // MeteredUsage ID 조건 추가
            if (readMeteredUsageRequestDto.getMeteredUsageId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readMeteredUsageRequestDto.getMeteredUsageId()));
            }
            // 업체 ID 조건 추가
            if (readMeteredUsageRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("company").get("id"), readMeteredUsageRequestDto.getCompanyId()));
            }
            // 서비스 사용 내역 조회 월 조건 추가 (주어진 ZoneId의 LocalDateTime을 UTC로 변환하여 조회)
            if (readMeteredUsageRequestDto.getUsageMonth() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(
                        root.get("usageDate"),
                        ZonedDateTime.of(readMeteredUsageRequestDto.getUsageMonth().atStartOfDay(), zoneId).toInstant(),
                        ZonedDateTime.of(readMeteredUsageRequestDto.getUsageMonth().plusMonths(1).atStartOfDay(), zoneId).toInstant()));
            }
            // 유료 API Call 횟수 조건 추가
            if (readMeteredUsageRequestDto.getMinimumApiCallCount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("apiCallCount"), readMeteredUsageRequestDto.getMinimumApiCallCount()));
            }
            if (readMeteredUsageRequestDto.getMaximumApiCallCount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("apiCallCount"), readMeteredUsageRequestDto.getMaximumApiCallCount()));
            }
            // IoT 설비 설치 개수 조건 추가
            if (readMeteredUsageRequestDto.getMinimumIotInstallationCount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("iotInstallationCount"), readMeteredUsageRequestDto.getMinimumIotInstallationCount()));
            }
            if (readMeteredUsageRequestDto.getMaximumIotInstallationCount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("iotInstallationCount"), readMeteredUsageRequestDto.getMaximumIotInstallationCount()));
            }
            return predicate;
        };
    }
}