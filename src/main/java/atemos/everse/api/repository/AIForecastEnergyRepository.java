package atemos.everse.api.repository;

import atemos.everse.api.entity.AIForecastEnergy;
import atemos.everse.api.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AIForecastEnergy 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 * - 특정 비즈니스 로직에 맞춘 추가적인 쿼리 메소드 정의
 */
public interface AIForecastEnergyRepository extends JpaRepository<AIForecastEnergy, Long>, JpaSpecificationExecutor<AIForecastEnergy> {
    /**
     * 해당 업체 ID와 특정 시각에 대해 등록된 AI 예측 에너지 사용량 데이터가 있는지 확인합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @param forecastTime 예측할 시각
     * @return 해당 업체 ID와 특정 시각에 대해 등록된 AI 예측 에너지 사용량 데이터 유무
     */
    boolean existsByCompanyIdAndForecastTime(Long companyId, LocalDateTime forecastTime);
    /**
     * 해당 업체 ID와 날짜로 등록된 AI 예측 에너지 사용량 데이터가 있는지 확인합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @param startDateTime 조회 시작 날짜
     * @param endDateTime 조회 종료 날짜
     * @return 해당 업체 ID와 날짜로 등록된 AI 예측 에너지 사용량 데이터 유무
     */
    boolean existsByCompanyIdAndForecastTimeBetween(Long companyId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    /**
     * 지정된 기준 시간 이전에 예측된 에너지 데이터를 특정 회사들에 대해 삭제합니다.
     *
     * @param forecastTime 삭제 기준 시간이 되는 LocalDateTime
     * @param companyIds   삭제 대상 회사의 ID 목록
     * @return 삭제된 AIForecastEnergy 레코드 수
     */
    long deleteByForecastTimeBeforeAndCompany_IdIn(LocalDateTime forecastTime, List<Long> companyIds);
    /**
     * 주어진 업체 ID와 날짜 범위에 따라 AI가 예측한 에너지 데이터를 조회합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @param startDateTime 조회 시작 시각
     * @param endDateTime 조회 종료 시각
     * @return 주어진 조건에 맞는 AI가 예측한 에너지 데이터 리스트
     */
    List<AIForecastEnergy> findByCompanyIdAndForecastTimeBetween(Long companyId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    /**
     * 주어진 업체 ID와 특정 시각에 대해 AI가 예측한 에너지 데이터를 조회합니다.
     * 이 메서드는 특정 시간대에 대한 예측 데이터가 있는지 확인하고자 할 때 사용됩니다.
     *
     * @param company 조회할 업체의 ID
     * @param forecastTime 예측 데이터의 시간
     * @return 주어진 조건에 맞는 AI가 예측한 에너지 데이터, 없을 경우 빈 Optional 반환
     */
    Optional<AIForecastEnergy> findByCompanyAndForecastTime(Company company, LocalDateTime forecastTime);
}