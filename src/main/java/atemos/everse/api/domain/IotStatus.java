package atemos.everse.api.domain;

/**
 * IoT 장비의 상태를 나타내는 열거형입니다.
 */
public enum IotStatus {
    /**
     * 정상 상태입니다. IoT 장비가 정상적으로 작동하고 있음을 나타냅니다.
     */
    NORMAL,
    /**
     * 오류 상태입니다. IoT 장비에서 문제가 발생하여 작동하지 않음을 나타냅니다.
     */
    ERROR
}