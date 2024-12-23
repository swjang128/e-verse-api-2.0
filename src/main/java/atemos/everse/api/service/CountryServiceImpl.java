package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.CountryDto;
import atemos.everse.api.entity.Country;
import atemos.everse.api.repository.CountryRepository;
import atemos.everse.api.specification.CountrySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CountryServiceImpl는 국가 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * 국가 등록, 조회, 수정, 삭제와 관련된 메서드를 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 국가를 등록합니다.
     *
     * @param createCountryDto 국가를 등록하기 위한 데이터 전송 객체입니다.
     * @return 등록된 국가 정보를 담고 있는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public CountryDto.ReadCountryResponse create(CountryDto.CreateCountry createCountryDto) {
        // 새로운 Country 엔티티를 빌드하고 저장합니다.
        var country = Country.builder()
                .name(createCountryDto.getName())
                .languageCode(createCountryDto.getLanguageCode())
                .timeZone(createCountryDto.getTimeZone())
                .build();
        // 저장된 Country 엔티티를 응답 객체로 변환하여 반환합니다.
        return new CountryDto.ReadCountryResponse(countryRepository.save(country));
    }

    /**
     * 조건에 맞는 국가 정보를 조회합니다.
     *
     * @param readCountryRequestDto 국가 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 국가 목록과 관련된 추가 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public CountryDto.ReadCountryPageResponse read(CountryDto.ReadCountryRequest readCountryRequestDto, Pageable pageable) {
        // 조건에 맞는 Country 목록을 조회합니다.
        var countryPage = countryRepository.findAll(CountrySpecification.findWith(readCountryRequestDto), pageable);
        // Country 응답 객체로 변환할 때 zoneId를 사용하여 반환(countryPage.getCode()를 Key로 해서 내리기)
        var countryList = countryPage.getContent().stream()
                .map(CountryDto.ReadCountryResponse::new)
                .toList();
        return new CountryDto.ReadCountryPageResponse(
                countryList,
                countryPage.getTotalElements(),
                countryPage.getTotalPages());
    }

    /**
     * 모든 국가 정보를 조회합니다.
     *
     * @return 모든 국가 목록과 관련된 추가 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, CountryDto.ReadAllCountryResponse> readAll() {
        // 모든 Country 목록을 조회합니다.
        var countries = countryRepository.findAll();
        // Country 응답 객체를 countryId에 따라 정렬하고, 필요한 정보만 포함하여 ISO 코드가 Key인 Map으로 변환
        return countries.stream()
                .sorted(Comparator.comparing(Country::getId))  // countryId로 정렬
                .collect(Collectors.toMap(
                        Country::getLanguageCode,
                        country -> new CountryDto.ReadAllCountryResponse(
                                country.getId(),
                                country.getName(),
                                country.getTimeZone()),
                        (existing, replacement) -> existing,  // 충돌이 발생하면 기존 항목 유지
                        LinkedHashMap::new  // 순서를 유지하기 위해 LinkedHashMap 사용
                ));
    }

    /**
     * 특정 국가 정보를 수정합니다.
     *
     * @param countryId 수정할 국가의 ID입니다.
     * @param updateCountryDto 국가 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 국가 정보를 담고 있는 응답 객체입니다.
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public CountryDto.ReadCountryResponse update(Long countryId, CountryDto.UpdateCountry updateCountryDto) {
        // 수정할 Country 엔티티를 조회합니다.
        var country = countryRepository.findById(countryId)
                .orElseThrow(() -> new EntityNotFoundException("No such country."));
        // 전달된 수정 정보로 Country 엔티티를 업데이트합니다.
        Optional.ofNullable(updateCountryDto.getName()).ifPresent(country::setName);
        Optional.ofNullable(updateCountryDto.getLanguageCode()).ifPresent(country::setLanguageCode);
        Optional.ofNullable(updateCountryDto.getTimeZone()).ifPresent(country::setTimeZone);
        // 수정된 Country 엔티티를 저장하고 응답 객체로 변환하여 반환합니다.
        return new CountryDto.ReadCountryResponse(countryRepository.save(country));
    }

    /**
     * 특정 ID에 해당하는 국가를 삭제합니다.
     *
     * @param countryId 삭제할 국가의 ID입니다.
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void delete(Long countryId) {
        // 삭제할 Country 엔티티를 조회하여 삭제합니다.
        countryRepository.findById(countryId).ifPresentOrElse(
                countryRepository::delete,
                () -> {
                    throw new EntityNotFoundException("No such country.");
                }
        );
    }
}