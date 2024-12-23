package atemos.everse.api.specification;

import atemos.everse.api.dto.SubscriptionDto;
import atemos.everse.api.entity.Subscription;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Subscription 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class SubscriptionSpecification {
    /**
     * 주어진 SubscriptionDto.ReadSubscriptionRequest를 기반으로 Subscription 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readSubscriptionRequestDto 구독 정보 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Subscription 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Subscription> findWith(SubscriptionDto.ReadSubscriptionRequest readSubscriptionRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 구독 정보 ID 조건 추가
            if (readSubscriptionRequestDto.getSubscriptionId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readSubscriptionRequestDto.getSubscriptionId()));
            }
            // 업체 ID 조건 추가
            if (readSubscriptionRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("company").get("id"), readSubscriptionRequestDto.getCompanyId()));
            }
            // 구독한 서비스 목록 조건 추가
            if (readSubscriptionRequestDto.getServiceList() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("service").in(readSubscriptionRequestDto.getServiceList()));
            }
            // 특정 날짜에 구독 상태를 체크하는 조건 추가
            if (readSubscriptionRequestDto.getSearchDate() != null) {
                var searchDateStart = ZonedDateTime.of(readSubscriptionRequestDto.getSearchDate().atStartOfDay(), zoneId).toInstant();
                var searchDateEnd = ZonedDateTime.of(readSubscriptionRequestDto.getSearchDate().atStartOfDay().plusDays(1), zoneId).toInstant();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(
                                criteriaBuilder.and(
                                        criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), searchDateEnd),
                                        criteriaBuilder.or(
                                                criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), searchDateStart),
                                                criteriaBuilder.isNull(root.get("endDate"))
                                        )
                                )
                        )
                );
            }
            return predicate;
        };
    }
}