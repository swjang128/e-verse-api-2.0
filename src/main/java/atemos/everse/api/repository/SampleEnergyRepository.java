package atemos.everse.api.repository;

import atemos.everse.api.entity.SampleEnergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SampleEnergyRepository extends JpaRepository<SampleEnergy, Long>, JpaSpecificationExecutor<SampleEnergy> {
}
