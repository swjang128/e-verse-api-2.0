package atemos.everse.api.domain;

/**
 * 알람 유형을 나타내는 열거형입니다.
 */
public enum AlarmType {
    /**
     * 최대 에너지 사용량 알람입니다.
     * 설정된 최대 에너지 사용량을 초과할 경우 발생하는 알람입니다.
     */
    MAXIMUM_ENERGY_USAGE,
    /**
     * 최소 에너지 사용량 알람입니다.
     * 설정된 최소 에너지 사용량을 하회할 경우 발생하는 알람입니다.
     */
    MINIMUM_ENERGY_USAGE,
    /**
     * 피크 또는 경피크 시간대 AI 예측 에너지 사용량 초과 알람입니다.
     * 피크 또는 경피크 시간대에 AI 예측 에너지 사용량을 초과할 경우 발생하는 알람입니다.
     */
    AI_PREDICTION_BILL_EXCEEDED
}