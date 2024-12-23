package atemos.everse.api.specification;

import atemos.everse.api.dto.CountryDto;
import atemos.everse.api.entity.Country;
import org.springframework.data.jpa.domain.Specification;

/**
 * Country 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class CountrySpecification {
    /**
     * 주어진 CountryDto.ReadCountryRequest를 기반으로 Country 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readCountryRequestDto Country 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Country 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Country> findWith(CountryDto.ReadCountryRequest readCountryRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // Country ID 조건 추가
            if (readCountryRequestDto.getCountryId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readCountryRequestDto.getCountryId()));
            }
            // 국가 이름 조건 추가
            if (readCountryRequestDto.getName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readCountryRequestDto.getName() + "%"));
            }
            // 국가의 언어 코드 조건 추가
            if (readCountryRequestDto.getLanguageCode() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("languageCode"), readCountryRequestDto.getLanguageCode()));
            }
            // 국가의 타임존 조건 추가
            if (readCountryRequestDto.getTimeZone() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("timeZone"), "%" + readCountryRequestDto.getTimeZone() + "%"));
            }
            return predicate;
        };
    }
}