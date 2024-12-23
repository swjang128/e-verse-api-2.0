package atemos.everse.api.service;

import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.entity.Member;
import org.springframework.data.domain.Pageable;

/**
 * MemberService는 사용자 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 사용자의 데이터베이스에 대한 CRUD 작업을 정의합니다.
 */
public interface MemberService {
    /**
     * 사용자를 등록합니다.
     *
     * @param createMemberDto 사용자를 생성하기 위한 정보가 담긴 데이터 전송 객체입니다.
     * @return 등록된 사용자 정보를 담고 있는 응답 객체입니다.
     */
    MemberDto.ReadMemberResponse create(MemberDto.CreateMember createMemberDto);
    /**
     * 조건에 맞는 사용자 목록을 조회합니다.
     *
     * @param readMemberRequestDto 사용자 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조회된 사용자 목록과 페이지 정보를 포함하는 맵 객체입니다.
     */
    MemberDto.ReadMemberPageResponse read(MemberDto.ReadMemberRequest readMemberRequestDto, Pageable pageable);
    /**
     * 기존의 사용자를 수정합니다.
     *
     * @param memberId 수정할 사용자의 ID입니다.
     * @param updateMemberDto 사용자 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 등록된 사용자 정보를 담고 있는 응답 객체입니다.
     */
    MemberDto.ReadMemberResponse update(Long memberId, MemberDto.UpdateMember updateMemberDto);
    /**
     * 사용자를 삭제합니다.
     *
     * @param memberId 삭제할 사용자의 ID입니다.
     */
    void delete(Long memberId);
    /**
     * 이메일 또는 전화번호가 중복되는지 확인하는 메서드입니다.
     * @param email 사용자 이메일
     * @param phone 사용자 전화번호
     */
    void checkDuplicateMember(String email, String phone);
    /**
     * 사용자 이메일로 사용자 정보를 로드합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티 객체
     */
    Member loadMemberByEmail(String email);
}