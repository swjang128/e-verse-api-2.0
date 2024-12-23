package atemos.everse.api.dto;

import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.entity.Subscription;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Subscription 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class SubscriptionDto {
    /**
     * 새로운 구독 정보를 생성하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateSubscription {
        /**
         * 구독 정보가 속한 업체의 ID입니다.
         * - 반드시 입력해야 하며, 1 이상의 자연수가 들어갑니다.
         */
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;
        /**
         * 구독한 서비스 이름입니다.
         * - 반드시 입력해야 하며, 최대 50자까지 입력 가능합니다.
         */
        @Schema(description = "구독한 서비스 이름", example = "AI_ENERGY_USAGE_FORECAST")
        @Size(max = 50)
        @Enumerated(EnumType.STRING)
        private SubscriptionServiceList service;
        /**
         * 구독 시작 날짜입니다.
         * - 반드시 입력해야 하며, YYYY-MM-DD 형식입니다.
         */
        @Schema(description = "구독 시작 날짜", example = "2024-07-01")
        @NotNull(message = "startDate는 null일 수 없습니다.")
        private LocalDate startDate;
        /**
         * 구독 종료 날짜입니다.
         * - 일반적으로 등록에서는 이 값은 null입니다.
         * - 구독 종료일을 지정할 수 있습니다.
         * - 구독이 종료되지 않았다면 null일 수 있습니다.
         */
        @Schema(description = "구독 종료 날짜", nullable = true)
        private LocalDate endDate;
    }

    /**
     * Subscription 정보를 조회할 때 요청할 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadSubscriptionRequest {
        /**
         * Subscription ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> subscriptionId;
        /**
         * Company ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private Long companyId;
        /**
         * 구독한 서비스 이름입니다.
         * - 예: "AI_ENERGY_USAGE_FORECAST"
         */
        private List<SubscriptionServiceList> serviceList;
        /**
         * 특정 날짜에 구독을 하는지 체크하는 파라미터입니다.
         * - YYYY-MM-DD 형식입니다.
         */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "searchDate 반드시 YYYY-MM-DD 형식이어야 합니다.")
        private LocalDate searchDate;
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
     * Subscription 정보를 조회할 때 응답으로 반환되는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadSubscriptionResponse {
        /**
         * Subscription ID를 나타냅니다.
         * - 예: 1
         */
        private Long subscriptionId;
        /**
         * 업체 ID
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체명
         * - 예: "아테모스"
         */
        private String companyName;
        /**
         * 구독한 서비스 목록입니다.
         * - 예: "AI_ENERGY_USAGE_FORECAST"
         */
        private SubscriptionServiceList service;
        /**
         * 구독 시작 날짜입니다.
         * - 예: 2024-07-01
         */
        private LocalDate startDate;
        /**
         * 구독 종료 날짜입니다.
         * - 예: 2024-07-31
         */
        private LocalDate endDate;
        /**
         * Subscription 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * Subscription 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;

        /**
         * Subscription 엔티티를 기반으로 DTO를 생성합니다.
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param subscription Subscription 엔티티 객체
         */
        public ReadSubscriptionResponse(Subscription subscription, ZoneId zoneId) {
            this.subscriptionId = subscription.getId();
            this.companyId = subscription.getCompany().getId();
            this.companyName = subscription.getCompany().getName();
            this.service = subscription.getService();
            this.startDate = subscription.getStartDate();
            this.endDate = subscription.getEndDate();
            this.createdDate = subscription.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = subscription.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 구독 정보 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadSubscriptionPageResponse {
        /**
         * 구독 정보 목록
         */
        private List<ReadSubscriptionResponse> subscriptionList;
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
     * Subscription 설정을 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateSubscription {
        /**
         * Company ID를 나타냅니다.
         * - 반드시 입력해야 하며, 1 이상의 자연수가 들어갑니다.
         */
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;
        /**
         * 구독한 서비스 목록입니다.
         * - 반드시 입력해야 하며, 최대 50자까지 입력 가능합니다.
         */
        @Schema(description = "구독한 서비스 이름")
        @Size(max = 50)
        @Enumerated(EnumType.STRING)
        private SubscriptionServiceList service;
        /**
         * 구독 시작 날짜입니다.
         * - 반드시 입력해야 하며, YYYY-MM-DD 형식입니다.
         */
        @Schema(description = "구독 시작 날짜", example = "2024-07-01")
        private LocalDate startDate;
        /**
         * 구독 종료 날짜입니다.
         * - 구독이 종료되지 않았다면 null일 수 있습니다.
         */
        @Schema(description = "구독 종료 날짜", nullable = true)
        private LocalDate endDate;
    }
}