package atemos.everse.api.specification;

import atemos.everse.api.dto.CompanyDto;
import atemos.everse.api.entity.Company;
import org.springframework.data.jpa.domain.Specification;

/**
 * Company 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class CompanySpecification {
    /**
     * 주어진 CompanyDto.ReadCompanyRequest를 기반으로 Company 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readCompanyRequestDto 업체 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Company 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Company> findWith(CompanyDto.ReadCompanyRequest readCompanyRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 업체 ID 조건 추가
            if (readCompanyRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readCompanyRequestDto.getCompanyId()));
            }
            // 국가 ID 조건 추가
            if (readCompanyRequestDto.getCountryId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("country").get("id").in(readCompanyRequestDto.getCountryId()));
            }
            // 업체명 조건 추가
            if (readCompanyRequestDto.getName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"),"%" + readCompanyRequestDto.getName() + "%"));
            }
            // 업체 유형 조건 추가
            if (readCompanyRequestDto.getType() != null&&!readCompanyRequestDto.getType().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("type").in(readCompanyRequestDto.getType()));
            }
            // 이메일 조건 추가
            if (readCompanyRequestDto.getEmail() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("email"),"%" + readCompanyRequestDto.getEmail() + "%"));
            }
            // 연락처 조건 추가
            if (readCompanyRequestDto.getTel() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("tel"),"%" + readCompanyRequestDto.getTel() + "%"));
            }
            // 팩스 조건 추가
            if (readCompanyRequestDto.getFax() != null) {
                predicate = criteriaBuilder.and(predicate,criteriaBuilder.like(root.get("fax"),"%" + readCompanyRequestDto.getFax() + "%"));
            }
            // 주소 조건 추가
            if (readCompanyRequestDto.getAddress() != null) {
                predicate = criteriaBuilder.and(predicate,criteriaBuilder.like(root.get("address"),"%" + readCompanyRequestDto.getAddress() + "%"));
            }
            return predicate;
        };
    }
}