package atemos.everse.api.service;

import atemos.everse.api.dto.SubscriptionDto;
import atemos.everse.api.entity.Subscription;
import org.springframework.data.domain.Pageable;

/**
 * SubscriptionService는 구독 정보 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 구독 정보와 관련된 주요 CRUD 작업을 처리하는 메소드를 정의합니다.
 */
public interface SubscriptionService {
    /**
     * 새로운 구독 정보를 등록합니다.
     *
     * @param createSubscriptionDto 구독 정보를 등록하기 위한 데이터 전송 객체입니다.
     */
    SubscriptionDto.ReadSubscriptionResponse create(SubscriptionDto.CreateSubscription createSubscriptionDto);
    /**
     * 조건에 맞는 구독 정보를 조회합니다.
     *
     * @param readSubscriptionRequestDto 구독 정보 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 구독 정보 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    SubscriptionDto.ReadSubscriptionPageResponse read(SubscriptionDto.ReadSubscriptionRequest readSubscriptionRequestDto, Pageable pageable);
    /**
     * 특정 구독 정보를 수정합니다.
     *
     * @param id 수정할 구독 정보의 ID입니다.
     * @param updateSubscriptionDto 구독 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 구독 정보
     */
    SubscriptionDto.ReadSubscriptionResponse update(Long id, SubscriptionDto.UpdateSubscription updateSubscriptionDto);
    /**
     * 특정 구독 ID에 대한 구독 정보를 취소합니다.
     * @param id 취소할 구독 ID
     */
    void cancelSubscription(Long id);
    /**
     * 특정 ID에 해당하는 구독 정보를 삭제합니다.
     *
     * @param id 삭제할 구독 정보의 ID입니다.
     */
    void delete(Long id);
    /**
     * Payment의 SubscriptionServiceList를 업데이트합니다.
     *
     * @param subscription 생성되거나 수정된 Subscription 엔티티
     * @param isDelete 플래그, 구독이 삭제된 경우 true로 설정
     */
    void updatePaymentSubscriptionServices(Subscription subscription, boolean isDelete);
}