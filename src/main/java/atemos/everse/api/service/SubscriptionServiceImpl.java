package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.SubscriptionDto;
import atemos.everse.api.entity.Payment;
import atemos.everse.api.entity.Subscription;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.PaymentRepository;
import atemos.everse.api.repository.SubscriptionRepository;
import atemos.everse.api.specification.SubscriptionSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SubscriptionServiceImpl는 SubscriptionService 인터페이스를 구현하며,
 * 구독 정보의 생성, 조회, 수정, 삭제 등의 기능을 제공합니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 구독 정보를 등록합니다.
     *
     * @param createSubscriptionDto 구독 정보를 등록하기 위한 데이터 전송 객체입니다.
     * @return 등록된 구독 정보 응답 객체
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public SubscriptionDto.ReadSubscriptionResponse create(SubscriptionDto.CreateSubscription createSubscriptionDto) {
        // 이미 해당 서비스를 구독하고 있다면 예외 처리
        var exists = subscriptionRepository.existsByCompanyIdAndServiceAndDateRangeOverlap(
                createSubscriptionDto.getCompanyId(),
                createSubscriptionDto.getService(),
                createSubscriptionDto.getStartDate(),
                createSubscriptionDto.getEndDate());
        if (exists) throw new IllegalStateException("The company is already subscribed to this service during the specified period.");
        // 업체가 존재하는지 확인
        var company = companyRepository.findById(createSubscriptionDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 등록하려는 구독 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(company.getId());
        // 등록할 Subscription 정보를 엔티티로 Build
        var subscription = Subscription.builder()
                .company(company)
                .service(createSubscriptionDto.getService())
                .startDate(createSubscriptionDto.getStartDate())
                .endDate(createSubscriptionDto.getEndDate())
                .build();
        // Subscription 저장
        subscriptionRepository.save(subscription);
        // 연관된 Payment 정보 업데이트
        updatePaymentSubscriptionServices(subscription, false);
        // 저장된 Subscription 정보를 반환
        return new SubscriptionDto.ReadSubscriptionResponse(subscription, company.getCountry().getZoneId());
    }

    /**
     * 조건에 맞는 구독 정보 정보를 조회합니다.
     *
     * @param readSubscriptionRequestDto 구독 정보 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 구독 정보 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public SubscriptionDto.ReadSubscriptionPageResponse read(SubscriptionDto.ReadSubscriptionRequest readSubscriptionRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 Subscription 목록 조회
        var subscriptionPage = subscriptionRepository.findAll(SubscriptionSpecification.findWith(readSubscriptionRequestDto, zoneId), pageable);
        // 응답 객체 반환
        return SubscriptionDto.ReadSubscriptionPageResponse.builder()
                .subscriptionList(subscriptionPage.getContent().stream()
                        .map(subscription -> new SubscriptionDto.ReadSubscriptionResponse(subscription, zoneId))
                        .collect(Collectors.toList()))
                .totalElements(subscriptionPage.getTotalElements())
                .totalPages(subscriptionPage.getTotalPages())
                .build();
    }

    /**
     * 특정 구독 정보를 수정합니다.
     * - 사용자가 구독을 취소하는 이벤트: 해당 업체가 기존에 구독하던 정보인지 확인하여 이미 구독 중인 정보라면 endDate를 현재 날짜로 수정합니다.
     *
     * @param subscriptionId 수정할 구독 정보의 ID입니다.
     * @param updateSubscriptionDto 구독 정보 수정 정보를 포함하는 데이터 전송 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public SubscriptionDto.ReadSubscriptionResponse update(Long subscriptionId, SubscriptionDto.UpdateSubscription updateSubscriptionDto) {
        // Subscription 조회
        var subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("No such subscription."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 구독 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(subscription.getCompany().getId());
        Optional.ofNullable(updateSubscriptionDto.getCompanyId())
                .ifPresent(companyId -> {
                    var company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new EntityNotFoundException("No such company."));
                    subscription.setCompany(company);
                });
        // 서비스 정보 업데이트
        Optional.ofNullable(updateSubscriptionDto.getService()).ifPresent(subscription::setService);
        // 시작 날짜 업데이트
        Optional.ofNullable(updateSubscriptionDto.getStartDate()).ifPresent(subscription::setStartDate);
        // 종료 날짜 업데이트
        Optional.ofNullable(updateSubscriptionDto.getEndDate()).ifPresent(subscription::setEndDate);
        // Subscription 정보 Update
        var updatedSubscription = subscriptionRepository.save(subscription);
        // 관련된 Payment 엔티티 업데이트
        updatePaymentSubscriptionServices(updatedSubscription, false);
        return new SubscriptionDto.ReadSubscriptionResponse(updatedSubscription, updatedSubscription.getCompany().getCountry().getZoneId());
    }

    /**
     * 특정 구독 ID에 대한 구독 정보를 취소합니다.
     *
     * @param subscriptionId 취소할 구독 ID
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        // Subscription 조회
        var subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("No such subscription."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 구독 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(subscription.getCompany().getId());
        // 이미 취소된 구독 정보는 예외 발생
        if (subscription.getEndDate() != null) {
            throw new IllegalArgumentException("This subscription is already cancelled.");
        }
        // 구독 취소 처리(endDate는 해당 업체의 타임존에 맞춰서 넣기)
        ZoneId companyZoneId = subscription.getCompany().getCountry().getZoneId();
        subscription.setEndDate(LocalDate.now(companyZoneId));
        // 업데이트된 Subscription 저장
        subscriptionRepository.save(subscription);
        // 관련된 Payment 엔티티 업데이트 (필요하다면 구현)
        updatePaymentSubscriptionServices(subscription, true);
    }

    /**
     * 특정 ID에 해당하는 구독 정보를 삭제합니다.
     *
     * @param subscriptionId 삭제할 구독 정보의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long subscriptionId) {
        // Subscription 조회
        var subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("No such subscription."));
        // 관련된 Payment 엔티티 업데이트
        updatePaymentSubscriptionServices(subscription, true);
        // 구독 정보 삭제
        subscriptionRepository.delete(subscription);
    }

    /**
     * Payment의 SubscriptionServiceList를 업데이트합니다.
     *
     * @param subscription 생성되거나 수정된 Subscription 엔티티
     * @param isDelete 플래그, 구독이 삭제된 경우 true로 설정
     */
    @Override
    public void updatePaymentSubscriptionServices(Subscription subscription, boolean isDelete) {
        // 해당 회사와 관련된 모든 Payment를 조회
        var payments = paymentRepository.findAllByCompanyId(subscription.getCompany().getId());
        for (Payment payment : payments) {
            // Payment의 SubscriptionServiceList를 재생성
            var currentSubscriptions = subscriptionRepository.findAllByCompanyIdsAndDate(
                    List.of(payment.getCompany().getId()), payment.getUsageDate());
            // 수정 가능한 리스트로 변환
            var subscriptionServiceList = currentSubscriptions.stream()
                    .map(Subscription::getService)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (isDelete) {
                // 삭제된 구독의 서비스를 제거
                subscriptionServiceList.remove(subscription.getService());
            }
            // 업데이트된 SubscriptionServiceList를 설정
            payment.setSubscriptionServiceList(subscriptionServiceList);
            // Payment의 금액을 재계산
            var newAmount = paymentService.calculateAmount(payment.getMeteredUsage(), subscriptionServiceList, payment.getStorageUsage());
            payment.setAmount(newAmount);
            // Payment 엔티티를 저장
            paymentRepository.save(payment);
        }
    }
}