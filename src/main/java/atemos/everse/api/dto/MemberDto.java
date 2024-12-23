package atemos.everse.api.dto;

import atemos.everse.api.domain.CompanyType;
import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.MemberStatus;
import atemos.everse.api.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * 사용자 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
@RequiredArgsConstructor
public class MemberDto {
    /**
     * 새로운 사용자 생성을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateMember {
        /**
         * 사용자의 이름을 나타냅니다.
         * - 예: "아테모스"
         * - 길이는 1자 이상 50자 이하입니다.
         */
        @Schema(description = "이름", defaultValue = "아테모스")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 사용자의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         * - 유효한 이메일 형식이어야 합니다.
         */
        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
        /**
         * 사용자의 비밀번호를 나타냅니다.
         * - 영문 대소문자, 숫자, 특수문자가 포함된 최소 8자 이상, 최대 32자 이하의 비밀번호입니다.
         * - 예: "P@ssw0rd"
         */
        @Schema(description = "비밀번호", example = "Atemos1234!")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,16}$",
                message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;
        /**
         * 사용자가 소속된 업체의 ID를 나타냅니다.
         * - 예: 1
         * - 양수이어야 합니다.
         */
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;
        /**
         * 사용자의 권한을 나타냅니다.
         * - 예: ADMIN
         */
        @Schema(description = "권한", example = "ADMIN")
        @Enumerated(EnumType.STRING)
        private MemberRole role;
        /**
         * 사용자의 연락처를 나타냅니다.
         * - 9자리에서 11자리 숫자 형식이어야 합니다.
         * - 예: "01012349876"
         */
        @Schema(description = "연락처", example = "01012349876")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String phone;
        /**
         * 사용자의 상태를 나타냅니다.
         * - 예: ACTIVE, INACTIVE, LOCKED, SUSPENDED, DELETED, PASSWORD_RESET
         * - 길이는 14자 이하입니다.
         */
        @Size(max = 14)
        @Enumerated(EnumType.STRING)
        private MemberStatus status;
    }

    /**
     * 기존 사용자 정보를 업데이트하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    public static class UpdateMember {
        /**
         * 사용자의 이름을 나타냅니다.
         * - 예: "아테모스2"
         * - 길이는 1자 이상 50자 이하입니다.
         */
        @Schema(description = "이름", example = "아테모스2")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 사용자의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         * - 유효한 이메일 형식이어야 합니다.
         */
        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
        /**
         * 사용자의 비밀번호를 나타냅니다.
         * - 영문 대소문자, 숫자, 특수문자가 포함된 최소 8자 이상, 최대 32자 이하의 비밀번호입니다.
         * - 예: "P@ssw0rd"
         */
        @Schema(description = "비밀번호", example = "Atemos1234!")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,16}$",
                message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;
        /**
         * 비밀번호가 틀린 횟수를 기록합니다.
         * - 기본값은 0으로 설정됩니다.
         * - 5회 이상 틀리면 Status를 LOCKED로 변경합니다.
         */
        @Schema(description = "비밀번호가 틀린 횟수", example = "0")
        @PositiveOrZero
        private Integer failedLoginAttempts;
        /**
         * 사용자이 소속된 업체의 ID를 나타냅니다.
         * - 예: 1
         * - 양수이어야 합니다.
         */
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;
        /**
         * 사용자의 권한을 나타냅니다.
         * - 예: MANAGER
         */
        @Schema(description = "권한", example = "ADMIN")
        private MemberRole role;
        /**
         * 사용자의 상태를 나타냅니다.
         * - 예: ACTIVE, INACTIVE, LOCKED, SUSPENDED, DELETED, PASSWORD_RESET
         * - 길이는 14자 이하입니다.
         */
        @Size(max = 14)
        @Enumerated(EnumType.STRING)
        private MemberStatus status;
        /**
         * 사용자의 연락처를 나타냅니다.
         * - 9자리에서 11자리 숫자 형식이어야 합니다.
         * - 예: "01045671234"
         */
        @Schema(description = "연락처", example = "01045671234")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String phone;
    }

    /**
     * 사용자 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMemberRequest {
        /**
         * 사용자의 ID를 나타냅니다.
         * - 예: 1
         * - 양수이어야 합니다.
         */
        @Positive
        private List<Long> memberId;
        /**
         * 업체 ID를 나타냅니다.
         * - 예: 1
         * - 양수이어야 합니다.
         */
        @Positive
        private List<Long> companyId;
        /**
         * 사용자의 이름을 나타냅니다.
         * - 예: "아테모스"
         * - 길이는 30자 이하입니다.
         */
        @Size(max = 30)
        private String name;
        /**
         * 사용자의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
        /**
         * 사용자의 연락처를 나타냅니다.
         * - 9자리에서 11자리 숫자 형식이어야 합니다.
         * - 예: "01012349876"
         */
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String phone;
        /**
         * 사용자의 권한을 나타냅니다.
         * - 예: ADMIN, MANAGER, USER
         * - 길이는 7자 이하입니다.
         */
        private List<MemberRole> role;
        /**
         * 사용자의 상태를 나타냅니다.
         * - 예: ACTIVE, INACTIVE, LOCKED, SUSPENDED, DELETED, PASSWORD_RESET
         * - 길이는 14자 이하입니다.
         */
        private List<MemberStatus> status;
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
        /**
         * 데이터 마스킹 여부에 대한 값입니다.
         */
        private Boolean masking;
    }

    /**
     * 사용자 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadMemberResponse {
        /**
         * 사용자 ID를 나타냅니다.
         * - 예: 1
         */
        private Long memberId;
        /**
         * 업체 ID를 나타냅니다.
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체명을 나타냅니다.
         * - 예: "아테모스"
         */
        private String companyName;
        /**
         * 업체 유형을 나타냅니다.
         * - 예: CORPORATION, SME 등
         */
        private CompanyType companyType;
        /**
         * 사용자의 권한을 나타냅니다.
         * - 예: ADMIN, MANAGER, USER
         */
        private MemberRole role;
        /**
         * 사용자의 이름을 나타냅니다.
         * - 예: "아테모스"
         */
        private String name;
        /**
         * 사용자의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        private String email;
        /**
         * 사용자의 연락처를 나타냅니다.
         * - 11자리 숫자 형식이어야 합니다.
         * - 예: "01012349876"
         */
        private String phone;
        /**
         * 사용자의 상태를 나타냅니다.
         * - 예: ACTIVE, INACTIVE, LOCKED, SUSPENDED, DELETED, PASSWORD_RESET
         */
        private MemberStatus status;
        /**
         * 사용자가 접근 가능한 메뉴 ID 목록을 나타냅니다.
         * - 예: [1,2,3]
         */
        private Set<Long> accessibleMenuIds;
        /**
         * 데이터 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * 데이터 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;
        /**
         * Member 엔티티와 접근 가능한 메뉴 ID를 기반으로 DTO를 생성하는 생성자입니다.
         */
        public ReadMemberResponse(Member member, Set<Long> accessibleMenuIds, ZoneId zoneId) {
            this.memberId = member.getId();
            this.companyId = member.getCompany().getId();
            this.companyName = member.getCompany().getName();
            this.companyType = member.getCompany().getType();
            this.role = member.getRole();
            this.name = member.getName();
            this.email = member.getEmail();
            this.phone = member.getPhone();
            this.status = member.getStatus();
            this.accessibleMenuIds = accessibleMenuIds;
            this.createdDate = member.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = member.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 사용자 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadMemberPageResponse {
        /**
         * 사용자 목록
         */
        private List<MemberDto.ReadMemberResponse> memberList;
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
     * Everse 사용자 정보를 반환하는 DTO입니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EverseUserInfo {
        /**
         * 사용자 ID를 나타냅니다.
         * - 예: 1
         */
        private Long memberId;
        /**
         * 사용자의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        private String email;
        /**
         * 사용자의 이름을 나타냅니다.
         * - 예: "아테모스"
         */
        private String name;
        /**
         * 사용자의 연락처를 나타냅니다.
         * - 11자리 숫자 형식이어야 합니다.
         * - 예: "01012349876"
         */
        private String phone;
        /**
         * 사용자의 권한을 나타냅니다.
         * - 예: ADMIN, MANAGER, USER
         */
        private MemberRole role;
        /**
         * 사용자이 소속된 업체의 ID를 나타냅니다.
         * - 예: 1
         */
        private Long companyId;
        /**
         * 업체명을 나타냅니다.
         * - 예: "아테모스"
         */
        private String companyName;
        /**
         * 업체 유형을 나타냅니다.
         * - 예: CORPORATION, SME 등
         */
        private CompanyType companyType;
        /**
         * 사용자가 접근 가능한 메뉴 ID 목록을 나타냅니다.
         * - 예: [1, 2, 3]
         */
        private Set<Long> accessibleMenuIds;
    }

    /**
     * 2차 인증 번호를 발송하기위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthCodeRequest {
        /**
         * 로그인할 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        @Schema(description = "로그인할 이메일", defaultValue = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
    }

    /**
     * 로그인 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        /**
         * 로그인할 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        @Schema(description = "로그인할 이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
        /**
         * 로그인할 비밀번호를 나타냅니다.
         * - 영문 대소문자, 숫자, 특수문자가 포함된 최소 8자 이상, 최대 32자 이하의 비밀번호입니다.
         * - 예: "Atemos1234!"
         */
        @Schema(description = "비밀번호", example = "Atemos1234!")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,16}$",
                message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;
        /**
         * 로그인할 사용자가 속한 업체의 국가 ID를 나타냅니다.
         * - 1 이상의 자연수입니다.
         * - 예: 1
         */
        @Schema(description = "로그인할 사용자가 속한 업체의 국가 ID를 나타냅니다.", example = "1")
        @Positive
        private Long countryId;
        /**
         * 2차 인증 번호입니다.
         * - 6자리의 무작위 숫자입니다.
         */
        @Schema(description = "2차 인증 번호입니다.")
        @Pattern(regexp = "^\\d{6}$",
                message = "The authentication code must be exactly 6 digits.")
        private String authCode;
    }

    /**
     * 로그인 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponse {
        /**
         * Access Token
         * - JWT 형식
         */
        @Schema(description = "Access Token")
        private String accessToken;
        /**
         * Refresh Token
         * - JWT 형식
         */
        @Schema(description = "Refresh Token")
        private String refreshToken;
    }

    /**
     * 비밀번호 초기화 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResetPassword {
        /**
         * 비밀번호를 초기화할 계정의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        @Schema(description = "비밀번호를 초기화할 계정", example = "atemos@atemos.co.kr")
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
    }

    /**
     * 비밀번호 변경 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePassword {
        /**
         * 비밀번호를 변경할 계정의 이메일을 나타냅니다.
         * - 예: "atemos@atemos.co.kr"
         */
        @Schema(description = "비밀번호를 변경할 계정", example = "atemos@atemos.co.kr")
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Must be a valid email format.")
        private String email;
        /**
         * 현재 비밀번호를 나타냅니다.
         * - 영문 대소문자, 숫자, 특수문자가 포함된 최소 8자 이상, 최대 32자 이하의 비밀번호입니다.
         * - 예: "Atemos1234!"
         */
        @Schema(description = "기존 비밀번호", example = "Atemos1234!")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,16}$",
                message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;
        /**
         * 새로운 비밀번호를 나타냅니다.
         * - 영문 대소문자, 숫자, 특수문자가 포함된 최소 8자 이상, 최대 32자 이하의 비밀번호입니다.
         * - 예: "Atemos1234!@"
         */
        @Schema(description = "신규 비밀번호", example = "Atemos1234!@")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,16}$",
                message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String newPassword;
    }
}