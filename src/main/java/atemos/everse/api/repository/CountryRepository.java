package atemos.everse.api.repository;

import atemos.everse.api.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Country 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 */
public interface CountryRepository extends JpaRepository<Country, Long>, JpaSpecificationExecutor<Country> {
    /**
     * 특정 이름을 가진 국가를 조회합니다.
     * @param countryName 국가명
     * @return 국가 정보
     */
    Optional<Country> findByName(String countryName);
}