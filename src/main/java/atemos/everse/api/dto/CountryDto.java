package atemos.everse.api.dto;

import atemos.everse.api.entity.Country;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Country 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class CountryDto {
    /**
     * 새로운 국가 정보를 생성하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateCountry {
        /**
         * 국가의 이름입니다.
         * 예시: "Korea", "USA", "Thailand", "Vietnam".
         */
        @Schema(description = "국가명", defaultValue = "Korea")
        private String name;
        /**
         * 국가의 언어 코드를 나타냅니다.
         * 유일키 속성을 갖습니다.
         * 예시: "ko-KR", "en-US", "th-TH", "vi-VN".
         */
        @Schema(description = "국가의 언어 코드", defaultValue = "ko-KR")
        private String languageCode;
        /**
         * 국가의 타임존입니다.
         * 예시: "Asia/Seoul", "America/New_York".
         */
        @Schema(description = "국가의 타임존", defaultValue = "Asia/Seoul")
        private String timeZone;
    }

    /**
     * Country 정보를 조회할 때 요청할 DTO입니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadCountryRequest {
        /**
         * Country ID를 나타냅니다.
         * - 예: 1
         */
        @Schema(description = "국가 ID", example = "1")
        @Positive
        private List<Long> countryId;
        /**
         * 국가의 이름입니다.
         * 예시: "Korea", "USA", "Thailand", "Vietnam".
         */
        @Schema(description = "국가명", example = "Korea")
        @Size(max = 20)
        private String name;
        /**
         * 국가의 언어 코드를 나타냅니다.
         * 유일키 속성을 갖습니다.
         * 예시: "ko-KR", "en-US", "th-TH", "vi-VN".
         */
        @Schema(description = "국가의 언어 코드", defaultValue = "ko-KR")
        private String languageCode;
        /**
         * 국가의 타임존입니다.
         * 예시: "Asia/Seoul", "America/New_York".
         */
        @Schema(description = "국가의 타임존", example = "Asia/Seoul")
        private String timeZone;
        /**
         * 페이지 번호를 나타냅니다. 0 이상의 정수를 갖습니다.
         * 페이지 번호 + 1이 페이지 번호가 됩니다. (ex. 0 = 1페이지)
         * - 예: 0
         */
        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;
        /**
         * 페이지당 row의 개수를 나타냅니다. 1 이상의 자연수를 갖습니다.
         */
        @Schema(description = "페이지 당 데이터 개수", example = "10")
        @Positive
        private Integer size;
    }

    /**
     * Country 정보를 조회할 때 응답으로 반환되는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCountryResponse {
        /**
         * Country ID를 나타냅니다.
         * - 예: 1
         */
        private Long countryId;
        /**
         * 국가의 이름을 나타냅니다.
         * - 예: "Korea"
         */        
        private String name;
        /**
         * 국가의 언어 코드를 나타냅니다.
         * 유일키 속성을 갖습니다.
         * 예시: "ko-KR", "en-US", "th-TH", "vi-VN".
         */
        private String languageCode;
        /**
         * 국가의 타임존입니다.
         * 예시: "Asia/Seoul", "America/New_York".
         */
        private String timeZone;
        /**
         * Country 엔티티를 기반으로 DTO를 생성합니다.
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param country Country 엔티티 객체
         */
        public ReadCountryResponse(Country country) {
            this.countryId = country.getId();
            this.name = country.getName();
            this.languageCode = country.getLanguageCode();
            this.timeZone = country.getTimeZone();
        }
    }

    /**
     * 국가 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCountryPageResponse {
        /**
         * 업체 목록
         */
        private List<CountryDto.ReadCountryResponse> countryList;
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
     * Country 정보를 조회할 때 응답으로 반환되는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAllCountryResponse {
        /**
         * Country ID를 나타냅니다.
         * - 예: 1
         */
        private Long countryId;
        /**
         * 국가의 이름을 나타냅니다.
         * - 예: "Korea"
         */
        private String name;
        /**
         * 국가의 타임존입니다.
         * 예시: "Asia/Seoul", "America/New_York".
         */
        private String timeZone;
        /**
         * Country 엔티티를 기반으로 DTO를 생성합니다.
         * - 엔티티 객체를 DTO로 변환합니다.
         * @param country Country 엔티티 객체
         */
        public ReadAllCountryResponse(Country country) {
            this.countryId = country.getId();
            this.name = country.getName();
            this.timeZone = country.getTimeZone();
        }
    }

    /**
     * 국가 목록을 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCountryMapResponse {
        /**
         * 국가 정보 맵 (ISO 코드가 키)
         */
        private Map<String, ReadAllCountryResponse> countryMap;
    }

    /**
     * Country 설정을 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateCountry {
        /**
         * 국가의 이름입니다.
         * 예시: "Korea", "USA", "Thailand", "Vietnam".
         */
        @Schema(description = "국가명", defaultValue = "Korea")
        private String name;
        /**
         * 국가의 언어 코드를 나타냅니다.
         * 유일키 속성을 갖습니다.
         * 예시: "ko-KR", "en-US", "th-TH", "vi-VN".
         */
        @Schema(description = "국가의 언어 코드", defaultValue = "ko-KR")
        private String languageCode;
        /**
         * 국가의 타임존입니다.
         * 예시: "Asia/Seoul", "America/New_York".
         */
        @Schema(description = "국가의 타임존", defaultValue = "Asia/Seoul")
        private String timeZone;
    }
}