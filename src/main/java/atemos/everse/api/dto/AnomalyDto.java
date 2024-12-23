package atemos.everse.api.dto;

import atemos.everse.api.entity.Anomaly;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Anomaly 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class AnomalyDto {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "새로운 Anomaly 설정을 생성하기 위한 DTO입니다.")
    public static class CreateAnomaly {
        /**
         * Anomaly와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;
        /**
         * 에너지 사용량의 시간당 최소 이상탐지치입니다.
         * - 예: 100
         * - 기본값은 100입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "에너지 사용량의 시간당 최소 이상탐지치", example = "100")
        @PositiveOrZero
        private BigDecimal lowestHourlyEnergyUsage;
        /**
         * 에너지 사용량의 시간당 최대 이상탐지치입니다.
         * - 예: 10000
         * - 기본값은 10000입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "에너지 사용량의 시간당 최대 이상탐지치", example = "10000")
        @PositiveOrZero
        private BigDecimal highestHourlyEnergyUsage;
        /**
         * Anomaly 설정의 활성화 여부를 나타냅니다.
         * - 예: true (활성화)
         */
        @Schema(description = "활성화 여부", example = "true", defaultValue = "true")
        private Boolean available;
    }

    /**
     * Anomaly 정보를 조회할 때 요청할 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAnomalyRequest {
        /**
         * Anomaly ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> anomalyId;
        /**
         * Anomaly와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> companyId;
        /**
         * 이상탐지에 등록된 최저 시간당 에너지 사용량의 최소값을 나타냅니다.
         * - 예: 0
         */
        @PositiveOrZero
        private BigDecimal minimumLowestHourlyEnergyUsage;
        /**
         * 이상탐지에 등록된 최저 시간당 에너지 사용량의 최대값을 나타냅니다.
         * - 예: 200
         */
        @PositiveOrZero
        private BigDecimal maximumLowestHourlyEnergyUsage;
        /**
         * 이상탐지에 등록된 최고 시간당 에너지 사용량의 최소값
         * - 예: 1000
         */
        @PositiveOrZero
        private BigDecimal minimumHighestHourlyEnergyUsage;
        /**
         * 이상탐지에 등록된 최고 시간당 에너지 사용량의 최대값
         * - 예: 4000
         */
        @PositiveOrZero
        private BigDecimal maximumHighestHourlyEnergyUsage;
        /**
         * Anomaly 설정의 활성화 여부를 나타냅니다.
         * - 예: true (활성화)
         */
        private Boolean available;
        /**
         * 조회 시작 날짜를 나타냅니다.
         * - 예: "2024-07-22T00:00:00"
         */
        private LocalDateTime startDate;
        /**
         * 조회 종료 날짜를 나타냅니다.
         * - 예: "2024-07-22T23:59:59"
         */
        private LocalDateTime endDate;
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
     * Anomaly 정보를 조회할 때 응답으로 반환되는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAnomalyResponse {
        /**
         * Anomaly ID를 나타냅니다.
         * - 예: 1
         */
        private Long anomalyId;
        /**
         * Anomaly와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체의 이름을 나타냅니다.
         * - 예: "아테모스"
         */
        private String companyName;
        /**
         * 에너지 사용량의 시간당 최소 이상탐지치입니다.
         * - 예: 100
         * - 기본값은 100입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        private BigDecimal lowestHourlyEnergyUsage;
        /**
         * 에너지 사용량의 시간당 최대 이상탐지치입니다.
         * - 예: 10000
         * - 기본값은 10000입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        private BigDecimal highestHourlyEnergyUsage;
        /**
         * Anomaly 설정의 활성화 여부를 나타냅니다.
         * - 예: true (활성화)
         */
        private Boolean available;
        /**
         * Anomaly 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * Anomaly 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;

        /**
         * Anomaly 엔티티를 기반으로 DTO를 생성합니다.
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param anomaly Anomaly 엔티티 객체
         */
        public ReadAnomalyResponse(Anomaly anomaly, ZoneId zoneId) {
            this.anomalyId = anomaly.getId();
            this.companyId = anomaly.getCompany().getId();
            this.companyName = anomaly.getCompany().getName();
            this.lowestHourlyEnergyUsage = anomaly.getLowestHourlyEnergyUsage();
            this.highestHourlyEnergyUsage = anomaly.getHighestHourlyEnergyUsage();
            this.available = anomaly.getAvailable();
            this.createdDate = anomaly.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = anomaly.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 이상탐지 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAnomalyPageResponse {
        /**
         * 이상탐지 목록
         */
        private List<AnomalyDto.ReadAnomalyResponse> anomalyList;
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
     * Anomaly 설정을 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateAnomaly {
        /**
         * Anomaly와 관련된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 에너지 사용량의 시간당 최소 이상탐지치입니다.
         * - 예: 100
         * - 기본값은 100입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "에너지 사용량의 시간당 최소 이상탐지치", defaultValue = "100")
        @PositiveOrZero
        private BigDecimal lowestHourlyEnergyUsage;
        /**
         * 에너지 사용량의 시간당 최대 이상탐지치입니다.
         * - 예: 10000
         * - 기본값은 10000입니다.
         * - 0 이상의 값만 허용됩니다.
         */
        @Schema(description = "에너지 사용량의 시간당 최대 이상탐지치", defaultValue = "10000")
        @PositiveOrZero
        private BigDecimal highestHourlyEnergyUsage;
        /**
         * Anomaly 설정의 활성화 여부를 나타냅니다.
         * - 예: false (비활성화)
         */
        @Schema(description = "활성화 여부", defaultValue = "false")
        private Boolean available;
    }
}