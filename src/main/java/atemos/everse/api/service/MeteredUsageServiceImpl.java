package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.MeteredUsageDto;
import atemos.everse.api.dto.StorageDto;
import atemos.everse.api.entity.MeteredUsage;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.MeteredUsageRepository;
import atemos.everse.api.repository.PaymentRepository;
import atemos.everse.api.specification.MeteredUsageSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MeteredUsageServiceImpl 클래스는 서비스 사용 내역의 관리 기능을 제공하는 서비스 구현 클래스입니다.
 * 이 클래스는 사용 내역 조회, 수정, 삭제 기능을 포함하고 있습니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class MeteredUsageServiceImpl implements MeteredUsageService {
    private final MeteredUsageRepository meteredUsageRepository;
    private final CompanyRepository companyRepository;
    private final StorageService storageService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * 조건에 맞는 서비스 사용 내역 정보를 조회합니다.
     *
     * @param readMeteredUsageRequestDto 서비스 사용 내역 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 서비스 사용 내역 목록과 관련된 추가 정보를 포함하는 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public MeteredUsageDto.ReadMeteredUsagePageResponse read(MeteredUsageDto.ReadMeteredUsageRequest readMeteredUsageRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 MeteredUsage 목록 조회
        var meteredUsagePage = meteredUsageRepository.findAll(
                MeteredUsageSpecification.findWith(readMeteredUsageRequestDto, zoneId), pageable);
        // readMeteredUsageRequestDto의 companyId 값이 있다면 해당 업체의 스토리지 사용량 조회
        StorageDto.StorageResponse storageResponse = null;
        if (readMeteredUsageRequestDto.getCompanyId() != null) {
            storageResponse = storageService.getDataUsageByCompanyId(readMeteredUsageRequestDto.getCompanyId());
        }
        // 응답 객체 생성 및 반환
        return MeteredUsageDto.ReadMeteredUsagePageResponse.builder()
                .meteredUsageList(meteredUsagePage.getContent().stream()
                        .map(meteredUsage -> new MeteredUsageDto.ReadMeteredUsageResponse(meteredUsage, zoneId))
                        .toList())
                .storageUsage(storageResponse != null ? storageResponse.getTotalStorageUsage() : null)
                .totalElements(meteredUsagePage.getTotalElements())
                .totalPages(meteredUsagePage.getTotalPages())
                .build();
    }

    /**
     * 특정 서비스 사용 내역 정보를 수정합니다.
     *
     * @param meteredUsageId 수정할 서비스 사용 내역의 ID입니다.
     * @param updateMeteredUsageDto 서비스 사용 내역 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 서비스 사용 내역 정보가 담긴 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public MeteredUsageDto.ReadMeteredUsageResponse update(Long meteredUsageId, MeteredUsageDto.UpdateMeteredUsage updateMeteredUsageDto) {
        // 서비스 사용 내역 조회
        var meteredUsage = meteredUsageRepository.findById(meteredUsageId)
                .orElseThrow(() -> new EntityNotFoundException("No such meteredUsage."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 서비스 사용 내역 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(meteredUsage.getCompany().getId());
        // 업체 정보 조회
        var company = companyRepository.findById(updateMeteredUsageDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 서비스 사용 내역 정보 수정
        meteredUsage.setCompany(company);
        meteredUsage.setUsageDate(Optional.ofNullable(updateMeteredUsageDto.getUsageDate())
                .orElse(meteredUsage.getUsageDate()));
        meteredUsage.setApiCallCount(Optional.ofNullable(updateMeteredUsageDto.getApiCallCount())
                .orElse(meteredUsage.getApiCallCount()));
        meteredUsage.setIotInstallationCount(Optional.ofNullable(updateMeteredUsageDto.getIotInstallationCount())
                .orElse(meteredUsage.getIotInstallationCount()));
        // 수정된 서비스 사용 내역 저장
        var updatedMeteredUsage = meteredUsageRepository.save(meteredUsage);
        // 관련된 결제 정보 업데이트
        updateRelatedPayments(meteredUsageId, updatedMeteredUsage);
        return new MeteredUsageDto.ReadMeteredUsageResponse(updatedMeteredUsage, company.getCountry().getZoneId());
    }

    /**
     * 특정 ID에 해당하는 서비스 사용 내역을 삭제합니다.
     *
     * @param meteredUsageId 삭제할 서비스 사용 내역의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long meteredUsageId) {
        // 서비스 사용 내역 조회 및 삭제
        var meteredUsage = meteredUsageRepository.findById(meteredUsageId)
                .orElseThrow(() -> new EntityNotFoundException("No such meteredUsage."));
        meteredUsageRepository.delete(meteredUsage);
    }

    /**
     * 관련된 결제 정보를 업데이트합니다.
     *
     * @param meteredUsageId 서비스 사용 내역 ID입니다.
     * @param updatedMeteredUsage 업데이트된 서비스 사용 내역 객체입니다.
     */
    private void updateRelatedPayments(Long meteredUsageId, MeteredUsage updatedMeteredUsage) {
        // 관련된 Payment 엔티티 조회 및 업데이트
        var payments = paymentRepository.findAllByMeteredUsageId(meteredUsageId);
        for (var payment : payments) {
            var newAmount = paymentService.calculateAmount(updatedMeteredUsage, payment.getSubscriptionServiceList(), payment.getStorageUsage());
            payment.setAmount(newAmount);
            paymentRepository.save(payment);
        }
    }
}