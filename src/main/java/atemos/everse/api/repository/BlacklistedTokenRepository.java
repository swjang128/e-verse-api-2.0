package atemos.everse.api.repository;

import atemos.everse.api.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;

/**
 * BlacklistedToken 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 */
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long>, JpaSpecificationExecutor<BlacklistedToken> {
    /**
     * 주어진 토큰이 블랙리스트에 존재하는지 여부를 확인합니다.
     *
     * @param token 블랙리스트에서 확인할 토큰
     * - 예: "exampleToken123" (토큰의 유효성을 확인할 때 사용)
     * @return 주어진 토큰이 블랙리스트에 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByToken(String token);
    /**
     * 지정된 기준 시간 이전이거나 생성 날짜가 없는 블랙리스트 토큰을 특정 회사들에 대해 삭제합니다.
     *
     * @param createdDate 삭제 기준 시간이 되는 Instant
     * @return 삭제된 BlacklistedToken 레코드 수
     */
    long deleteByCreatedDateBeforeOrCreatedDateIsNull(Instant createdDate);
}