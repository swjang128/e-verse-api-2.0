package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.AnomalyDto;
import atemos.everse.api.entity.Anomaly;
import atemos.everse.api.repository.AnomalyRepository;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.specification.AnomalySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * AnomalyServiceImpl는 이상탐지와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이상탐지 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnomalyServiceImpl implements AnomalyService {
    private final AnomalyRepository anomalyRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * 이상탐지 등록
     *
     * @param createAnomalyDto 이상탐지 등록을 위한 데이터 전송 객체
     * @return 등록된 이상탐지 객체의 읽기 응답
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public AnomalyDto.ReadAnomalyResponse create(AnomalyDto.CreateAnomaly createAnomalyDto) {
        // Company ID로 업체가 존재하는지 확인
        var company = companyRepository.findById(createAnomalyDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 등록하려는 업체 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(company.getId());
        // Anomaly 엔티티 생성 및 저장
        var anomaly = Anomaly.builder()
                .company(company)
                .lowestHourlyEnergyUsage(createAnomalyDto.getLowestHourlyEnergyUsage())
                .highestHourlyEnergyUsage(createAnomalyDto.getHighestHourlyEnergyUsage())
                .available(createAnomalyDto.getAvailable())
                .build();
        return new AnomalyDto.ReadAnomalyResponse(anomalyRepository.save(anomaly), company.getCountry().getZoneId());
    }

    /**
     * 조건에 맞는 이상탐지 조회
     *
     * @param readAnomalyRequestDto 이상탐지 조회 조건을 포함하는 데이터 전송 객체
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 이상탐지 목록과 관련된 추가 정보를 포함하는 맵
     */
    @Override
    @Transactional(readOnly = true)
    public AnomalyDto.ReadAnomalyPageResponse read(AnomalyDto.ReadAnomalyRequest readAnomalyRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 Anomaly 목록 조회 및 DTO 변환
        var anomalyPage = anomalyRepository.findAll(AnomalySpecification.findWith(readAnomalyRequestDto, zoneId), pageable);
        // DTO로 변환할 때 zoneId를 사용
        var anomalyList = anomalyPage.getContent().stream()
                .map(anomaly -> new AnomalyDto.ReadAnomalyResponse(anomaly, zoneId))
                .toList();
        return new AnomalyDto.ReadAnomalyPageResponse(
                anomalyList,
                anomalyPage.getTotalElements(),
                anomalyPage.getTotalPages());
    }

    /**
     * 특정 이상탐지 정보를 수정합니다.
     *
     * @param anomalyId 수정할 이상탐지의 ID입니다.
     * @param updateAnomalyDto 이상탐지 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 이상탐지 정보를 담고 있는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public AnomalyDto.ReadAnomalyResponse update(Long anomalyId, AnomalyDto.UpdateAnomaly updateAnomalyDto) {
        // ID로 Anomaly 조회
        var anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("No such anomaly."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 이상탐지 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(anomaly.getCompany().getId());
        // 업데이트할 Anomaly 정보 적용
        Optional.ofNullable(updateAnomalyDto.getCompanyId())
                .ifPresent(companyId -> {
                    var company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new EntityNotFoundException("No such company."));
                    anomaly.setCompany(company);
                });
        Optional.ofNullable(updateAnomalyDto.getLowestHourlyEnergyUsage()).ifPresent(anomaly::setLowestHourlyEnergyUsage);
        Optional.ofNullable(updateAnomalyDto.getHighestHourlyEnergyUsage()).ifPresent(anomaly::setHighestHourlyEnergyUsage);
        Optional.ofNullable(updateAnomalyDto.getAvailable()).ifPresent(anomaly::setAvailable);
        return new AnomalyDto.ReadAnomalyResponse(
                anomalyRepository.save(anomaly),
                anomaly.getCompany().getCountry().getZoneId());
    }

    /**
     * 이상탐지 삭제
     *
     * @param anomalyId 삭제할 이상탐지의 ID
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public void delete(Long anomalyId) {
        // ID로 Anomaly 조회
        var anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new EntityNotFoundException("No such anomaly."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 삭제하려는 이상탐지 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(anomaly.getCompany().getId());
        anomalyRepository.delete(anomaly);
    }
}