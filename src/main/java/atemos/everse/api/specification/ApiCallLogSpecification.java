package atemos.everse.api.specification;

import atemos.everse.api.dto.ApiCallLogDto;
import atemos.everse.api.entity.ApiCallLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;

/**
 * ApiCallLogSpecification 클래스는 ApiCallLog 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 * 다양한 필터링 조건을 지원하며, 주어진 조건에 따라 API 호출 로그를 조회하는 데 사용됩니다.
 */
public class ApiCallLogSpecification {
    /**
     * 주어진 ApiCallLogDto.ReadApiCallLogRequest 객체를 기반으로 ApiCallLog 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readApiCallLogRequestDto API 호출 로그 조회 조건을 포함하는 데이터 전송 객체
     * @return 주어진 조건에 맞는 ApiCallLog 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<ApiCallLog> findWith(ApiCallLogDto.ReadApiCallLogRequest readApiCallLogRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 초기화
            var predicate = criteriaBuilder.conjunction();
            // ApiCallLog ID 조건 추가
            if (readApiCallLogRequestDto.getApiCallLogId() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readApiCallLogRequestDto.getApiCallLogId()));
            }
            // 업체 ID 조건 추가
            if (readApiCallLogRequestDto.getCompanyId() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("company").get("id"), readApiCallLogRequestDto.getCompanyId()));
            }
            // 상태 코드 조건 추가
            if (readApiCallLogRequestDto.getStatusCode() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("statusCode"), readApiCallLogRequestDto.getStatusCode()));
            }
            // 클라이언트 IP 조건 추가
            if (readApiCallLogRequestDto.getClientIp() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("clientIp"), readApiCallLogRequestDto.getClientIp()));
            }
            // HTTP 메서드 조건 추가
            if (readApiCallLogRequestDto.getHttpMethod() != null) {
                predicate = criteriaBuilder.and(predicate, root.get("httpMethod").in(readApiCallLogRequestDto.getHttpMethod()));
            }
            // 과금 여부 조건 추가
            if (readApiCallLogRequestDto.getIsCharge() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isCharge"), readApiCallLogRequestDto.getIsCharge()));
            }
            // API 호출 시작 날짜 조건 추가
            if (readApiCallLogRequestDto.getStartDate() != null) {
                var startInstant = readApiCallLogRequestDto.getStartDate().atZone(ZoneId.systemDefault()).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("requestTime"), startInstant));
            }
            // API 호출 종료 날짜 조건 추가
            if (readApiCallLogRequestDto.getEndDate() != null) {
                var endInstant = readApiCallLogRequestDto.getEndDate().atZone(ZoneId.systemDefault()).toInstant();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("requestTime"), endInstant));
            }
            return predicate;
        };
    }
}