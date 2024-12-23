package atemos.everse.api.repository;

import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Subscription 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    /**
     * 특정 업체가 특정 서비스에 대해 아직 종료되지 않은 활성 구독을 가지고 있는지 확인합니다.
     *
     * @param companyId 업체의 ID
     * @param service   구독 서비스 목록 중 하나 (예: AI_ENERGY_USAGE_FORECAST)
     * @return 활성 구독이 존재하면 true, 그렇지 않으면 false
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.company.id = :companyId AND s.service = :service AND (" +
            "(s.startDate <= :endDate AND s.endDate >= :startDate) OR " +  // 새로운 기간이 기존 구독과 겹치는지 확인
            "(s.startDate <= :startDate AND (s.endDate IS NULL OR s.endDate >= :endDate))" +  // 기존 구독이 새로운 기간과 완전히 포함되는지 확인
            ")")
    boolean existsByCompanyIdAndServiceAndDateRangeOverlap(@Param("companyId") Long companyId,
                                                           @Param("service") SubscriptionServiceList service,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    /**
     * 특정 업체 ID 목록과 특정 날짜에 해당하는 활성 구독 목록을 조회합니다.
     * 이 메서드는 업체가 해당 날짜에 활성화된 구독 서비스를 가지고 있는지 확인하는 데 사용됩니다.
     * @param companyIds 업체 ID 목록
     * @param date       조회할 날짜 (해당 날짜에 유효한 구독 정보를 조회)
     * @return 특정 날짜에 활성화된 구독 목록
     */
    @Query("SELECT s FROM Subscription s WHERE s.company.id IN :companyIds AND s.startDate <= :date AND (s.endDate IS NULL OR s.endDate >= :date)")
    List<Subscription> findAllByCompanyIdsAndDate(@Param("companyIds") List<Long> companyIds, @Param("date") LocalDate date);
    /**
     * 주어진 업체와 서비스에 대해 현재 유효한 구독의 수를 반환합니다.
     *
     * @param company 확인할 업체 객체
     * @param service 확인할 구독 서비스
     * @param date    기준 날짜 (일반적으로 현재 날짜)
     * @return 현재 유효한 구독의 수
     */
    @Query("SELECT COUNT(s) FROM Subscription s " +
            "WHERE s.company = :company " +
            "AND s.service = :service " +
            "AND s.startDate <= :date " +
            "AND (s.endDate IS NULL OR s.endDate >= :date)")  // 종료일이 오늘 이후거나 종료일이 NULL인 경우만 포함
    Long countValidSubscription(@Param("company") Company company,
                                @Param("service") SubscriptionServiceList service,
                                @Param("date") LocalDate date);
}