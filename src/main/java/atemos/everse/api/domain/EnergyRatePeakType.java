package atemos.everse.api.domain;

/**
 * 피크, 경피크, 비피크 상태를 정의하는 열거형입니다.
 */
public enum EnergyRatePeakType {
    /**
     * 피크
     */
    PEAK,
    /**
     * 경피크
     */
    MID_PEAK,
    /**
     * 비피크
     */
    OFF_PEAK,
    /**
     * 알 수 없음
     */
    UNKNOWN
}