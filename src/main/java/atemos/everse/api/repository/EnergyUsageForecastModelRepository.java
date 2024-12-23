package atemos.everse.api.repository;

import atemos.everse.api.entity.EnergyUsageForecastModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * EnergyUsageForecastModel 엔티티에 대한 CRUD 작업을 수행하는 JPA 레포지토리입니다.
 * Oracle HeatWave에서 학습된 모델 데이터를 가져오기 위한 메서드를 포함합니다.
 */
@Repository
public interface EnergyUsageForecastModelRepository extends JpaRepository<EnergyUsageForecastModel, Long> {
    /**
     * 회사 ID와 예측 시간에 따라 HeatWave 데이터를 조회합니다.
     *
     * @param companyId    회사의 ID
     * @param forecastTime 예측된 시간
     * @return 회사의 특정 시간에 해당하는 HeatWave 예측 데이터
     */
    Optional<EnergyUsageForecastModel> findByCompanyIdAndForecastTime(Long companyId, LocalDateTime forecastTime);
}
