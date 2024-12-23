package atemos.everse.api.repository;

import atemos.everse.api.entity.AuthenticationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthenticationLogRepository extends JpaRepository<AuthenticationLog, Long>, JpaSpecificationExecutor<AuthenticationLog> {
}
