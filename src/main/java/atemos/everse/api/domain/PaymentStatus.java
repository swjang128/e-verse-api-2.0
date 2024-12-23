package atemos.everse.api.domain;

/**
 * 결제 상태를 정의하는 열거형입니다.
 */
public enum PaymentStatus {
    /**
     * 결제가 완료된 상태입니다.
     */
    COMPLETE,
    /**
     * 결제가 미완료된 상태입니다.
     */
    OUTSTANDING
}