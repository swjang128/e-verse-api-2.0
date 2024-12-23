package atemos.everse.api.specification;

import atemos.everse.api.dto.PaymentDto;
import atemos.everse.api.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Payment 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class PaymentSpecification {
    /**
     * 주어진 PaymentDto.ReadPaymentRequest를 기반으로 Payment 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readPaymentRequestDto Payment 조회 조건을 포함하는 데이터 전송 객체
     * @param zoneId 클라이언트의 타임존 정보
     * @return 조건에 맞는 Payment 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Payment> findWith(PaymentDto.ReadPaymentRequest readPaymentRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction(); // 기본 조건 생성
            // Payment ID 조건 추가
            if (readPaymentRequestDto.getPaymentId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readPaymentRequestDto.getPaymentId()));
            }
            // 업체 ID 조건 추가
            if (readPaymentRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readPaymentRequestDto.getCompanyId()));
            }
            // 서비스 사용 내역 ID 조건 추가
            if (readPaymentRequestDto.getMeteredUsageId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("meteredUsage").get("id").in(readPaymentRequestDto.getMeteredUsageId()));
            }
            // 구독 서비스 목록 조건 추가 (member of로 변경)
            if (readPaymentRequestDto.getSubscriptionServiceList() != null && !readPaymentRequestDto.getSubscriptionServiceList().isEmpty()) {
                for (var service : readPaymentRequestDto.getSubscriptionServiceList()) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(service, root.get("subscriptionServiceList")));
                }
            }
            // 결제 방법 조건 추가
            if (readPaymentRequestDto.getMethod() != null && !readPaymentRequestDto.getMethod().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("method").in(readPaymentRequestDto.getMethod()));
            }
            // 지불할 금액 조건 추가
            if (readPaymentRequestDto.getMinimumAmount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), readPaymentRequestDto.getMinimumAmount()));
            }
            if (readPaymentRequestDto.getMaximumAmount() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("amount"), readPaymentRequestDto.getMaximumAmount()));
            }
            // 결제 상태 조건 추가
            if (readPaymentRequestDto.getStatus() != null && !readPaymentRequestDto.getStatus().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(readPaymentRequestDto.getStatus()));
            }
            // 결제 내역의 기준 사용 날짜 조건 추가 (UsageDate)
            if (readPaymentRequestDto.getUsageDateStart() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(
                        root.get("usageDate"),
                        ZonedDateTime.of(readPaymentRequestDto.getUsageDateStart().atStartOfDay(), zoneId).toInstant()));
            }
            if (readPaymentRequestDto.getUsageDateEnd() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(
                        root.get("usageDate"),
                        ZonedDateTime.of(readPaymentRequestDto.getUsageDateEnd().atStartOfDay().plusDays(1), zoneId).toInstant()));
            }
            // 결제 예정일 조건 추가 (ScheduledPaymentDate)
            if (readPaymentRequestDto.getScheduledPaymentDateStart() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(
                        root.get("scheduledPaymentDate"),
                        ZonedDateTime.of(readPaymentRequestDto.getScheduledPaymentDateStart().atStartOfDay(), zoneId).toInstant()));
            }
            if (readPaymentRequestDto.getScheduledPaymentDateEnd() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(
                        root.get("scheduledPaymentDate"),
                        ZonedDateTime.of(readPaymentRequestDto.getScheduledPaymentDateEnd().atStartOfDay().plusDays(1), zoneId).toInstant()));
            }
            return predicate;
        };
    }
}