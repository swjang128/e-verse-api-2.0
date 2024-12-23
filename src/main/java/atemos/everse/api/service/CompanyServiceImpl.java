package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.CompanyDto;
import atemos.everse.api.entity.Company;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.CountryRepository;
import atemos.everse.api.specification.CompanySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * CompanyServiceImpl는 업체와 관련된 서비스 로직을 구현한 클래스입니다.
 * 업체 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CountryRepository countryRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final JwtUtil jwtUtil;

    /**
     * 업체를 등록합니다.
     *
     * @param createCompanyDto 등록할 업체 정보를 담고 있는 DTO
     * @return 등록된 업체 정보를 담고 있는 DTO 응답
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CompanyDto.ReadCompanyResponse create(CompanyDto.CreateCompany createCompanyDto) {
        // 국가가 존재하는지 확인하고 존재하지 않으면 예외 처리
        var country = countryRepository.findById(createCompanyDto.getCountryId())
                .orElseThrow(() -> new EntityNotFoundException("No such country."));
        // 업체 정보를 빌드하고 저장
        var company = Company.builder()
                .name(createCompanyDto.getName())
                .email(createCompanyDto.getEmail())
                .tel(createCompanyDto.getTel())
                .fax(createCompanyDto.getFax())
                .address(createCompanyDto.getAddress())
                .type(createCompanyDto.getType())
                .country(country)
                .build();
        // 업체를 데이터베이스에 저장
        company = companyRepository.save(company);
        // 업체가 소속된 국가의 시간대 정보를 가져오기
        var zoneId = company.getCountry().getZoneId();
        // 저장된 업체 정보를 반환
        return new CompanyDto.ReadCompanyResponse(company, zoneId);
    }

    /**
     * 조건에 맞는 업체 목록을 조회합니다.
     *
     * @param readCompanyRequestDto 업체 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 업체 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public CompanyDto.ReadCompanyPageResponse read(CompanyDto.ReadCompanyRequest readCompanyRequestDto, Pageable pageable) {
        // 조건에 맞는 업체 목록을 페이지 단위로 조회
        var companyPage = companyRepository.findAll(
                CompanySpecification.findWith(readCompanyRequestDto),
                pageable);
        // 조회된 업체 목록을 응답 객체로 변환하여 반환
        var companyList = companyPage.getContent().stream()
                .map(company -> new CompanyDto.ReadCompanyResponse(company, company.getCountry().getZoneId()))
                .toList();
        return CompanyDto.ReadCompanyPageResponse.builder()
                .companyList(companyList)
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .build();
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인된 사용자의 업체 정보를 조회합니다.
     *
     * @return 업체 정보 객체입니다. 사용자가 속한 업체 정보가 포함됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyDto.ReadCompanyResponse readCompanyInfo() {
        // 현재 사용자의 정보 조회
        var memberInfo = authenticationService.getCurrentUserInfo();
        // 업체 정보 조회
        var company = companyRepository.findById(memberInfo.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 업체가 소속된 국가의 시간대 정보를 가져오기
        var zoneId = company.getCountry().getZoneId();
        return new CompanyDto.ReadCompanyResponse(company, zoneId);
    }

    /**
     * 회원 가입 화면에서 노출되는 업체 목록을 조회합니다.
     *
     * @return 가입 가능한 업체 목록을 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyDto.ReadCompanyListResponse readSignUpCompanyList() {
        // 모든 업체를 조회하여 DTO로 변환
        var companyList = companyRepository.findAll().stream()
                .map(company -> CompanyDto.ReadCompanyResponse.builder()
                        .companyId(company.getId())
                        .name(company.getName())
                        .languageCode(company.getCountry().getLanguageCode())
                        .build())
                .toList();
        // 조회된 업체 목록을 응답 객체로 반환
        return CompanyDto.ReadCompanyListResponse.builder()
                .companyList(companyList)
                .build();
    }

    /**
     * 기존 업체 정보를 수정합니다.
     *
     * @param companyId 수정할 업체의 ID
     * @param updateCompanyDto 수정할 업체 정보를 담고 있는 DTO
     * @return 수정된 업체 정보를 담은 응답 객체
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public CompanyDto.ReadCompanyResponse update(Long companyId, CompanyDto.UpdateCompany updateCompanyDto) {
        // 수정할 업체가 존재하는지 확인하고 없으면 예외 처리
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 현재 사용자가 이 업체에 접근할 권한이 있는지 확인
        authenticationService.validateCompanyAccess(company.getId());
        // 국가가 존재하는지 확인하고, 업데이트하려는 경우 설정
        Optional.ofNullable(updateCompanyDto.getCountryId())
                .ifPresent(countryId -> {
                    var country = countryRepository.findById(countryId)
                            .orElseThrow(() -> new EntityNotFoundException("No such country."));
                    company.setCountry(country);
                });
        // 업체의 나머지 필드들을 업데이트
        Optional.ofNullable(updateCompanyDto.getName()).ifPresent(company::setName);
        Optional.ofNullable(updateCompanyDto.getType()).ifPresent(company::setType);
        Optional.ofNullable(updateCompanyDto.getEmail()).ifPresent(company::setEmail);
        Optional.ofNullable(updateCompanyDto.getTel()).ifPresent(company::setTel);
        Optional.ofNullable(updateCompanyDto.getFax()).ifPresent(company::setFax);
        Optional.ofNullable(updateCompanyDto.getAddress()).ifPresent(company::setAddress);
        // 업체의 시간대 정보를 가져오기
        var zoneId = company.getCountry().getZoneId();
        // 수정된 업체 정보를 저장하고 반환
        return new CompanyDto.ReadCompanyResponse(companyRepository.save(company), zoneId);
    }

    /**
     * 업체를 삭제합니다.
     *
     * @param companyId 삭제할 업체의 ID
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long companyId) {
        // 삭제할 업체가 존재하는지 확인하고 없으면 예외 처리
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 현재 사용자가 이 업체에 접근할 권한이 있는지 확인
        authenticationService.validateCompanyAccess(company.getId());
        // 업체를 삭제
        companyRepository.delete(company);
    }
}