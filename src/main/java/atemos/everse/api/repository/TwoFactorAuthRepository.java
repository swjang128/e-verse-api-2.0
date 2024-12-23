package atemos.everse.api.repository;

import atemos.everse.api.entity.Member;
import atemos.everse.api.entity.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Optional;

/**
 * TwoFactorAuth 엔티티와 관련된 데이터베이스 작업을 처리하는 리포지토리 인터페이스입니다.
 */
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long>, JpaSpecificationExecutor<TwoFactorAuth> {
    /**
     * 특정 회원의 가장 최근에 생성된 검증되지 않은(isVerified가 false인) TwoFactorAuth 엔티티를 조회합니다.
     * 생성일(createdDate)을 기준으로 내림차순 정렬하여 가장 최신의 엔티티를 가져옵니다.
     *
     * @param member 회원 엔티티
     * @return 가장 최근의 검증되지 않은 TwoFactorAuth 엔티티의 Optional 객체
     */
    Optional<TwoFactorAuth> findFirstByMemberAndIsVerifiedFalseOrderByCreatedDateDesc(Member member);
    /**
     * 지정된 회원의 특정 시간 이후에 생성된 가장 이른 2차 인증 정보를 반환합니다.
     *
     * @param member 대상 회원
     * @param after 이 시간 이후에 생성된 인증 정보 중 가장 이른 것을 반환
     * @return 가장 먼저 생성된 2차 인증 정보를 포함하는 Optional 객체
     */
    Optional<TwoFactorAuth> findFirstByMemberAndCreatedDateAfterOrderByCreatedDateDesc(Member member, Instant after);
    /**
     * 지정된 기준 시간 이전에 생성된 2차 인증 정보를 삭제합니다.
     *
     * @param createdDate 삭제 기준 시간이 되는 Instant
     * @return 삭제된 TwoFactorAuth 레코드 수
     */
    long deleteByCreatedDateBefore(Instant createdDate);
}