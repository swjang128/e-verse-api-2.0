package atemos.everse.api.dto;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.domain.IotType;
import atemos.everse.api.entity.Iot;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * IoT 장비 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class IotDto {
    /**
     * IoT 장비 생성을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateIot {
        /**
         * 업체 ID
         * - 예: 1
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 시리얼 넘버
         * - 예: "atemos-1234"
         */
        @Schema(description = "시리얼 넘버", defaultValue = "atemos-1234")
        @Size(max = 30)
        private String serialNumber;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        @Schema(description = "상태", defaultValue = "NORMAL")
        @Enumerated(EnumType.STRING)
        @Size(max = 6)
        private IotStatus status;
        /**
         * IoT 장비 유형
         * - 예: AIR_CONDITIONER, MOTOR, RADIATOR, ETC
         */
        @Schema(description = "유형", defaultValue = "AIR_CONDITIONER")
        @Enumerated(EnumType.STRING)
        @Size(max = 20)
        private IotType type;
        /**
         * 설치 위치
         * - 예: "1층 분전반"
         */
        @Schema(description = "설치 위치", defaultValue = "1층 분전반")
        @Size(max = 50)
        private String location;
        /**
         * 장비 단가
         * - 예: 60 (달러)
         */
        @Schema(description = "단가", defaultValue = "60")
        @PositiveOrZero
        private BigDecimal price;
    }

    /**
     * IoT 장비 업데이트를 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateIot {
        /**
         * 업체 ID
         * - 예: 2
         */
        @Schema(description = "업체 ID", defaultValue = "2")
        @Positive
        private Long companyId;
        /**
         * 시리얼 넘버
         * - 예: "atemos-6789"
         */
        @Schema(description = "시리얼 넘버", defaultValue = "atemos-6789")
        @Size(min = 2, max = 30)
        private String serialNumber;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        @Schema(description = "상태", defaultValue = "NORMAL")
        @Enumerated(EnumType.STRING)
        @Size(max = 6)
        private IotStatus status;
        /**
         * IoT 장비 유형
         * - 예: AIR_CONDITIONER, MOTOR, RADIATOR, ETC
         */
        @Schema(description = "유형", defaultValue = "RADIATOR")
        @Size(min = 1, max = 20)
        private IotType type;
        /**
         * 설치 위치
         * - 예: "2층 분전반"
         */
        @Schema(description = "설치 위치", defaultValue = "2층 분전반")
        @Size(max = 50)
        private String location;
        /**
         * 장비 단가
         * - 예: 50 (달러)
         */
        @Schema(description = "단가", defaultValue = "50")
        @PositiveOrZero
        private BigDecimal price;
    }

    /**
     * IoT 장비 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadIotRequest {
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
        private List<Long> companyId;
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
         * 최소 단가
         * - 예: 5 (달러)
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
     * IoT 장비 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadIotResponse {
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
         * 시리얼 넘버
         * - 예: "atemos-1234"
         */
        private String serialNumber;
        /**
         * IoT 장비 상태
         * - 예: NORMAL, ERROR
         */
        private IotStatus status;
        /**
         * IoT 장비 유형
         * - 예: AIR_CONDITIONER, MOTOR, RADIATOR, ETC
         */
        private IotType type;
        /**
         * 설치 위치
         * - 예: "1층 분전반"
         */
        private String location;
        /**
         * 장비 단가
         * - 예: 60 (달러)
         */
        private BigDecimal price;
        /**
         * 데이터 생성일
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * 데이터 수정일
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;
        /**
         * IoT 장비 엔티티를 기반으로 DTO를 생성하는 생성자
         */
        public ReadIotResponse(Iot iot, ZoneId zoneId) {
            this.iotId = iot.getId();
            this.companyId = iot.getCompany().getId();
            this.serialNumber = iot.getSerialNumber();
            this.status = iot.getStatus();
            this.type = iot.getType();
            this.location = iot.getLocation();
            this.price = iot.getPrice();
            this.createdDate = iot.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = iot.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * IoT 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadIotPageResponse {
        /**
         * IoT 현황 목록
         */
        private List<IotDto.ReadIotResponse> iotList;
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
     * IotUsageData 클래스는 특정 업체의 특정 날짜에 대한 IoT 설치 수를 나타내는 데이터 클래스입니다.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IotUsageData {
        private Long companyId;      // 업체 ID
        private Instant createdDate; // 생성 날짜
        private Long count;          // 설치 수

        /**
         * createdDate를 주어진 ZoneId를 사용해 LocalDate로 변환하는 메서드
         *
         * @param zoneId 타임존 정보
         * @return 변환된 LocalDate
         */
        public LocalDate getCreatedDateAsLocalDate(ZoneId zoneId) {
            // 국가의 시간대 정보를 사용하여 Instant를 LocalDate로 변환
            return createdDate != null ? LocalDate.ofInstant(createdDate, zoneId) : null;
        }
    }
}