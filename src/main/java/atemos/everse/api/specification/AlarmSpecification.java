package atemos.everse.api.specification;

import atemos.everse.api.dto.AlarmDto;
import atemos.everse.api.entity.Alarm;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;

/**
 * AlarmSpecification 클래스는 Alarm 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 * 다양한 필터링 조건을 지원하며, 주어진 조건에 따라 알람 데이터를 조회하는 데 사용됩니다.
 */
public class AlarmSpecification {
    /**
     * 주어진 AlarmDto.ReadAlarmRequest 객체를 기반으로 Alarm 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readAlarmRequestDto 알람 조회 조건을 포함하는 데이터 전송 객체
     * @param zoneId 클라이언트의 타임존
     * @return 주어진 조건에 맞는 Alarm 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Alarm> findWith(AlarmDto.ReadAlarmRequest readAlarmRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건을 위한 Predicate 초기화
            var predicate = criteriaBuilder.conjunction();
            // 알람 ID로 필터링
            if (readAlarmRequestDto.getAlarmId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readAlarmRequestDto.getAlarmId()));
            }
            // 업체 ID로 필터링
            if (readAlarmRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readAlarmRequestDto.getCompanyId()));
            }
            // 알람 유형으로 필터링
            if (readAlarmRequestDto.getType() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("type").in(readAlarmRequestDto.getType()));
            }
            // 우선 순위로 필터링
            if (readAlarmRequestDto.getPriority() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("priority"), readAlarmRequestDto.getPriority()));
            }
            // 알림 여부로 필터링
            if (readAlarmRequestDto.getNotify() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("notify"), readAlarmRequestDto.getNotify()));
            }
            // 읽음 여부로 필터링
            if (readAlarmRequestDto.getIsRead() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isRead"), readAlarmRequestDto.getIsRead()));
            }
            // 알람 만료일로 필터링
            if (readAlarmRequestDto.getExpirationDate() != null) {
                var expirationDateUTC = readAlarmRequestDto.getExpirationDate().atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("expirationDate"), expirationDateUTC));
            }
            // 알람 생성일로 필터링 (시작일과 종료일)
            if (readAlarmRequestDto.getEndDateTime() != null) {
                var endDate = readAlarmRequestDto.getEndDateTime().withHour(23).withMinute(59).withSecond(59);
                var endDateUTC = endDate.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDateUTC));
            }
            if (readAlarmRequestDto.getStartDateTime() != null) {
                var startDateUTC = readAlarmRequestDto.getStartDateTime().atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC")).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDateUTC));
            }
            // 최종적으로 생성된 조건을 반환
            return predicate;
        };
    }
}