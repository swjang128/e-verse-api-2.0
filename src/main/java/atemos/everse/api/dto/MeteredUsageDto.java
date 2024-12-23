package atemos.everse.api.dto;

import atemos.everse.api.entity.MeteredUsage;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * MeteredUsage 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class MeteredUsageDto {
    /**
     * 새로운 서비스 사용 내역를 생성하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateMeteredUsage {
        /**
         * Company ID를 나타냅니다.
         * - 반드시 입력해야 하며, 1 이상의 자연수가 들어갑니다.
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 서비스 사용 내역의 기준 날짜입니다.
         * 해당 일을 기준으로 API Call 개수, 스토리지 사용 용량, IoT 설비 설치 개수, AI 예측 서비스 사용 여부 컬럼의 데이터가 담깁니다.
         * - YYYY-MM-DD로 들어갑니다.
         */
        @Schema(description = "기준 날짜", defaultValue = "2024-07")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "usageDate는 반드시 YYYY-MM-DD 형식이어야 합니다.")
        private LocalDate usageDate;
        /**
         * 유료 API Call 횟수입니다.
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @Schema(description = "유료 API Call 횟수", defaultValue = "248")
        @PositiveOrZero
        private Long apiCallCount;
        /**
         * IoT 설비 설치 개수
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @Schema(description = "IoT 설비 설치 개수", defaultValue = "8")
        @PositiveOrZero
        private Integer iotInstallationCount;
    }

    /**
     * MeteredUsage 정보를 조회할 때 요청할 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMeteredUsageRequest {
        /**
         * MeteredUsage ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private List<Long> meteredUsageId;
        /**
         * Company ID를 나타냅니다.
         * - 예: 1
         */
        @Positive
        private Long companyId;
        /**
         * 서비스 사용 내역을 조회할 연도와 월입니다.
         * 조회하는 연도와 월에 해당하는 일자의 API Call 개수, 스토리지 사용 용량, IoT 설비 설치 개수, AI 예측 서비스 사용 여부 컬럼의 데이터를 담습니다.
         * - YYYY-MM로 들어갑니다.
         */
        private LocalDate usageMonth;
        /**
         * 유료 API Call 횟수의 최소값입니다.
         * - 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Long minimumApiCallCount;
        /**
         * 유료 API Call 횟수의 최대값입니다.
         * - 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Long maximumApiCallCount;
        /**
         * 스토리지 사용 용량 (Byte 단위)의 최소값입니다.
         * - 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Long minimumStorageUsage;
        /**
         * 스토리지 사용 용량 (Byte 단위)의 최소값입니다.
         * - 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Long maximumStorageUsage;
        /**
         * IoT 설비 설치 개수의 최소값
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Integer minimumIotInstallationCount;
        /**
         * IoT 설비 설치 개수의 최대값
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @PositiveOrZero
        private Integer maximumIotInstallationCount;
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
     * MeteredUsage 정보를 조회할 때 응답으로 반환되는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadMeteredUsageResponse {
        /**
         * MeteredUsage ID를 나타냅니다.
         * - 예: 1
         */
        private Long meteredUsageId;
        /**
         * 업체 ID
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체명
         * - 예: "아테모스"
         */
        private String companyName;
        /**
         * 서비스 사용 내역의 기준 날짜입니다.
         * 해당 일을 기준으로 API Call 개수, 스토리지 사용 용량, IoT 설비 설치 개수, AI 예측 서비스 사용 여부 컬럼의 데이터가 담깁니다.
         * - 예: 2024-08-15
         */
        private LocalDate usageDate;
        /**
         * 유료 API Call 횟수입니다.
         * - 예: 12
         */
        private Long apiCallCount;
        /**
         * IoT 설비 설치 개수
         * - 예: 4
         */
        private Integer iotInstallationCount;
        /**
         * MeteredUsage 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * MeteredUsage 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;

        /**
         * MeteredUsage 엔티티를 기반으로 DTO를 생성합니다.
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param meteredUsage MeteredUsage 엔티티 객체
         */
        public ReadMeteredUsageResponse(MeteredUsage meteredUsage, ZoneId zoneId) {
            this.meteredUsageId = meteredUsage.getId();
            this.companyId = meteredUsage.getCompany().getId();
            this.companyName = meteredUsage.getCompany().getName();
            this.usageDate = meteredUsage.getUsageDate();
            this.apiCallCount = meteredUsage.getApiCallCount();
            this.iotInstallationCount = meteredUsage.getIotInstallationCount();
            this.createdDate = meteredUsage.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = meteredUsage.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 서비스 사용 내역 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadMeteredUsagePageResponse {
        /**
         * 서비스 사용 내역 목록
         */
        private List<MeteredUsageDto.ReadMeteredUsageResponse> meteredUsageList;
        /**
         * 데이터베이스 스토리지 사용량
         */
        private Long storageUsage;
        /**
         * 전체 row 개수
         */
        private long totalElements;
        /**
         * 전체 페이지 수
         */
        private int totalPages;
    }

    /**
     * MeteredUsage 설정을 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateMeteredUsage {
        /**
         * Company ID를 나타냅니다.
         * - 반드시 입력해야 하며, 1 이상의 자연수가 들어갑니다.
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 서비스 사용 내역의 기준 날짜입니다.
         * 해당 일을 기준으로 API Call 개수, 스토리지 사용 용량, IoT 설비 설치 개수, AI 예측 서비스 사용 여부 컬럼의 데이터가 담깁니다.
         * - YYYY-MM-DD로 들어갑니다.
         */
        @Schema(description = "기준 날짜", defaultValue = "2024-07")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "usageDate는 반드시 YYYY-MM-DD 형식이어야 합니다.")
        private LocalDate usageDate;
        /**
         * 유료 API Call 횟수입니다.
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @Schema(description = "유료 API Call 횟수", defaultValue = "248")
        @PositiveOrZero
        private Long apiCallCount;
        /**
         * IoT 설비 설치 개수
         * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
         */
        @Schema(description = "IoT 설비 설치 개수", defaultValue = "8")
        @PositiveOrZero
        private Integer iotInstallationCount;
    }
}