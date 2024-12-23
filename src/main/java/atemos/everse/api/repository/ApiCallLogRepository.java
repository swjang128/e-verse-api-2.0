package atemos.everse.api.repository;

import atemos.everse.api.dto.ApiCallLogDto;
import atemos.everse.api.entity.ApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * ApiCallLog 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 */
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long>, JpaSpecificationExecutor<ApiCallLog> {
    /**
     * 주어진 시간 이전에 생성된 ApiCallLog 엔티티의 리스트를 조회합니다.
     *
     * @param untilDateTime 조회 기준이 되는 날짜 및 시간
     * - 예: 특정 시점 이전의 모든 API 호출 로그를 조회할 때 사용
     * @return 주어진 시간 이전에 생성된 ApiCallLog 엔티티의 리스트
     */
    List<ApiCallLog> findByRequestTimeBefore(Instant untilDateTime);
    /**
     * 특정 업체 ID와 청구 여부에 따라 주어진 날짜 범위 내의 API 호출 로그 수를 계산합니다.
     *
     * @param companyId 업체 ID
     * - 예: 1 (특정 업체의 로그를 조회할 때 사용)
     * @param isCharge 과금 여부
     * - 예: true (과금), false (비과금)
     * @param startOfDay 날짜 범위의 시작 시점
     * - 예: 2024-07-01T00:00:00 (일 시작 시간)
     * @param endOfDay 날짜 범위의 종료 시점
     * - 예: 2024-07-01T23:59:59 (일 종료 시간)
     * @return 주어진 조건에 맞는 API 호출 로그의 수
     */
    long countByCompanyIdAndIsChargeAndRequestTimeBetween(Long companyId, boolean isCharge, Instant startOfDay, Instant endOfDay);

    /**
     * 특정 업체들에 대한 주어진 기간 동안의 유료 API 호출 로그 데이터를 조회합니다.
     *
     * @param companyIds 조회할 업체들의 ID 목록입니다.
     * @param startDate 조회 시작 날짜입니다.
     * @param endDate 조회 종료 날짜입니다.
     * @return 업체 ID와 날짜별 API 호출 로그 데이터를 담은 LogUsageData 객체 리스트를 반환합니다.
     */
    @Query("SELECT new atemos.everse.api.dto.ApiCallLogDto$LogUsageData(c.id, a.requestTime, COUNT(a)) " +
            "FROM ApiCallLog a JOIN a.company c WHERE c.id IN :companyIds AND a.requestTime BETWEEN :startDate AND :endDate AND a.isCharge = true " +
            "GROUP BY c.id, a.requestTime")
    List<ApiCallLogDto.LogUsageData> findLogUsageDataByCompanyIdsAndDateRange(
            @Param("companyIds") List<Long> companyIds,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}