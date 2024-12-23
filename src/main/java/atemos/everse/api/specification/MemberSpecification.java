package atemos.everse.api.specification;

import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.entity.Member;
import org.springframework.data.jpa.domain.Specification;

/**
 * Member 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class MemberSpecification {
    /**
     * 주어진 MemberDto.ReadMemberRequest를 기반으로 Member 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readMemberRequestDto 사용자 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Member 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Member> findWith(MemberDto.ReadMemberRequest readMemberRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate=criteriaBuilder.conjunction();
            // 사용자 ID 조건 추가
            if (readMemberRequestDto.getMemberId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readMemberRequestDto.getMemberId()));
            }
            // 업체 ID 조건 추가
            if (readMemberRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readMemberRequestDto.getCompanyId()));
            }
            // 이름 조건 추가
            if (readMemberRequestDto.getName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readMemberRequestDto.getName() + "%"));
            }
            // 이메일 조건 추가
            if (readMemberRequestDto.getEmail() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("email"), "%" + readMemberRequestDto.getEmail() + "%"));
            }
            // 연락처 조건 추가
            if (readMemberRequestDto.getPhone() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("phone"), "%" + readMemberRequestDto.getPhone() + "%"));
            }
            // 권한 조건 추가
            if (readMemberRequestDto.getRole() != null && !readMemberRequestDto.getRole().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("role").in(readMemberRequestDto.getRole()));
            }
            // 상태 조건 추가
            if (readMemberRequestDto.getStatus() != null && !readMemberRequestDto.getStatus().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(readMemberRequestDto.getStatus()));
            }
            return predicate;
        };
    }
}