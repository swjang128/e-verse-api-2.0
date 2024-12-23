package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.PaymentDto;
import atemos.everse.api.entity.MeteredUsage;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.MeteredUsageRepository;
import atemos.everse.api.repository.PaymentRepository;
import atemos.everse.api.specification.PaymentSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 결제 정보를 관리하는 서비스 클래스입니다.
 * 결제 정보 조회, 수정, 삭제 및 요금 계산을 담당합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    @Value("${payment.api-call-rate}")
    private BigDecimal apiCallRate; // API Call 1회 당 단가
    @Value("${payment.iot-installation-rate}")
    private BigDecimal iotInstallationRate; // 설치된 IoT 개수 당 단가
    @Value("${payment.storage-usage-rate-per-1gb}")
    private BigDecimal storageUsageRatePer1GB;  // 데이터베이스 스토리지 사용량에 대한 단가
    @Value("${payment.free-storage-limit-gb}")
    private Long freeStorageLimitGB;    // 무료로 제공되는 스토리지 용량(GB)

    private final PaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;
    private final MeteredUsageRepository meteredUsageRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * 특정 조건에 해당하는 결제 정보를 조회합니다.
     *
     * @param readPaymentRequestDto 서비스 사용 정보 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable              페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 서비스 사용 정보 목록과 관련된 추가 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PaymentDto.ReadPaymentPageResponse read(PaymentDto.ReadPaymentRequest readPaymentRequestDto, Pageable pageable) {
        // 사용자의 타임존 설정
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 결제 정보를 페이징 처리하여 조회
        var paymentPage = paymentRepository.findAll(
                PaymentSpecification.findWith(readPaymentRequestDto, zoneId), pageable);
        // 모든 결제 내역을 조회하여 리스트로 가져옴
        var paymentList = paymentRepository.findAll(PaymentSpecification.findWith(readPaymentRequestDto, zoneId));
        // 종료 날짜 설정, 파라미터가 없을 경우 오늘 날짜를 사용
        var usageDateEnd = Optional.ofNullable(readPaymentRequestDto.getUsageDateEnd()).orElse(LocalDate.now());
        // 요금 관련 변수 초기화
        var summaryApiCallCount = 0;
        var recentlyIotInstallationCount = 0;
        var recentlyStorageUsage = new HashMap<Long, Long>();
        var subscribedCount = new HashMap<Long, Map<SubscriptionServiceList, Long>>();
        var summarySubscriptionAmount = BigDecimal.ZERO;
        var summaryApiCallAmount = BigDecimal.ZERO;
        var summaryIotInstallationAmount = BigDecimal.ZERO;
        var summaryStorageUsageAmount = BigDecimal.ZERO;
        // 결제 내역을 순회하면서 요금 관련 계산 수행
        for (var payment : paymentList) {
            // API 호출 수와 요금 계산
            if (payment.getMeteredUsage() != null) {
                summaryApiCallCount += payment.getMeteredUsage().getApiCallCount();
                summaryApiCallAmount = summaryApiCallAmount.add(calculateApiCallAmount(payment.getMeteredUsage()));
            }
            // IoT 설치 개수와 요금 계산(가장 최근 값만 사용)
            if (payment.getUsageDate().isBefore(usageDateEnd.plusDays(1))) {
                if (payment.getMeteredUsage() != null && payment.getMeteredUsage().getIotInstallationCount() > recentlyIotInstallationCount) {
                    // IoT 설치 개수의 최근 값을 유지
                    recentlyIotInstallationCount = payment.getMeteredUsage().getIotInstallationCount();
                    // 최근 IoT 설치 개수에 대한 요금을 계산(가장 최근 값만 사용)
                    summaryIotInstallationAmount = calculateIotInstallationAmount(payment.getMeteredUsage());
                }
            }
            // 스토리지 사용량과 요금 계산(가장 최근 값만 사용)
            recentlyStorageUsage.merge(payment.getCompany().getId(),
                    payment.getUsageDate().isBefore(usageDateEnd.plusDays(1)) ? payment.getStorageUsage() : 0L,
                    Math::max);
            summaryStorageUsageAmount = summaryStorageUsageAmount.add(calculateStorageUsageAmount(payment.getStorageUsage()));
            // 구독 서비스 리스트의 개수와 요금 계산
            if (payment.getSubscriptionServiceList() != null && !payment.getSubscriptionServiceList().isEmpty()) {
                var companySubscribedCount = subscribedCount.computeIfAbsent(payment.getCompany().getId(), k -> new HashMap<>());
                for (var service : payment.getSubscriptionServiceList()) {
                    companySubscribedCount.merge(service, 1L, Long::sum);
                    summarySubscriptionAmount = summarySubscriptionAmount.add(service.getRate());
                }
            }
        }
        // Payment 정보가 담긴 응답 DTO 생성 및 반환
        return PaymentDto.ReadPaymentPageResponse.builder()
                .paymentList(paymentPage.getContent().stream()
                        .map(payment -> new PaymentDto.ReadPaymentResponse(payment, zoneId))
                        .collect(Collectors.toList()))
                .summaryApiCallCount(summaryApiCallCount)
                .recentlyIotInstallationCount(recentlyIotInstallationCount)
                .recentlyStorageUsage(recentlyStorageUsage)
                .subscribedCount(subscribedCount)
                .summarySubscriptionAmount(summarySubscriptionAmount)
                .summaryApiCallAmount(summaryApiCallAmount)
                .summaryIotInstallationAmount(summaryIotInstallationAmount)
                .summaryStorageUsageAmount(summaryStorageUsageAmount)
                .summaryAmount(summarySubscriptionAmount.add(summaryApiCallAmount).add(summaryIotInstallationAmount).add(summaryStorageUsageAmount))
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .build();
    }

    /**
     * 결제 정보를 수정합니다.
     *
     * @param paymentId          결제 ID
     * @param updatePaymentDto   업데이트할 결제 정보
     * @return 수정된 결제 정보가 담긴 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public PaymentDto.ReadPaymentResponse update(Long paymentId, PaymentDto.UpdatePayment updatePaymentDto) {
        // Payment 조회
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("No such payment."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 결제 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(payment.getCompany().getId());
        // 업체 정보 업데이트
        Optional.ofNullable(updatePaymentDto.getCompanyId()).ifPresent(companyId -> {
            var company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new EntityNotFoundException("No such company."));
            payment.setCompany(company);
        });
        // 서비스 사용 내역 업데이트
        Optional.ofNullable(updatePaymentDto.getMeteredUsageId()).ifPresent(meteredUsageId -> {
            var meteredUsage = meteredUsageRepository.findById(meteredUsageId)
                    .orElseThrow(() -> new EntityNotFoundException("No such meteredUsage."));
            payment.setMeteredUsage(meteredUsage);
        });
        // Payment 정보 업데이트
        Optional.ofNullable(updatePaymentDto.getSubscriptionServiceList()).ifPresent(payment::setSubscriptionServiceList);
        Optional.ofNullable(updatePaymentDto.getMethod()).ifPresent(payment::setMethod);
        Optional.ofNullable(updatePaymentDto.getAmount()).ifPresent(payment::setAmount);
        Optional.ofNullable(updatePaymentDto.getStatus()).ifPresent(payment::setStatus);
        Optional.ofNullable(updatePaymentDto.getUsageDate()).ifPresent(payment::setUsageDate);
        Optional.ofNullable(updatePaymentDto.getScheduledPaymentDate()).ifPresent(payment::setScheduledPaymentDate);
        // 업데이트된 Payment 엔티티 저장 후 DTO 반환
        return new PaymentDto.ReadPaymentResponse(paymentRepository.save(payment), payment.getCompany().getCountry().getZoneId());
    }

    /**
     * 결제 정보를 삭제합니다.
     *
     * @param paymentId 삭제할 결제 ID
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long paymentId) {
        // 결제 정보 삭제
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("No such payment."));
        paymentRepository.delete(payment);
    }

    /**
     * Payment 엔티티에 들어갈 금액(amount)을 계산합니다.
     * @param meteredUsage          서비스 사용량
     * @param subscriptionServiceLists 구독한 서비스 목록
     * @param storageUsage          데이터베이스 스토리지 사용량
     * @return Payment 엔티티에 들어갈 금액(amount)
     */
    @Override
    public BigDecimal calculateAmount(MeteredUsage meteredUsage, List<SubscriptionServiceList> subscriptionServiceLists, Long storageUsage) {
        BigDecimal apiCallAmount = calculateApiCallAmount(meteredUsage);
        BigDecimal subscriptionAmount = calculateSubscriptionAmount(subscriptionServiceLists);
        BigDecimal storageUsageAmount = calculateStorageUsageAmount(storageUsage);
        BigDecimal iotInstallationAmount = calculateIotInstallationAmount(meteredUsage);
        // 각 항목의 요금을 합산하여 최종 금액을 반환
        return apiCallAmount.add(subscriptionAmount)
                .add(storageUsageAmount)
                .add(iotInstallationAmount);
    }

    /**
     * API 호출 요금을 계산합니다.
     *
     * @param meteredUsage 서비스 사용량 정보
     * @return 계산된 API 호출 요금
     */
    private BigDecimal calculateApiCallAmount(MeteredUsage meteredUsage) {
        if (meteredUsage != null) {
            return BigDecimal.valueOf(meteredUsage.getApiCallCount()).multiply(apiCallRate);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 구독 서비스 요금을 계산합니다.
     *
     * @param subscriptionServiceLists 구독한 서비스 목록
     * @return 계산된 구독 서비스 요금
     */
    private BigDecimal calculateSubscriptionAmount(List<SubscriptionServiceList> subscriptionServiceLists) {
        BigDecimal subscriptionAmount = BigDecimal.ZERO;
        if (subscriptionServiceLists != null) {
            for (SubscriptionServiceList service : subscriptionServiceLists) {
                subscriptionAmount = subscriptionAmount.add(service.getRate());
            }
        }
        return subscriptionAmount;
    }

    /**
     * 스토리지 사용 요금을 계산합니다.
     *
     * @param storageUsage 스토리지 사용량
     * @return 계산된 스토리지 사용 요금
     */
    private BigDecimal calculateStorageUsageAmount(Long storageUsage) {
        long freeStorageLimit = freeStorageLimitGB * 1024 * 1024 * 1024; // 20GB를 바이트로 변환
        if (storageUsage > freeStorageLimit) {
            BigDecimal excessStorageInGB = BigDecimal.valueOf(storageUsage - freeStorageLimit)
                    .divide(BigDecimal.valueOf(1024L * 1024 * 1024), RoundingMode.HALF_UP);
            return excessStorageInGB.multiply(storageUsageRatePer1GB);
        }
        return BigDecimal.ZERO;
    }

    /**
     * IoT 설치 요금을 계산합니다.
     *
     * @param meteredUsage 서비스 사용량 정보
     * @return 계산된 IoT 설치 요금
     */
    private BigDecimal calculateIotInstallationAmount(MeteredUsage meteredUsage) {
        if (meteredUsage != null) {
            // IoT 설치 개수에 비례하여 금액을 계산
            return BigDecimal.valueOf(meteredUsage.getIotInstallationCount()).multiply(iotInstallationRate);
        }
        return BigDecimal.ZERO;
    }
}