package atemos.everse.api.service;

import atemos.everse.api.dto.CountryDto;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * CountryService는 국가 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 국가와 관련된 주요 CRUD 작업을 처리하는 메소드를 정의합니다.
 */
public interface CountryService {
    /**
     * 새로운 국가를 등록합니다.
     *
     * @param createCountryDto 국가를 등록하기 위한 데이터 전송 객체입니다.
     * @return 등록된 국가 정보를 담고 있는 응답 객체입니다.
     */
    CountryDto.ReadCountryResponse create(CountryDto.CreateCountry createCountryDto);
    /**
     * 조건에 맞는 국가 정보를 조회합니다.
     *
     * @param readCountryRequestDto 국가 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 국가 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    CountryDto.ReadCountryPageResponse read(CountryDto.ReadCountryRequest readCountryRequestDto, Pageable pageable);
    /**
     * 모든 국가 목록을 조회합니다.
      * @return 국가 목록
     */
    Map<String, CountryDto.ReadAllCountryResponse> readAll();
    /**
     * 특정 국가 정보를 수정합니다.
     *
     * @param countryId 수정할 국가의 ID입니다.
     * @param updateCountryDto 국가 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 국가 정보를 담고 있는 응답 객체입니다.
     */
    CountryDto.ReadCountryResponse update(Long countryId, CountryDto.UpdateCountry updateCountryDto);
    /**
     * 특정 ID에 해당하는 국가를 삭제합니다.
     *
     * @param countryId 삭제할 국가의 ID입니다.
     */
    void delete(Long countryId);
}