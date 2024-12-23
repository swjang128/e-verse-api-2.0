package atemos.everse.api.repository;

import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Payment 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 스펙을 사용하여 복잡한 조건의 쿼리를 작성할 수 있도록 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 동적이고 복잡한 쿼리 작성 지원
 */
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    /**
     * 특정 업체의 특정 사용일(usageDate)에 해당하는 Payment 엔티티를 조회합니다.
     *
     * @param company 업체
     * @param usageDate 사용일 (조회할 날짜)
     * @return Optional로 감싼 Payment 엔티티. 없을 경우 빈 Optional을 반환합니다.
     */
    Optional<Payment> findByCompanyAndUsageDate(Company company, LocalDate usageDate);
    /**
     * 특정 업체와 관련된 모든 Payment 정보를 조회합니다.
     *
     * @param companyId 업체 ID
     * @return 해당 업체의 모든 Payment 목록
     */
    List<Payment> findAllByCompanyId(Long companyId);
    List<Payment> findAllByMeteredUsageId(Long meteredUsageId);
}