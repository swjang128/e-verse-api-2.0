package atemos.everse.api.repository;

import atemos.everse.api.entity.Anomaly;
import atemos.everse.api.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Anomaly 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 스펙을 사용하여 복잡한 조건의 쿼리를 작성할 수 있도록 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 동적이고 복잡한 쿼리 작성 지원
 * - 특정 회사와 알람 유형, 활성화 여부를 기반으로 Anomaly 엔티티 조회 지원
 */
public interface AnomalyRepository extends JpaRepository<Anomaly, Long>, JpaSpecificationExecutor<Anomaly> {
    /**
     * 주어진 업체, 활성화 여부를 기반으로 Anomaly 엔티티를 조회합니다.
     *
     * @param company 조회할 Anomaly 엔티티와 관련된 업체
     * @param available 활성화 여부
     * @return 조건에 맞는 Anomaly 엔티티, 없으면 null 반환
     */
    Optional<Anomaly> findByCompanyAndAvailable(Company company, Boolean available);
}