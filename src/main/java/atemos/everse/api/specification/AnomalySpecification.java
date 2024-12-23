package atemos.everse.api.specification;

import atemos.everse.api.dto.AnomalyDto;
import atemos.everse.api.entity.Anomaly;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * AnomalySpecification 클래스는 Anomaly 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 * 다양한 필터링 조건을 지원하며, 주어진 조건에 따라 이상 탐지 데이터를 조회하는 데 사용됩니다.
 */
public class AnomalySpecification {
    /**
     * 주어진 AnomalyDto.ReadAnomalyRequest 객체를 기반으로 Anomaly 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readAnomalyRequestDto Anomaly 조회 조건을 포함하는 데이터 전송 객체
     * @param zoneId 클라이언트의 타임존
     * @return 주어진 조건에 맞는 Anomaly 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Anomaly> findWith(AnomalyDto.ReadAnomalyRequest readAnomalyRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 초기화
            var predicate = criteriaBuilder.conjunction();
            // Anomaly ID 조건 추가
            if (readAnomalyRequestDto.getAnomalyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readAnomalyRequestDto.getAnomalyId()));
            }
            // 업체 ID 조건 추가
            if (readAnomalyRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readAnomalyRequestDto.getCompanyId()));
            }
            // 최소 에너지 사용량 조건 추가
            if (readAnomalyRequestDto.getMinimumLowestHourlyEnergyUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("lowestHourlyEnergyUsage"), readAnomalyRequestDto.getMinimumLowestHourlyEnergyUsage()));
            }
            // 최대 에너지 사용량 조건 추가
            if (readAnomalyRequestDto.getMaximumLowestHourlyEnergyUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("lowestHourlyEnergyUsage"), readAnomalyRequestDto.getMaximumLowestHourlyEnergyUsage()));
            }
            // 최소 최고 에너지 사용량 조건 추가
            if (readAnomalyRequestDto.getMinimumHighestHourlyEnergyUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("highestHourlyEnergyUsage"), readAnomalyRequestDto.getMinimumHighestHourlyEnergyUsage()));
            }
            // 최대 최고 에너지 사용량 조건 추가
            if (readAnomalyRequestDto.getMaximumHighestHourlyEnergyUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("highestHourlyEnergyUsage"), readAnomalyRequestDto.getMaximumHighestHourlyEnergyUsage()));
            }
            // 알림 여부 조건 추가
            if (readAnomalyRequestDto.getAvailable() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("available"), readAnomalyRequestDto.getAvailable()));
            }
            // 종료 날짜 조건 추가
            if (readAnomalyRequestDto.getEndDate() != null) {
                var endDate = readAnomalyRequestDto.getEndDate().withHour(23).withMinute(59).withSecond(59);
                var endDateUTC = ZonedDateTime.of(endDate, zoneId).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDateUTC));
            }
            // 시작 날짜 조건 추가
            if (readAnomalyRequestDto.getStartDate() != null) {
                var startDateUTC = ZonedDateTime.of(readAnomalyRequestDto.getStartDate(), zoneId).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDateUTC));
            }
            return predicate;
        };
    }
}