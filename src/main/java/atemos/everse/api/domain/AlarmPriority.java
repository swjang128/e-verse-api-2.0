package atemos.everse.api.domain;

/**
 * 알람 우선순위를 나타내는 열거형입니다.
 */
public enum AlarmPriority {
    /**
     * 높은 우선순위를 나타내는 알람입니다.
     * 즉시 대응이 필요한 중요하고 긴급한 알람입니다.
     */
    HIGH,
    /**
     * 중간 우선순위를 나타내는 알람입니다.
     * 중요한 문제지만 높은 우선순위보다는 덜 긴급한 알람입니다.
     */
    MEDIUM,
    /**
     * 낮은 우선순위를 나타내는 알람입니다.
     * 긴급하지 않은 문제로, 나중에 처리해도 되는 알람입니다.
     */
    LOW
}