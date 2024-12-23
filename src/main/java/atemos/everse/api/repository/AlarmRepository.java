package atemos.everse.api.repository;

import atemos.everse.api.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;

/**
 * Alarm 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 */
public interface AlarmRepository extends JpaRepository<Alarm, Long>, JpaSpecificationExecutor<Alarm> {
    /**
     * 지정된 기준 시간 이전에 생성된 알람 데이터를 특정 회사들에 대해 삭제합니다.
     *
     * @param createdDate 삭제 기준 시간이 되는 Instant
     * @param companyIds  삭제 대상 회사의 ID 목록
     * @return 삭제된 Alarm 레코드 수
     */
    long deleteByCreatedDateBeforeAndCompany_IdIn(Instant createdDate, List<Long> companyIds);
}