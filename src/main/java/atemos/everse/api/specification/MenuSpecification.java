package atemos.everse.api.specification;

import atemos.everse.api.dto.MenuDto;
import atemos.everse.api.entity.Menu;
import org.springframework.data.jpa.domain.Specification;

/**
 * Menu 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class MenuSpecification {
    /**
     * 주어진 MenuDto.ReadMenuRequest를 기반으로 Menu 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readMenuRequestDto 메뉴 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Menu 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Menu> findWith(MenuDto.ReadMenuRequest readMenuRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate=criteriaBuilder.conjunction();
            // 메뉴 ID 조건 추가
            if (readMenuRequestDto.getMenuId() != null) {
                predicate=criteriaBuilder.and(predicate, root.get("id").in(readMenuRequestDto.getMenuId()));
            }
            // 메뉴 이름 조건 추가
            if (readMenuRequestDto.getName() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readMenuRequestDto.getName() + "%"));
            }
            // URL 조건 추가
            if (readMenuRequestDto.getUrl() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("url"), "%" + readMenuRequestDto.getUrl() + "%"));
            }
            // 설명 조건 추가
            if (readMenuRequestDto.getDescription() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("description"), "%" + readMenuRequestDto.getDescription() + "%"));
            }
            // 사용 여부 조건 추가
            if (readMenuRequestDto.getAvailable() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("available"), readMenuRequestDto.getAvailable()));
            }
            // 상위 메뉴 ID 조건 추가
            if (readMenuRequestDto.getParentId() != null) {
                predicate=criteriaBuilder.and(predicate, root.get("parent").get("id").in(readMenuRequestDto.getParentId()));
            }
            // 접근 권한 조건 추가
            if (readMenuRequestDto.getRoles() != null && !readMenuRequestDto.getRoles().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.join("accessibleRoles").in(readMenuRequestDto.getRoles()));
            }
            return predicate;
        };
    }
}