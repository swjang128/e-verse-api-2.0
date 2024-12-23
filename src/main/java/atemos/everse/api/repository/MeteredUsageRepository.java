package atemos.everse.api.repository;

import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.MeteredUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MeteredUsage 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 */
public interface MeteredUsageRepository extends JpaRepository<MeteredUsage, Long>, JpaSpecificationExecutor<MeteredUsage> {
    /**
     * 특정 회사의 가장 이른 MeteredUsage를 조회합니다.
     *
     * @param company 업체
     * @return 가장 이른 MeteredUsage 또는 Optional.empty()
     */
    Optional<MeteredUsage> findFirstByCompanyOrderByUsageDateAsc(Company company);
    /**
     * 특정 회사의 특정 날짜 범위 내 MeteredUsage 데이터를 조회합니다.
     *
     * @param company 업체 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 해당 범위 내의 MeteredUsage 리스트
     */
    List<MeteredUsage> findByCompanyAndUsageDateBetween(Company company, LocalDate startDate, LocalDate endDate);
    /**
     * 특정 업체의 특정 사용일(usageDate)에 해당하는 MeteredUsage 엔티티를 조회합니다.
     *
     * @param company 업체
     * @param companyLocalDate 해당 업체의 서비스 사용일
     * @return Optional로 감싼 MeteredUsage 엔티티. 없을 경우 빈 Optional을 반환합니다.
     */
    Optional<MeteredUsage> findByCompanyAndUsageDate(Company company, LocalDate companyLocalDate);
}