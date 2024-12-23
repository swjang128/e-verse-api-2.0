package atemos.everse.api.dto;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.domain.IotType;
import atemos.everse.api.entity.IotStatusHistory;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT 장비 이력 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class IotStatusHistoryDto {
    /**
     * 새로운 IoT 장비 이력 생성을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateIotHistory {
        /**
         * IoT 장비 ID
         * - 예: 1
         */
        @Schema(description = "IoT ID", defaultValue = "1")
        @Positive
        private Long iotId;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        @Schema(description = "상태", defaultValue = "NORMAL")
        @Enumerated(EnumType.STRING)
        @Size(max = 6)
        private IotStatus status;
        /**
         * 가동량
         * - 예: 110
         */
        @Schema(description = "가동량", defaultValue = "110")
        @PositiveOrZero
        private BigDecimal facilityUsage;
    }

    /**
     * IoT 장비 이력 업데이트를 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateIotHistory {
        /**
         * IoT 장비 ID
         * - 예: 1
         */
        @Schema(description = "IoT ID", defaultValue = "1")
        @Positive
        private Long iotId;
        /**
         * IoT 장비 상태
         * - 예: IDLE
         */
        @Schema(description = "상태", defaultValue = "IDLE")
        @Enumerated(EnumType.STRING)
        @Size(max = 10)
        private IotStatus status;
        /**
         * 가동량
         * - 예: 150
         */
        @Schema(description = "가동량", defaultValue = "150")
        @PositiveOrZero
        private BigDecimal facilityUsage;
    }

    /**
     * IoT 장비 이력 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadIotHistoryRequest {
        /**
         * IoT 장비 이력 ID
         * - 예: 1
         */
        @Positive
        private List<Long> iotHistoryId;
        /**
         * IoT 장비 ID
         * - 예: 1
         */
        @Positive
        private List<Long> iotId;
        /**
         * 업체 ID
         * - 예: 1
         */
        @Positive
        private Long companyId;
        /**
         * 시리얼 넘버
         * - 예: "atemos-1234"
         */
        @Size(max = 30)
        private String serialNumber;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        private List<IotStatus> status;
        /**
         * IoT 장비 유형
         * - 예: AIR_CONDITIONER, MOTOR, RADIATOR, ETC
         */
        private List<IotType> type;
        /**
         * 설치 위치
         * - 예: "1층 분전반"
         */
        @Size(max = 50)
        private String location;
        /**
         * 최소 가동량
         * - 예: 0
         */
        @PositiveOrZero
        private BigDecimal minimumFacilityUsage;
        /**
         * 최대 가동량
         * - 예: 1000
         */
        @PositiveOrZero
        private BigDecimal maximumFacilityUsage;
        /**
         * 최소 단가
         * - 예: 50 (달러)
         */
        @PositiveOrZero
        private BigDecimal minimumPrice;
        /**
         * 최대 단가
         * - 예: 100 (달러)
         */
        @PositiveOrZero
        private BigDecimal maximumPrice;
        /**
         * 조회 시작일
         */
        private LocalDate startDate;
        /**
         * 조회 종료일
         */
        private LocalDate endDate;
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
        /**
         * 시간별 데이터를 포함할지 여부
         */
        private boolean isHourly;
    }

    /**
     * IoT 장비 이력 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadIotHistoryResponse {
        /**
         * IoT 장비 이력 ID
         * - 예: 1
         */
        private Long iotHistoryId;
        /**
         * IoT 장비 ID
         * - 예: 1
         */
        private Long iotId;
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
         * 시리얼 넘버
         * - 예: "atemos-1234"
         */
        private String serialNumber;
        /**
         * IoT 장비 유형
         * - 예: AIR_CONDITIONER, MOTOR, RADIATOR, ETC
         */
        private IotType type;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        private IotStatus status;
        /**
         * 설치 위치
         * - 예: "1층 분전반"
         */
        private String location;
        /**
         * 집계 시각
         */
        private LocalDateTime referenceTime;
        /**
         * IoT 기기 상태별 개수
         */
        private Map<IotStatus, Long> iotStatus;
        /**
         * 데이터 생성일
         * - 예: "2024-07-22T14:30:00"
         */
        private Instant createdDate;
        /**
         * IoT 장비 이력 엔티티를 기반으로 DTO를 생성하는 생성자
         */
        public ReadIotHistoryResponse(IotStatusHistory iotStatusHistory) {
            this.iotHistoryId = iotStatusHistory.getId();
            this.iotId = iotStatusHistory.getIot().getId();
            this.companyId = iotStatusHistory.getIot().getCompany().getId();
            this.companyName = iotStatusHistory.getIot().getCompany().getName();
            this.serialNumber = iotStatusHistory.getIot().getSerialNumber();
            this.type = iotStatusHistory.getIot().getType();
            this.status = iotStatusHistory.getStatus();
            this.location = iotStatusHistory.getIot().getLocation();
            this.createdDate = iotStatusHistory.getCreatedDate();
        }
    }

    /**
     * IoT 현황 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadIotHistoryPageResponse {
        /**
         * 집계 시각
         */
        private LocalDateTime referenceTime;
        /**
         * IoT 현황 목록
         */
        private List<IotStatusHistoryDto.ReadIotHistoryResponse> iotHistoryList;
        /**
         * IoT 기기 상태별 개수
         */
        private Map<IotStatus, Long> iotStatus;
        /**
         * 전체 row 개수
         */
        private Long totalElements;
        /**
         * 전체 페이지 수
         */
        private Integer totalPages;
    }
}