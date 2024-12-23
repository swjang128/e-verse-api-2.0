package atemos.everse.api.repository;

import atemos.everse.api.entity.Iot;
import atemos.everse.api.entity.IotStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * IotHistory 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 * - 특정 비즈니스 로직에 맞춘 추가적인 쿼리 메소드 정의
 */
public interface IotStatusHistoryRepository extends JpaRepository<IotStatusHistory, Long>, JpaSpecificationExecutor<IotStatusHistory> {
    /**
     * 지정된 기준 시간 이전에 생성된 IoT 상태 기록을 특정 회사들에 대해 삭제합니다.
     *
     * @param createdDate 삭제 기준 시간이 되는 Instant
     * @param companyIds  삭제 대상 회사의 ID 목록
     * @return 삭제된 IotStatusHistory 레코드 수
     */
    long deleteByCreatedDateBeforeAndIot_Company_IdIn(Instant createdDate, List<Long> companyIds);
    /**
     * 특정 날짜에 해당 업체의 IoT 이력 데이터를 조회합니다.
     * @param companyId 업체 ID
     * @param startOfDayUtc 검색 날짜 시작일시 (ZonedDateTime -> LocalDateTime, UTC)
     * @param endOfDayUtc 검색 날짜 종료 일시 (ZonedDateTime -> LocalDateTime, UTC)
     * @return Iot 이력 데이터 목록
     */
    List<IotStatusHistory> findByIot_Company_IdAndCreatedDateBetween(Long companyId, Instant startOfDayUtc, Instant endOfDayUtc);
    /**
     * 특정 IoT 장비의 상태 이력 데이터를 삭제합니다.
     * @param iot Iot 장비의 정보
     */
    void deleteByIot(Iot iot);
    /**
     * 특정 회사에서 주어진 기간 동안 등록된 IoT 장비의 개수를 조회하는 메서드입니다.
     *
     * @param companyId 조회할 회사의 ID입니다.
     * @param startDate 조회 시작 날짜입니다 (해당 날짜의 00:00:00).
     * @param endDate 조회 종료 날짜입니다 (해당 날짜의 23:59:59).
     * @return 주어진 기간 동안 등록된 IoT 장비의 개수를 반환합니다.
     */
    @Query(
            "SELECT COUNT(DISTINCT iotStatusHistory.iot.id) " +
                    "FROM IotStatusHistory iotStatusHistory " +
                    "WHERE iotStatusHistory.iot.company.id = :companyId " +
                    "AND iotStatusHistory.createdDate BETWEEN :startDate AND :endDate"
    )
    Long countIotInstallationsByCompanyAndDateRange(
            @Param("companyId") Long companyId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


}