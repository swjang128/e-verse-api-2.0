package atemos.everse.api.service;

import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.PaymentDto;
import atemos.everse.api.entity.MeteredUsage;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * PaymentService는 결제 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 결제 관련 작업을 처리하는 메소드를 정의합니다.
 */
public interface PaymentService {
    /**
     * 특정 조건에 해당하는 결제 정보를 조회합니다.
     *
     * @param readPaymentRequestDto 서비스 사용 정보 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 서비스 사용 정보 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    PaymentDto.ReadPaymentPageResponse read(PaymentDto.ReadPaymentRequest readPaymentRequestDto, Pageable pageable);
    /**
     * 결제 정보를 수정합니다.
     *
     * @param paymentId 결제의 ID입니다.
     * @param updatePaymentDto 결제 정보를 업데이트하기 위한 데이터 전송 객체입니다.
     * @return 수정된 결제 정보
     */
    PaymentDto.ReadPaymentResponse update(Long paymentId, PaymentDto.UpdatePayment updatePaymentDto);
    /**
     * 특정 ID에 해당하는 결제를 삭제합니다.
     *
     * @param paymentId 삭제할 결제의 ID입니다.
     */
    void delete(Long paymentId);
    /**
     * Payment 엔티티에 들어갈 금액(amount) 계산
     * @param meteredUsage 서비스 사용량
     * @param subscriptionServiceLists 구독한 서비스 목록
     * @param storageUsage 데이터베이스 스토리지 사용량
     * @return Payment 엔티티에 들어갈 금액(amount)
     */
    BigDecimal calculateAmount(MeteredUsage meteredUsage, List<SubscriptionServiceList> subscriptionServiceLists, Long storageUsage);
}