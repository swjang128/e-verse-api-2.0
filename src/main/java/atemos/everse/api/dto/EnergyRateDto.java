package atemos.everse.api.dto;

import atemos.everse.api.domain.EnergyRatePeakType;
import atemos.everse.api.entity.EnergyRate;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class EnergyRateDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateEnergyRate {
        @Schema(description = "국가 ID", defaultValue = "1")
        @Positive
        private Long countryId;

        @Schema(description = "산업용 전력 요금", example = "0.7823")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal industrialRate;

        @Schema(description = "상업용 전력 요금", example = "0.6319")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal commercialRate;

        @Schema(description = "피크 시간대 요금 증감율", example = "1.5")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal peakMultiplier;

        @Schema(description = "경피크 시간대 요금 증감율", example = "1.2")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal midPeakMultiplier;

        @Schema(description = "비피크 시간대 요금 증감율", example = "0.8")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal offPeakMultiplier;

        @Schema(description = "피크 시간대 리스트", example = "[10, 11, 17, 18, 19]")
        private List<Integer> peakHours;

        @Schema(description = "경피크 시간대 리스트", example = "[8, 9, 12, 13, 14, 15, 16]")
        private List<Integer> midPeakHours;

        @Schema(description = "비피크 시간대 리스트", example = "[0, 1, 2, 3, 4, 5, 6, 7, 20, 21, 22, 23]")
        private List<Integer> offPeakHours;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateEnergyRate {
        @Schema(description = "산업용 전력 요금", example = "0.7823")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal industrialRate;

        @Schema(description = "상업용 전력 요금", example = "0.6319")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal commercialRate;

        @Schema(description = "피크 시간대 요금 증감율", example = "1.5")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal peakMultiplier;

        @Schema(description = "경피크 시간대 요금 증감율", example = "1.2")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal midPeakMultiplier;

        @Schema(description = "비피크 시간대 요금 증감율", example = "0.8")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal offPeakMultiplier;

        @Schema(description = "피크 시간대 리스트", example = "[10, 11, 17, 18, 19]")
        private List<Integer> peakHours;

        @Schema(description = "경피크 시간대 리스트", example = "[8, 9, 12, 13, 14, 15, 16]")
        private List<Integer> midPeakHours;

        @Schema(description = "비피크 시간대 리스트", example = "[0, 1, 2, 3, 4, 5, 6, 7, 20, 21, 22, 23]")
        private List<Integer> offPeakHours;
    }

    /**
     * 에너지 요금 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadEnergyRateRequest {
        /**
         * 에너지 요금 ID
         * - 예: 1
         */
        @Positive
        private List<Long> energyRateId;
        /**
         * 국가 ID
         * - 예: 1
         */
        @Positive
        private List<Long> countryId;
        /**
         * 최소 산업용 전력 요금
         * - 예: 0.5 (kWh당 $0.5)
         */
        @PositiveOrZero
        private BigDecimal minimumIndustrialRate;
        /**
         * 최대 산업용 전력 요금
         * - 예: 1.5 (kWh당 $1.5)
         */
        @PositiveOrZero
        private BigDecimal maximumIndustrialRate;
        /**
         * 최소 상업용 전력 요금
         * - 예: 0.5 (kWh당 $0.5)
         */
        @PositiveOrZero
        private BigDecimal minimumCommercialRate;
        /**
         * 최대 상업용 전력 요금
         * - 예: 1.5 (kWh당 $1.5)
         */
        @PositiveOrZero
        private BigDecimal maximumCommercialRate;
        /**
         * 최소 피크 시간대 요금 증감율
         * - 예: 1.2
         */
        @PositiveOrZero
        private BigDecimal minimumPeakMultiplier;
        /**
         * 최대 피크 시간대 요금 증감율
         * - 예: 2.0
         */
        @PositiveOrZero
        private BigDecimal maximumPeakMultiplier;
        /**
         * 최소 경피크(중간 피크) 시간대 요금 증감율
         * - 예: 1.0
         */
        @PositiveOrZero
        private BigDecimal minimumMidPeakMultiplier;
        /**
         * 최대 경피크(중간 피크) 시간대 요금 증감율
         * - 예: 1.5
         */
        @PositiveOrZero
        private BigDecimal maximumMidPeakMultiplier;
        /**
         * 최소 비피크(할인) 시간대 요금 증감율
         * - 예: 0.5
         */
        @PositiveOrZero
        private BigDecimal minimumOffPeakMultiplier;
        /**
         * 최대 비피크(할인) 시간대 요금 증감율
         * - 예: 1.0
         */
        @PositiveOrZero
        private BigDecimal maximumOffPeakMultiplier;
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

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEnergyRateResponse {
        private Long energyRateId;
        private Long countryId;
        private String countryName;
        private BigDecimal industrialRate;
        private BigDecimal commercialRate;
        private BigDecimal peakMultiplier;
        private BigDecimal midPeakMultiplier;
        private BigDecimal offPeakMultiplier;
        private List<Integer> peakHours;
        private List<Integer> midPeakHours;
        private List<Integer> offPeakHours;

        public ReadEnergyRateResponse(EnergyRate energyRate) {
            this.energyRateId = energyRate.getId();
            this.countryId = energyRate.getCountry().getId();
            this.countryName = energyRate.getCountry().getName();
            this.industrialRate = energyRate.getIndustrialRate();
            this.commercialRate = energyRate.getCommercialRate();
            this.peakMultiplier = energyRate.getPeakMultiplier();
            this.midPeakMultiplier = energyRate.getMidPeakMultiplier();
            this.offPeakMultiplier = energyRate.getOffPeakMultiplier();
            this.peakHours = energyRate.getPeakHours();
            this.midPeakHours = energyRate.getMidPeakHours();
            this.offPeakHours = energyRate.getOffPeakHours();
        }
    }

    /**
     * 에너지 요금 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEnergyRatePageResponse {
        /**
         * 에너지 요금 목록
         */
        private List<EnergyRateDto.ReadEnergyRateResponse> energyRateList;
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
     * 시간대별 요금과 상태를 표현하는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HourlyRateDetail {
        /**
         * 해당 시간대의 요금
         * - 예: 0.05083
         */
        private BigDecimal rate;

        /**
         * 해당 시간대의 상태 (피크, 경피크, 비피크)
         * - 예: "OFF_PEAK"
         */
        private EnergyRatePeakType status;
    }

    /**
     * 특정 업체의 시간대별 요금을 표현하는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HourlyRate {
        /**
         * 업체의 ID
         * - 예: 1
         */
        private Long companyId;

        /**
         * 시간대별 요금과 상태를 담은 맵 (0시부터 23시까지)
         * - 예: {0: {"rate": 0.05083, "status": "OFF_PEAK"}, 1: {...}, ...}
         */
        private Map<Integer, HourlyRateDetail> hourlyRates;
    }

    /**
     * 여러 업체의 시간대별 요금을 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HourlyRatesResponse {
        /**
         * 여러 업체의 시간대별 요금을 담은 리스트
         * - 예: [{"companyId": 1, "hourlyRates": {...}}, {"companyId": 2, "hourlyRates": {...}}]
         */
        private List<HourlyRate> companyHourlyRates;
    }
}