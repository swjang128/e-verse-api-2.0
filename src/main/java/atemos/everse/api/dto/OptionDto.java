package atemos.everse.api.dto;

import atemos.everse.api.domain.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 시스템에서 제공하는 다양한 설정 옵션을 포함하는 데이터 전송 객체입니다.
 * 이 DTO는 로그인 관련 설정, 회원가입 설정, 데이터 검색 범위 설정 및 업체 사용 유형과 같은
 * 다양한 설정 정보를 반환하는 데 사용됩니다.
 */
public class OptionDto {
    @Schema(description = "설정 옵션에 대한 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionResponse {
        @Schema(description = "로그인 시 2Factor 인증이 적용되는 국가", example = "Korea")
        private String login2FactorCountry;

        @Schema(description = "로그인 시 2Factor 인증 발송 매체", example = "Email")
        private String login2FactorMethod;

        @Schema(description = "로그인 시 2Factor 인증 시간 제한 (분)", example = "3")
        private Long login2FactorTimeoutMinutes;

        @Schema(description = "사용자 등록 후 Email 인증 사용 여부", example = "N")
        private String signupEmailVerificationRequired;

        @Schema(description = "과거 데이터 검색 시 선택할 수 있는 최장 범위 (개월)", example = "6")
        private Long maxSearchPeriodMonths;

        @Schema(description = "업체에서 사용하는 시스템 유형", example = "FEMS/BEMS")
        private List<CompanyType> companyUsageType;
    }
}