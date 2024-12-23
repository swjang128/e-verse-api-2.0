package atemos.everse.api.repository;

import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Energy;
import atemos.everse.api.entity.Iot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Energy 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 * - 특정 비즈니스 로직에 맞춘 추가적인 쿼리 메소드 정의
 */
public interface EnergyRepository extends JpaRepository<Energy, Long>, JpaSpecificationExecutor<Energy> {
    /**
     *  주어진 IoT ID와 날짜 범위에 따라 에너지 데이터를 조회합니다.
     * @param iotList 조회할 IoT의 ID
     * @param startDateTime 조회 시작 날짜
     * @param endDateTime 조회 종료 날짜
     * @return 주어진 조건에 맞는 에너지 데이터
     */
    List<Energy> findByIotInAndReferenceTimeBetween(List<Iot> iotList, LocalDateTime startDateTime, LocalDateTime endDateTime);
    /**
     * 지정된 기준 시간 이전에 수집된 에너지 데이터를 특정 회사들에 대해 삭제합니다.
     *
     * @param referenceTime 삭제 기준 시간이 되는 LocalDateTime
     * @param companyIds    삭제 대상 회사의 ID 목록
     * @return 삭제된 Energy 레코드 수
     */
    long deleteByReferenceTimeBeforeAndIot_Company_IdIn(LocalDateTime referenceTime, List<Long> companyIds);
    /**
     * 특정 IoT 장비의 에너지 수집 기록을 삭제합니다.
     *
     * @param iot Iot 장비의 정보
     */
    void deleteByIot(Iot iot);
    /**
     * 특정 IoT 장비와 기준 시각에 해당하는 총 시설 에너지 사용량을 반환합니다.
     * 이 메서드는 지정된 IoT 장비(`iotId`)에서 특정 기준 시각(`referenceTime`)에 기록된
     * 에너지 사용량(`facilityUsage`)을 합산하여 총 에너지 사용량을 구합니다.
     *
     * @param iotId          에너지 사용량을 조회할 IoT 장비의 고유 식별자
     * @param referenceTime  에너지 사용량을 조회할 기준 시각
     * @return 지정된 IoT 장비와 기준 시각에 대한 총 시설 에너지 사용량 (`BigDecimal`);
     *         데이터가 없을 경우 `null`을 반환합니다.
     */
    @Query("SELECT SUM(e.facilityUsage) FROM Energy e WHERE e.iot.id = :iotId AND e.referenceTime = :referenceTime")
    BigDecimal findHourlyUsageByIotAndTime(@Param("iotId") Long iotId,
                                           @Param("referenceTime") LocalDateTime referenceTime);
    /**
     * 특정 회사의 특정 기간에 대한 총 에너지 사용량을 반환합니다.
     *
     * @param company 회사 엔티티
     * @param start   조회할 시작 일시
     * @param end     조회할 종료 일시
     * @return 총 에너지 사용량 (BigDecimal)
     */
    @Query("SELECT SUM(e.facilityUsage) FROM Energy e WHERE e.iot.company = :company AND e.referenceTime BETWEEN :start AND :end")
    BigDecimal getTotalFacilityUsage(@Param("company") Company company,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}