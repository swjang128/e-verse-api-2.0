package atemos.everse.api.repository;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Iot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Iot 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 */
public interface IotRepository extends JpaRepository<Iot, Long>, JpaSpecificationExecutor<Iot> {
    /**
     * 해당 업체가 보유한 IoT 장비를 조회합니다.
     * @param companyId 업체 ID
     * @return 해당 업체가 보유한 IoT 장비 데이터
     */
    List<Iot> findByCompanyId(Long companyId);
    /**
     * 특정 업체에서 주어진 상태의 IoT 장비 개수를 반환합니다.
     *
     * @param company   조회할 업체 객체
     * @param iotStatus 조회할 IoT 장비의 상태 (예: NORMAL, ERROR 등)
     * @return 해당 상태의 IoT 장비 개수
     */
    long countByCompanyAndStatus(Company company, IotStatus iotStatus);
    /**
     * IoT 장치와 관련된 Company와 Country를 함께 로드
     * @return 모든 IoT 장비
     */
    @Query("SELECT i FROM Iot i JOIN FETCH i.company c JOIN FETCH c.country")
    List<Iot> findAllWithCompanyAndCountry();
}