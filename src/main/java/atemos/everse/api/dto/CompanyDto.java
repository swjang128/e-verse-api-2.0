package atemos.everse.api.dto;

import atemos.everse.api.domain.CompanyType;
import atemos.everse.api.entity.Company;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 업체와 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class CompanyDto {
    /**
     * 신규 업체 등록을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCompany {
        /**
         * 업체명
         * - 예: "아테모스"
         * - 최소 1자, 최대 50자
         */
        @Schema(description = "업체명", defaultValue = "아테모스")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 업체가 속한 국가 ID
         * - 예: "1"
         */
        @Schema(description = "업체가 속한 국가 ID", defaultValue = "1")
        @Positive
        private Long countryId;
        /**
         * 업체 유형
         * - 예: "FEMS", "BEMS"
         */
        @Schema(description = "업체 유형", defaultValue = "FEMS")
        @Enumerated
        private CompanyType type;
        /**
         * 담당자 이메일
         * - 예: "atemos@atemos.co.kr"
         */
        @Schema(description = "담당자 이메일", defaultValue = "atemos@atemos.co.kr")
        @Email
        private String email;
        /**
         * 연락처
         * - 예: "01098765432"
         * - 9자리에서 11자리 숫자
         */
        @Schema(description = "연락처", defaultValue = "01098765432")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit tel number.")
        private String tel;
        /**
         * 팩스
         * - 예: "01079798282"
         * - 9자리에서 11자리 숫자
         */
        @Schema(description = "팩스", defaultValue = "01079798282")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit fax number.")
        private String fax;
        /**
         * 주소
         * - 예: "경기도 하남시 ****"
         */
        @Schema(description = "주소", defaultValue = "경기도 하남시 ****")
        private String address;
    }

    /**
     * 업체 정보 수정 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateCompany {
        /**
         * 업체명
         * - 예: "아테모스"
         * - 최소 1자, 최대 50자
         */
        @Schema(description = "업체명", defaultValue = "아테모스")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 업체가 속한 국가 ID
         * - 예: "1"
         */
        @Schema(description = "업체가 속한 국가 ID", defaultValue = "1")
        @Positive
        private Long countryId;
        /**
         * 업체 유형
         * - 예: "BEMS"
         */
        @Schema(description = "업체 유형", defaultValue = "BEMS")
        private CompanyType type;
        /**
         * 담당자 이메일
         * - 예: "atemos1@atemos.co.kr"
         */
        @Schema(description = "담당자 이메일", defaultValue = "atemos1@atemos.co.kr")
        @Email
        private String email;
        /**
         * 연락처
         * - 예: "01097532468"
         * - 9자리에서 11자리 숫자
         */
        @Schema(description = "연락처", defaultValue = "01097532468")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit tel number.")
        private String tel;
        /**
         * 팩스
         * - 예: "01077778888"
         * - 11자리 숫자
         */
        @Schema(description = "팩스", defaultValue = "01077778888")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit fax number.")
        private String fax;
        /**
         * 주소
         * - 예: "경기도 성남시 ****"
         */
        @Schema(description = "주소", defaultValue = "경기도 성남시 ****")
        private String address;
    }

    /**
     * 업체 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadCompanyRequest {
        /**
         * 업체 ID
         * - 양수값
         */
        @Positive
        private List<Long> companyId;
        /**
         * 업체가 속한 국가 ID
         * - 양수값
         */
        @Positive
        private List<Long> countryId;
        /**
         * 업체명
         * - 최대 50자
         */
        @Size(max = 50)
        private String name;
        /**
         * 업체 유형 리스트
         */
        private List<CompanyType> type;
        /**
         * 담당자 이메일
         * - 이메일 형식
         */
        @Email
        private String email;
        /**
         * 연락처
         * - 11자리 숫자
         */
        @Pattern(regexp = "\\d{11}")
        private String tel;
        /**
         * 팩스
         * - 11자리 숫자
         */
        @Pattern(regexp = "\\d{11}")
        private String fax;
        /**
         * 주소
         * - 최대 255자
         */
        @Size(max = 255)
        private String address;
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
     * 업체 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCompanyResponse {
        /**
         * 업체 ID
         */
        private Long companyId;
        /**
         * 업체가 속한 국가 ID
         */
        private Long countryId;
        /**
         * 업체가 속한 국가명
         */
        private String countryName;
        /**
         * 업체가 속한 국가의 언어 코드
         */
        private String languageCode;
        /**
         * 업체명
         */
        private String name;
        /**
         * 업체 유형
         * - 예: "FEMS", "BEMS"
         */
        private CompanyType type;
        /**
         * 담당자 이메일
         */
        private String email;
        /**
         * 연락처
         */
        private String tel;
        /**
         * 팩스
         */
        private String fax;
        /**
         * 주소
         */
        private String address;
        /**
         * 업체 생성일
         */
        private LocalDateTime createdDate;
        /**
         * 업체 수정일
         */
        private LocalDateTime modifiedDate;
        /**
         * Company 엔티티를 기반으로 DTO를 생성하는 생성자입니다.
         */
        public ReadCompanyResponse(Company company, ZoneId zoneId) {
            this.companyId = company.getId();
            this.countryId = company.getCountry().getId();
            this.countryName = company.getCountry().getName();
            this.name = company.getName();
            this.type = company.getType();
            this.email = company.getEmail();
            this.tel = company.getTel();
            this.fax = company.getFax();
            this.address = company.getAddress();
            this.createdDate = company.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = company.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 업체 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCompanyPageResponse {
        /**
         * 업체 목록
         */
        private List<CompanyDto.ReadCompanyResponse> companyList;
        /**
         * 전체 row 수
         */
        private long totalElements;
        /**
         * 전체 페이지 수
         */
        private int totalPages;
    }

    /**
     * 회원 가입 화면에서 가입이 가능한 업체 목록을 가져오는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCompanyListResponse {
        /**
         * 업체 목록
         */
        private List<CompanyDto.ReadCompanyResponse> companyList;
    }
}