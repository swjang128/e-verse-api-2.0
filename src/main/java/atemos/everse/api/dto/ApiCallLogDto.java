package atemos.everse.api.dto;

import atemos.everse.api.entity.ApiCallLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * API 호출 로그와 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class ApiCallLogDto {
    /**
     * API 호출 로그를 조회할 때 사용할 요청 정보를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadApiCallLogRequest {
        /**
         * 로그 ID (양수값이어야 함)
         */
        @Positive
        private List<Long> apiCallLogId;
        /**
         * 업체 ID (양수값이어야 함)
         */
        @Positive
        private Long companyId;
        /**
         * HTTP 상태 코드 (양수값이어야 함)
         */
        @Positive
        private Integer statusCode;
        /**
         * 클라이언트 IP 주소
         */
        private String clientIp;
        /**
         * HTTP 메서드 (예: [GET,POST,PUT,DELETE])
         */
        private List<String> httpMethod;
        /**
         * 과금 여부
         */
        private Boolean isCharge;
        /**
         * 조회 시작일시
         */
        private LocalDateTime startDate;
        /**
         * 조회 종료일시
         */
        private LocalDateTime endDate;
        /**
         * 특정 요청 날짜
         */
        private LocalDate targetDate;
        /**
         * 페이지 번호를 나타냅니다. 0 이상의 정수를 갖습니다.
         * 페이지 번호 + 1이 페이지 번호가 됩니다. (ex. 0 = 1페이지)
         * - 예: 0
         */
        @PositiveOrZero
        private Integer page;
        /**
         * 페이지당 row의 개수를 나타냅니다. 1 이상의 자연수를 갖습니다.
         */
        @Positive
        private Integer size;
    }

    /**
     * 조회된 API 호출 로그의 상세 정보를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadApiCallLogResponse {
        /**
         * 로그 ID
         */
        private Long apiCallLogId;
        /**
         * 사용자 ID
         */
        private Long memberId;
        /**
         * 사용자 이메일
         */
        private String memberEmail;
        /**
         * 업체 ID
         */
        private Long companyId;
        /**
         * 업체 이름
         */
        private String companyName;
        /**
         * API 경로
         */
        private String apiPath;
        /**
         * HTTP 상태 코드
         */
        private Integer statusCode;
        /**
         * 클라이언트 IP 주소
         */
        private String clientIp;
        /**
         * HTTP 메서드 (예: GET, POST, PUT, DELETE)
         */
        private String httpMethod;
        /**
         * 추가 메타데이터
         */
        private String metaData;
        /**
         * 요금 여부
         */
        private Boolean isCharge;
        /**
         * 요청 시각
         */
        private LocalDateTime requestTime;
        /**
         * ApiCallLog 엔티티를 기반으로 ReadApiCallLogResponse를 생성하는 생성자
         *
         * @param apiCallLog ApiCallLog 엔티티 객체
         */
        public ReadApiCallLogResponse(ApiCallLog apiCallLog, ZoneId zoneId) {
            this.apiCallLogId = apiCallLog.getId();
            if (apiCallLog.getMember() != null) {
                this.memberId = apiCallLog.getMember().getId();
                this.memberEmail = apiCallLog.getMember().getEmail();
            }
            if (apiCallLog.getCompany() != null) {
                this.companyId = apiCallLog.getCompany().getId();
                this.companyName = apiCallLog.getCompany().getName();
            }
            this.apiPath = apiCallLog.getApiPath();
            this.httpMethod = apiCallLog.getHttpMethod();
            this.metaData = apiCallLog.getMetaData();
            this.requestTime = apiCallLog.getRequestTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.statusCode = apiCallLog.getStatusCode();
            this.clientIp = apiCallLog.getClientIp();
            this.isCharge = apiCallLog.getIsCharge();
        }
    }

    /**
     * API 호출 로그 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadApiCallLogPageResponse {
        /**
         * API 호출 로그 목록
         */
        private List<ReadApiCallLogResponse> apiCallLogList;
        /**
         * 전체 row 개수
         */
        private long apiCallLogTotalElements;
        /**
         * 전체 페이지 수
         */
        private int apiCallLogTotalPages;
    }

    /**
     * 유료 API 호출 횟수를 조회할 때 사용할 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadChargeableApiCallCountResponse {
        /**
         * 허용된 API 호출 횟수
         */
        private int allowedApiCalls;
        /**
         * 일별 유료 API 호출 횟수
         */
        private long dailyChargeableApiCalls;
        /**
         * 월별 유료 API 호출 횟수
         */
        private long monthlyChargeableApiCalls;
    }

    /**
     * API 호출 로그를 삭제할 때 사용할 요청 정보를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeleteApiCallLogRequest {
        /**
         * API 요청 시각 (yyyymmdd 형식으로 입력해야 함)
         */
        @Schema(description = "API 요청 시각", defaultValue = "20240603")
        @Pattern(regexp = "\\d{8}", message = "requestTime은 반드시 yyyymmdd 형태로 입력해야 합니다.")
        private String requestTime;
    }

    /**
     * LogUsageData 클래스는 특정 업체의 특정 날짜에 대한 API 호출 로그 데이터를 나타내는 데이터 클래스입니다.
     */
    @AllArgsConstructor
    @Getter
    public static class LogUsageData {
        private Long companyId;      // 업체 ID
        private Instant createdDate; // 생성 날짜
        private Long count;          // 호출 수

        /**
         * createdDate를 주어진 ZoneId를 사용해 LocalDate로 변환하는 메서드
         *
         * @param zoneId 타임존 정보
         * @return 변환된 LocalDate
         */
        public LocalDate getCreatedDateAsLocalDate(ZoneId zoneId) {
            return createdDate != null ? createdDate.atZone(zoneId).toLocalDate() : null;
        }
    }
}