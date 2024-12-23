package atemos.everse.api.specification;

import atemos.everse.api.dto.IotDto;
import atemos.everse.api.entity.Iot;
import org.springframework.data.jpa.domain.Specification;

/**
 * Iot 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class IotSpecification {
    /**
     * 주어진 IotDto.ReadIotRequest를 기반으로 Iot 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readIotRequestDto IoT 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Iot 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Iot> findWith(IotDto.ReadIotRequest readIotRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // ID 조건 추가
            if (readIotRequestDto.getIotId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readIotRequestDto.getIotId()));
            }
            // 업체 ID 조건 추가
            if (readIotRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readIotRequestDto.getCompanyId()));
            }
            // 시리얼 넘버 조건 추가
            if (readIotRequestDto.getSerialNumber() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("serialNumber"), "%" + readIotRequestDto.getSerialNumber() + "%"));
            }
            // 상태 조건 추가
            if (readIotRequestDto.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(readIotRequestDto.getStatus()));
            }
            // 유형 조건 추가
            if (readIotRequestDto.getType() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("type").in(readIotRequestDto.getType()));
            }
            // 설비 위치 조건 추가
            if (readIotRequestDto.getLocation() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("location"), "%" + readIotRequestDto.getLocation() + "%"));
            }
            // 최소 가격 조건 추가
            if (readIotRequestDto.getMinimumPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("price"), readIotRequestDto.getMinimumPrice()));
            }
            // 최대 가격 조건 추가
            if (readIotRequestDto.getMaximumPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("price"), readIotRequestDto.getMaximumPrice()));
            }
            return predicate;
        };
    }
}