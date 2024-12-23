package atemos.everse.api.specification;

import atemos.everse.api.dto.IotStatusHistoryDto;
import atemos.everse.api.entity.IotStatusHistory;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * IotStatusHistory 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class IotStatusHistorySpecification {
    /**
     * 주어진 IotHistoryDto.ReadIotHistoryRequest를 기반으로 IotStatusHistory 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readIotHistoryRequestDto IoT 히스토리 조회 조건을 포함하는 데이터 전송 객체
     * @param zoneId                   타임존 정보를 포함하는 ZoneId 객체
     * @return 조건에 맞는 IotStatusHistory 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<IotStatusHistory> findWith(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto, ZoneId zoneId) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성 (기본적으로는 모든 조건이 true인 conjunction)
            var predicate = criteriaBuilder.conjunction();
            // ID 조건 추가 (iotHistoryId가 존재하는 경우)
            if (readIotHistoryRequestDto.getIotHistoryId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readIotHistoryRequestDto.getIotHistoryId()));
            }
            // IoT ID 조건 추가 (iotId가 존재하는 경우)
            if (readIotHistoryRequestDto.getIotId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("iot").get("id").in(readIotHistoryRequestDto.getIotId()));
            }
            // 업체 ID 조건 추가 (companyId가 존재하는 경우)
            if (readIotHistoryRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("iot").get("company").get("id"), (readIotHistoryRequestDto.getCompanyId())));
            }
            // 시리얼 넘버 조건 추가 (serialNumber가 존재하는 경우)
            if (readIotHistoryRequestDto.getSerialNumber() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("iot").get("serialNumber"), "%" + readIotHistoryRequestDto.getSerialNumber() + "%"));
            }
            // 상태 조건 추가 (status 리스트가 존재하는 경우)
            if (readIotHistoryRequestDto.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(readIotHistoryRequestDto.getStatus()));
            }
            // 유형 조건 추가 (type 리스트가 존재하는 경우)
            if (readIotHistoryRequestDto.getType() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("iot").get("type").in(readIotHistoryRequestDto.getType()));
            }
            // 위치 조건 추가 (location이 존재하는 경우)
            if (readIotHistoryRequestDto.getLocation() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("iot").get("location"), "%" + readIotHistoryRequestDto.getLocation() + "%"));
            }
            // 최소 설비 사용량 조건 추가 (minimumFacilityUsage가 존재하는 경우)
            if (readIotHistoryRequestDto.getMinimumFacilityUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("facilityUsage"), readIotHistoryRequestDto.getMinimumFacilityUsage()));
            }
            // 최대 설비 사용량 조건 추가 (maximumFacilityUsage가 존재하는 경우)
            if (readIotHistoryRequestDto.getMaximumFacilityUsage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("facilityUsage"), readIotHistoryRequestDto.getMaximumFacilityUsage()));
            }
            // 최소 가격 조건 추가 (minimumPrice가 존재하는 경우)
            if (readIotHistoryRequestDto.getMinimumPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("iot").get("price"), readIotHistoryRequestDto.getMinimumPrice()));
            }
            // 최대 가격 조건 추가 (maximumPrice가 존재하는 경우)
            if (readIotHistoryRequestDto.getMaximumPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("iot").get("price"), readIotHistoryRequestDto.getMaximumPrice()));
            }
            // 날짜 필터링 추가 (startDate와 endDate가 존재하는 경우)
            if (readIotHistoryRequestDto.getStartDate() != null || readIotHistoryRequestDto.getEndDate() != null) {
                var startOfDay = readIotHistoryRequestDto.getStartDate() != null
                        ? ZonedDateTime.of(readIotHistoryRequestDto.getStartDate().atStartOfDay(), zoneId).toInstant()
                        : null;
                var endOfDay = readIotHistoryRequestDto.getEndDate() != null
                        ? ZonedDateTime.of(readIotHistoryRequestDto.getEndDate().atTime(23, 59, 59), zoneId).toInstant()
                        : null;
                if (startOfDay != null && endOfDay != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("createdDate"), startOfDay, endOfDay));
                } else if (startOfDay != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startOfDay));
                } else if (endOfDay != null) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endOfDay));
                }
            }
            return predicate;
        };
    }
}