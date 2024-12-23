package atemos.everse.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 구독 서비스의 이름을 정의하는 열거형입니다.
 */
@Getter
@AllArgsConstructor
public enum SubscriptionServiceList {
    /**
     * 보고서 다운로드 기능이 포함된 서비스입니다.
     */
    @Schema(description = "Report download available")
    REPORT_DOWNLOAD("Report download available", "보고서 다운로드 기능", BigDecimal.valueOf(0.17)),
    /**
     * 에너지 사용량 AI 예측 분석 기능 서비스입니다.
     */
    @Schema(description = "AI energy usage forecast")
    AI_ENERGY_USAGE_FORECAST("AI energy usage forecast", "에너지 사용량 AI 예측", BigDecimal.valueOf(0.24)),
    /**
     * 대화형 AI 서비스입니다.
     */
    @Schema(description = "Interactive AI service")
    INTERACTIVE_AI("Interactive AI service", "대화형 AI 제공", BigDecimal.valueOf(0.06));

    // 영어로 된 항목 이름
    private final String name;
    // 한글로 된 항목 설명
    private final String description;
    // 해당 서비스의 일당 단가(단위: 달러)
    private final BigDecimal rate;
}