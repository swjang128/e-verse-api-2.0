package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.IotDto;
import atemos.everse.api.entity.Iot;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.EnergyRepository;
import atemos.everse.api.repository.IotRepository;
import atemos.everse.api.repository.IotStatusHistoryRepository;
import atemos.everse.api.specification.IotSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * IotServiceImpl 클래스는 IoT 장치의 생성, 조회, 수정, 삭제 등의 기능을 제공하는 서비스 클래스입니다.
 * 이 클래스는 회사의 ID에 따라 IoT 장치에 접근할 수 있는지 검증하고,
 * IoT 장치와 관련된 다양한 데이터를 관리합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IotServiceImpl implements IotService {
    private final IotRepository iotRepository;
    private final CompanyRepository companyRepository;
    private final EnergyRepository energyRepository;
    private final IotStatusHistoryRepository iotStatusHistoryRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * IoT 장치를 등록합니다.
     *
     * @param createIotDto IoT 생성 정보를 담은 DTO입니다.
     * @return 등록된 IoT 정보가 담긴 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public IotDto.ReadIotResponse create(IotDto.CreateIot createIotDto) {
        // 업체가 존재하는지 확인
        var company = companyRepository.findById(createIotDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 등록하려는 IoT 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(company.getId());
        // IoT 엔티티 생성 및 저장
        var iot = Iot.builder()
                .company(company)
                .serialNumber(createIotDto.getSerialNumber())
                .status(createIotDto.getStatus())
                .type(createIotDto.getType())
                .location(createIotDto.getLocation())
                .price(createIotDto.getPrice())
                .build();
        // IoT Insert 및 엔티티 리턴
        return new IotDto.ReadIotResponse(iotRepository.save(iot), company.getCountry().getZoneId());
    }

    /**
     * 조건에 맞는 IoT 장치를 조회합니다.
     *
     * @param readIotRequestDto IoT 조회 요청을 위한 DTO입니다.
     * @param pageable 페이지 정보입니다.
     * @return 조건에 맞는 IoT 목록과 페이지 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public IotDto.ReadIotPageResponse read(IotDto.ReadIotRequest readIotRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var member = jwtUtil.getCurrentMember();
        var zoneId = member.getCompany().getCountry().getZoneId();
        // 조건에 맞는 IoT 조회
        var iotPage = iotRepository.findAll(IotSpecification.findWith(readIotRequestDto), pageable);
        // 결과를 DTO로 변환하여 반환(zoneId를 사용하여 생성일과 수정일을 보여주기)
        var iotList = iotPage.getContent().stream()
                .map(iot -> new IotDto.ReadIotResponse(iot, zoneId))
                .toList();
        return new IotDto.ReadIotPageResponse(
                iotList,
                iotPage.getTotalElements(),
                iotPage.getTotalPages());
    }

    /**
     * 기존의 IoT 장치를 수정합니다.
     *
     * @param iotId 수정할 IoT 장치의 ID입니다.
     * @param updateIotDto IoT 장치 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 수정된 IoT 장치의 정보가 담긴 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public IotDto.ReadIotResponse update(Long iotId, IotDto.UpdateIot updateIotDto) {
        // IoT가 존재하는지 확인
        var iot = iotRepository.findById(iotId)
                .orElseThrow(() -> new EntityNotFoundException("No such IoT"));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 IoT 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(iot.getCompany().getId());
        // 업체가 존재하는지 확인
        Optional.ofNullable(updateIotDto.getCompanyId())
                .ifPresent(companyId -> {
                    var company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new EntityNotFoundException("No such company."));
                    iot.setCompany(company);
                });
        // IoT 정보 업데이트
        Optional.ofNullable(updateIotDto.getSerialNumber()).ifPresent(iot::setSerialNumber);
        Optional.ofNullable(updateIotDto.getType()).ifPresent(iot::setType);
        Optional.ofNullable(updateIotDto.getLocation()).ifPresent(iot::setLocation);
        Optional.ofNullable(updateIotDto.getPrice()).ifPresent(iot::setPrice);
        Optional.ofNullable(updateIotDto.getStatus()).ifPresent(iot::setStatus);
        // IoT 정보 저장
        return new IotDto.ReadIotResponse(iotRepository.save(iot), iot.getCompany().getCountry().getZoneId());
    }

    /**
     * IoT 장치를 삭제합니다.
     *
     * @param iotId 삭제할 IoT 장치의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public void delete(Long iotId) {
        // IoT 정보가 있는지 확인
        var iot = iotRepository.findById(iotId)
                .orElseThrow(() -> new EntityNotFoundException("No such IoT."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 삭제하려는 IoT 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(iot.getCompany().getId());
        // 연관된 energy 데이터 삭제
        energyRepository.deleteByIot(iot);
        // IoT와 연관된 IotStatusHistory 데이터를 삭제
        iotStatusHistoryRepository.deleteByIot(iot);
        // IoT 정보 삭제
        iotRepository.delete(iot);
    }
}