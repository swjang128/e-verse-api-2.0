package atemos.everse.api.domain;

/**
 * 결제 방법을 정의하는 열거형입니다.
 */
public enum PaymentMethod {
    /**
     * 은행 이체를 통한 결제 방법입니다.
     */
    TRANSFER,
    /**
     * 신용카드 또는 체크카드를 통한 결제 방법입니다.
     */
    CARD
}