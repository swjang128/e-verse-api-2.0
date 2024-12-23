package atemos.everse.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 에너지 관련 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class EnergyDto {
    /**
     * 시간별 에너지 사용량과 요금 등의 데이터들을 담습니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HourlyResponse {
        // 기준 시각
        private LocalDateTime referenceTime;
        // 실제 에너지 사용량
        private BigDecimal usage;
        // AI 예측 에너지 사용량
        private BigDecimal forecastUsage;
        // 실제와 AI 예측 에너지 사용량의 차이
        private BigDecimal actualAndForecastUsageDifference;
        // 실제 요금
        private BigDecimal bill;
        // AI 예측 요금
        private BigDecimal forecastBill;
        // 실제와 AI 예측 요금의 차이
        private BigDecimal actualAndForecastBillDifference;
        // 실제와 AI 예측의 편차율
        private BigDecimal deviationRate;
        // 실제와 AI 예측의 정확도
        private BigDecimal forecastAccuracy;
    }

    /**
     * 일별 에너지 사용량과 요금 등의 데이터들을 담습니다.
     * 시간별 데이터를 key로 사용하여 Map으로 관리합니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailyResponse {
        // 기준 일
        private LocalDate referenceDate;
        // 일일 실제 에너지 사용량
        private BigDecimal dailyUsage;
        // 일일 AI 예측 에너지 사용량
        private BigDecimal dailyForecastUsage;
        // 일일 실제와 AI 예측 에너지 사용량의 차이
        private BigDecimal dailyActualAndForecastUsageDifference;
        // 일일 실제 요금
        private BigDecimal dailyBill;
        // 일일 AI 예측 요금
        private BigDecimal dailyForecastBill;
        // 일일 실제와 AI 예측 요금의 차이
        private BigDecimal dailyActualAndForecastBillDifference;
        // 일일 실제와 AI 예측의 편차율
        private BigDecimal dailyDeviationRate;
        // 일일 실제와 AI 예측의 정확도
        private BigDecimal dailyForecastAccuracy;
        // 시간별 데이터 리스트
        private List<HourlyResponse> hourlyResponse;
    }

    /**
     * 월별 에너지 사용량과 요금 등의 데이터들을 담습니다.
     * 일별 데이터를 key로 사용하여 Map으로 관리합니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MonthlyResponse {
        // 기준 월
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
        private LocalDate referenceMonth;
        // 월별 실제 에너지 사용량
        private BigDecimal monthlyUsage;
        // 월별 AI 예측 에너지 사용량
        private BigDecimal monthlyForecastUsage;
        // 월별 실제와 AI 예측 에너지 사용량의 차이
        private BigDecimal monthlyActualAndForecastUsageDifference;
        // 월별 실제 요금
        private BigDecimal monthlyBill;
        // 월별 AI 예측 요금
        private BigDecimal monthlyForecastBill;
        // 월별 실제와 AI 예측 요금의 차이
        private BigDecimal monthlyActualAndForecastBillDifference;
        // 월별 실제와 AI 예측의 편차율
        private BigDecimal monthlyDeviationRate;
        // 월별 실제와 AI 예측의 정확도
        private BigDecimal monthlyForecastAccuracy;
        // 일별 데이터 Map (키는 날짜 값)
        private Map<String, DailyResponse> dailyResponse;
    }

    /**
     * 전체 에너지 사용량과 요금 등의 데이터들을 담습니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SummaryResponse {
        // 전체 실제 에너지 사용량
        private BigDecimal summaryUsage;
        // 전체 AI 예측 사용량
        private BigDecimal summaryForecastUsage;
        // 전체 실제와 AI 예측 에너지 사용량의 차이
        private BigDecimal summaryUsageForecastDifference;
        // 전체 실제 요금
        private BigDecimal summaryBill;
        // 전체 AI 예측 요금
        private BigDecimal summaryForecastBill;
        // 전체 실제와 AI 예측 요금의 차이
        private BigDecimal summaryBillForecastDifference;
        // 전체 실제와 AI 예측의 편차율
        private BigDecimal summaryDeviationRate;
        // 전체 실제와 AI 예측의 정확도
        private BigDecimal summaryForecastAccuracy;
        // 월별 데이터 Map (키는 월 값)
        private Map<String, MonthlyResponse> monthlyResponse;
    }

    /**
     * 실시간 및 전월 에너지 사용량과 요금 데이터를 담습니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RealTimeAndLastMonthResponse {
        private SummaryResponse realTimeData;
        private SummaryResponse lastMonthData;
    }

    /**
     * 이번 달 및 저번 달의 에너지 사용량과 요금을 담는 응답 객체입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ThisAndLastMonthResponse {
        private SummaryResponse thisMonthData;
        private SummaryResponse lastMonthData;
    }
}