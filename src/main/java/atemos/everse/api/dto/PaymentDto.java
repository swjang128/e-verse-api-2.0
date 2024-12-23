package atemos.everse.api.dto;

import atemos.everse.api.domain.PaymentMethod;
import atemos.everse.api.domain.PaymentStatus;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.entity.Payment;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 결제 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class PaymentDto {
    /**
     * 새로운 결제를 생성하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreatePayment {
        /**
         * 결제와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 서비스 사용 내역의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "서비스 사용 내역 ID", defaultValue = "1")
        @Positive
        private Long meteredUsageId;
        /**
         * 업체가 특정 일에 구독하던 서비스 목록입니다.
         * - 예: [AI_ENERGY_USAGE_FORECAST,INTERACTIVE_AI]
         */
        @Schema(description = "업체가 특정일에 구독하던 서비스 목록")
        @Enumerated(EnumType.STRING)
        private List<SubscriptionServiceList> subscriptionServiceList;
        /**
         * 해당 업체의 데이터베이스 저장소 사용량입니다.
         * - 단위는 Byte 단위입니다.
         * - 해당 시점의 데이터베이스 저장소 사용량을 담습니다.
         */
        @Schema(description = "데이터베이스 저장소 사용량", defaultValue = "0")
        @PositiveOrZero
        private Long storageUsage;
        /**
         * 결제 방법을 나타냅니다.
         * - 예: "CARD" (카드 결제), "TRANSFER" (이체)
         * - 최대 8자까지 허용됩니다.
         */
        @Schema(description = "결제 방법", defaultValue = "CARD")
        @Enumerated(EnumType.STRING)
        @Size(max = 8)
        private PaymentMethod method;
        /**
         * 지불할 금액을 나타냅니다.
         * - 예: 50000
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "지불액", defaultValue = "50000.00")
        @PositiveOrZero
        private BigDecimal amount;
        /**
         * 결제 상태를 나타냅니다.
         * - 예: "OUTSTANDING" (미결제), "COMPLETE" (완료)
         * - 최대 12자까지 허용됩니다.
         */
        @Schema(description = "결제 상태", defaultValue = "OUTSTANDING")
        @Enumerated(EnumType.STRING)
        @Size(max = 12)
        private PaymentStatus status;
        /**
         * 결제 내역의 기준 사용 날짜를 나타냅니다.
         * - 예: 2024-08-01
         */
        @Schema(description = "결제 내역의 기준 사용 날짜", defaultValue = "2024-08-01")
        private LocalDate usageDate;
        /**
         * 결제 예정일을 나타냅니다.
         * - 결제 시스템이 다음 결제 날짜를 관리하는데 사용됩니다.
         * - 예: 2024-08-25
         */
        @Schema(description = "결제 예정일", defaultValue = "2024-08-25")
        private LocalDate scheduledPaymentDate;
    }

    /**
     * Payment 정보를 조회할 때 요청할 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadPaymentRequest {
        /**
         * Payment ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> paymentId;
        /**
         * Company ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> companyId;
        /**
         * 서비스 사용 내역 ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> meteredUsageId;
        /**
         * 업체가 특정 일에 구독하던 서비스 목록입니다.
         * - 예: [AI_ENERGY_USAGE_FORECAST,INTERACTIVE_AI]
         */
        private List<SubscriptionServiceList> subscriptionServiceList;
        /**
         * 결제 방법을 나타냅니다.
         * - 예: "CARD" (카드 결제), "TRANSFER" (이체)
         * - 최대 8자까지 허용됩니다.
         */
        private List<PaymentMethod> method;
        /**
         * 지불할 금액의 최소값을 나타냅니다.
         * - 예: 0
         * - 0 이상의 값만 허용됩니다.
         */
        @PositiveOrZero
        private BigDecimal minimumAmount;
        /**
         * 지불할 금액의 최대값을 나타냅니다.
         * - 예: 100000
         * - 0 이상의 값만 허용됩니다.
         */
        @PositiveOrZero
        private BigDecimal maximumAmount;
        /**
         * 결제 상태를 나타냅니다.
         * - 예: "OUTSTANDING" (미결제), "COMPLETE" (완료)
         * - 최대 12자까지 허용됩니다.
         */
        private List<PaymentStatus> status;
        /**
         * 결제 내역의 기준 사용 날짜에 대한 조회 시작일을 나타냅니다.
         * - 예: 2024-08-01
         */
        private LocalDate usageDateStart;
        /**
         * 결제 내역의 기준 사용 날짜에 대한 조회 종료일을 나타냅니다.
         * - 예: 2024-08-31
         */
        private LocalDate usageDateEnd;
        /**
         * 결제 예정일에 대한 조회 시작일을 나타냅니다.
         * - 예: 2024-08-01
         */
        private LocalDate scheduledPaymentDateStart;
        /**
         * 결제 예정일에 대한 조회 종료일을 나타냅니다.
         * - 예: 2024-09-30
         */
        private LocalDate scheduledPaymentDateEnd;
        /**
         * 페이지 번호를 나타냅니다. 0 이상의 정수를 갖습니다.
         * 페이지 번호 + 1이 페이지 번호가 됩니다. (ex. 0 = 1페이지)
         * - 예: 0
         */
        @PositiveOrZero
        private Integer page;
        /**
         * 페이지당 row의 개수를 나타냅니다. 1 이상의 자연수를 갖습니다.
         */
        @Positive
        private Integer size;
    }

    /**
     * 결제 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadPaymentResponse {
        /**
         * 결제 ID를 나타냅니다.
         * - 예: 1
         */
        private Long paymentId;
        /**
         * 결제와 관련된 업체 ID를 나타냅니다.
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체의 이름을 나타냅니다.
         * - 예: "ACME Corp"
         */
        private String companyName;
        /**
         * 결제와 관련된 서비스 사용 내역 ID를 나타냅니다.
         * - 예: 1
         */
        private Long meteredUsageId;
        /**
         * 업체가 특정 일에 구독하던 서비스 목록입니다.
         * - 예: [AI_ENERGY_USAGE_FORECAST,INTERACTIVE_AI]
         */
        private List<SubscriptionServiceList> subscriptionServiceList;
        /**
         * 유료 API Call 횟수입니다.
         * - 0 이상의 양수가 들어갑니다.
         */
        private Long apiCallCount;
        /**
         * IoT 설비 설치 개수
         * - 0 이상의 양수가 들어갑니다.
         */
        private Integer iotInstallationCount;
        /**
         * 데이터베이스 저장소 사용량
         * - 0 이상의 양수가 들어갑니다.
         */
        private Long storageUsage;
        /**
         * 결제 방법을 나타냅니다.
         * - 예: "CARD" (카드 결제)
         */
        private PaymentMethod method;
        /**
         * 지불한 금액을 나타냅니다.
         * - 예: 50000.00
         */
        private BigDecimal amount;
        /**
         * 결제 상태를 나타냅니다.
         * - 예: "OUTSTANDING" (미결제)
         */
        private PaymentStatus status;
        /**
         * 결제 내역의 기준 사용 날짜를 나타냅니다.
         * - 예: 2024-08-01
         */
        private LocalDate usageDate;
        /**
         * 결제 예정일을 나타냅니다.
         * - 결제 시스템이 다음 결제 날짜를 관리하는데 사용됩니다.
         * - 예: 2024-08-25
         */
        private LocalDate scheduledPaymentDate;
        /**
         * 결제 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * 결제 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;
        /**
         * Payment 엔티티를 기반으로 DTO 생성자 추가
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param payment 결제 엔티티 객체
         */
        public ReadPaymentResponse(Payment payment, ZoneId zoneId) {
            this.paymentId = payment.getId();
            this.companyId = payment.getCompany().getId();
            this.companyName = payment.getCompany().getName();
            this.meteredUsageId = payment.getMeteredUsage() != null ? payment.getMeteredUsage().getId() : null;
            this.subscriptionServiceList = payment.getSubscriptionServiceList();
            this.storageUsage = payment.getStorageUsage();
            this.apiCallCount = payment.getMeteredUsage() != null ? payment.getMeteredUsage().getApiCallCount() : null;
            this.iotInstallationCount = payment.getMeteredUsage() != null ? payment.getMeteredUsage().getIotInstallationCount() : null;
            this.method = payment.getMethod();
            this.amount = payment.getAmount();
            this.status = payment.getStatus();
            this.usageDate = payment.getUsageDate();
            this.scheduledPaymentDate = payment.getScheduledPaymentDate();
            this.createdDate = payment.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = payment.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 결제 내역 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadPaymentPageResponse {
        /**
         * 결제 내역 목록
         */
        private List<ReadPaymentResponse> paymentList;
        /**
         * 지정한 기간 내 유료 API 호출 건수의 합계
         */
        private Integer summaryApiCallCount;
        /**
         * 지정한 기간 내 가장 최근 usageDate에 해당하는 IoT 설치 개수
         */
        private Integer recentlyIotInstallationCount;
        /**
         * 지정한 기간 내 가장 최근 usageDate에 해당하는 데이터베이스 저장소 사용량
         */
        private Map<Long,Long> recentlyStorageUsage;
        /**
         * 지정한 기간 내 각 SubscriptionService를 몇 일씩 구독했는지 산출
         */
        private Map<Long, Map<SubscriptionServiceList, Long>> subscribedCount;
        /**
         * 지정한 기간 내 AI 구독 요금
         */
        private BigDecimal summarySubscriptionAmount;

        /**
         * 지정한 기간 내 API 호출 요금
         */
        private BigDecimal summaryApiCallAmount;

        /**
         * 지정한 기간 내 IoT 설비 요금
         */
        private BigDecimal summaryIotInstallationAmount;

        /**
         * 지정한 기간 내 데이터 사용량 요금
         */
        private BigDecimal summaryStorageUsageAmount;
        /**
         *
         * 지정한 기간 내 합산 요금
         */
        private BigDecimal summaryAmount;
        /**
         * 전체 row 개수
         */
        private long totalElements;
        /**
         * 전체 페이지 수
         */
        private int totalPages;
    }

    /**
     * 결제를 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePayment {
        /**
         * 결제와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 서비스 사용 내역의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "서비스 사용 내역 ID", defaultValue = "1")
        @Positive
        private Long meteredUsageId;
        /**
         * 업체가 특정 일에 구독하던 서비스 목록입니다.
         * - 예: [AI_ENERGY_USAGE_FORECAST,INTERACTIVE_AI]
         */
        @Schema(description = "업체가 특정일에 구독하던 서비스 목록")
        private List<SubscriptionServiceList> subscriptionServiceList;
        /**
         * 해당 업체의 데이터베이스 저장소 사용량입니다.
         * - 단위는 Byte 단위입니다.
         * - 해당 시점의 데이터베이스 저장소 사용량을 담습니다.
         */
        @Schema(description = "데이터베이스 저장소 사용량", defaultValue = "0")
        @PositiveOrZero
        private Long storageUsage;
        /**
         * 결제 방법을 나타냅니다.
         * - 예: "CARD" (카드 결제), "TRANSFER" (이체)
         * - 최대 8자까지 허용됩니다.
         */
        @Schema(description = "결제 방법", defaultValue = "CARD")
        @Enumerated(EnumType.STRING)
        @Size(max = 8)
        private PaymentMethod method;
        /**
         * 지불할 금액을 나타냅니다.
         * - 예: 50000
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "지불액", defaultValue = "50000.00")
        @PositiveOrZero
        private BigDecimal amount;
        /**
         * 결제 상태를 나타냅니다.
         * - 예: "OUTSTANDING" (미결제), "COMPLETE" (완료)
         * - 최대 12자까지 허용됩니다.
         */
        @Schema(description = "결제 상태", defaultValue = "OUTSTANDING")
        @Enumerated(EnumType.STRING)
        @Size(max = 12)
        private PaymentStatus status;
        /**
         * 결제 내역의 기준 사용 날짜를 나타냅니다.
         * - 예: 2024-08-01
         */
        @Schema(description = "결제 내역의 기준 사용 날짜", defaultValue = "2024-08-01")
        private LocalDate usageDate;
        /**
         * 결제 예정일을 나타냅니다.
         * - 결제 시스템이 다음 결제 날짜를 관리하는데 사용됩니다.
         * - 예: 2024-08-25
         */
        @Schema(description = "결제 예정일", defaultValue = "2024-08-25")
        private LocalDate scheduledPaymentDate;
    }
}