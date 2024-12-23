package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 인증/인가 처리 API 컨트롤러.
 * 이 클래스는 사용자 인증 및 인가와 관련된 API 엔드포인트를 정의합니다.
 * 로그인, 사용자 정보 조회, 로그아웃, 비밀번호 초기화 및 변경, 웰컴 이메일 발송 및 2차 인증 관련 기능 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "사용자 인증/인가 처리 관리 API", description = "사용자 인증/인가 처리 API 모음")
public class AuthenticationController {
    private final ApiResponseManager apiResponseManager;
    private final AuthenticationService authenticationService;

    /**
     * 2차 인증 번호 발송 API.
     * 사용자가 로그인 후 2단계 인증을 위한 인증 번호를 이메일로 발송합니다.
     *
     * @param authCodeRequestDto 2차 인증 번호를 발송할 이메일을 포함한 DTO 객체
     * @return 인증 번호 발송 결과
     */
    @Operation(summary = "2차 인증 번호 발송", description = "로그인 후 2차 인증을 위한 인증 번호를 이메일로 발송하는 API")
    @PostMapping("/2fa")
    public ResponseEntity<ApiResponseDto> sendTwoFactorAuthCode(
            @Valid @RequestBody MemberDto.AuthCodeRequest authCodeRequestDto
    ) {
        authenticationService.sendTwoFactorAuthCode(authCodeRequestDto);
        return apiResponseManager.success("Please check the verification code sent to " + authCodeRequestDto.getEmail() + ".");
    }

    /**
     * 사용자 정보와 2차 인증 번호를 확인 후 로그인 처리하는 API.
     * 로그인 요청을 처리합니다.
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @return 인증 성공 시 JWT Access Token과 Refresh Token을 포함한 응답 또는 실패 결과
     */
    @Operation(summary = "로그인", description = "로그인 API")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(
            @Valid @RequestBody MemberDto.LoginRequest loginRequestDto,
            HttpServletRequest request
    ) {
        return apiResponseManager.login(authenticationService.login(loginRequestDto, true), request.getRequestURI());
    }

    /**
     * 사용자 정보를 확인 후 로그인 처리하는 API.(2차 인증 Skip)
     * 로그인 요청을 처리합니다.(2차 인증 Skip)
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @return 인증 성공 시 JWT Access Token과 Refresh Token을 포함한 응답 또는 실패 결과
     */
    @Operation(summary = "로그인(2차 인증 Skip)", description = "로그인 API(2차 인증 Skip)")
    @PostMapping("/login/no-2fa")
    public ResponseEntity<ApiResponseDto> loginWithout2fa(
            @Valid @RequestBody MemberDto.LoginRequest loginRequestDto,
            HttpServletRequest request
    ) {
        return apiResponseManager.login(authenticationService.login(loginRequestDto, false), request.getRequestURI());
    }

    /**
     * Refresh Token 재발급 API.
     * 만료된 Access Token을 Refresh Token을 이용해 재발급합니다.
     *
     * @param refreshToken 클라이언트에서 제공된 Refresh Token
     * @return 새로운 Access Token
     */
    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용해 새로운 Access Token을 발급하는 API")
    @PostMapping("/renew")
    public ResponseEntity<ApiResponseDto> refreshToken(
            @Parameter(description = "Refresh Token") @RequestParam String refreshToken,
            HttpServletRequest request
    ) {
        // Refresh Token의 유효성 확인 및 Access Token 재발급
        return apiResponseManager.success(authenticationService.renewAccessToken(refreshToken, request));
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API.
     * JWT 토큰을 이용하여 현재 로그인한 사용자 정보를 조회합니다.
     *
     * @return 사용자 정보
     */
    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "JWT 토큰을 이용하여 현재 로그인한 사용자 정보를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDto> getCurrentUserInfo() {
        return apiResponseManager.success(authenticationService.getCurrentUserInfo());
    }

    /**
     * 현재 로그인한 사용자의 권한, 접근 가능한 메뉴를 조회
     * JWT 토큰을 이용하여 현재 로그인한 사용자의 권한, 접근 가능한 메뉴를 조회
     *
     * @return 사용자 정보
     */
    @Operation(summary = "현재 로그인한 사용자의 권한, 접근 가능한 메뉴를 조회", description = "JWT 토큰을 이용하여 현재 로그인한 사용자의 권한, 접근 가능한 메뉴를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/role")
    public ResponseEntity<ApiResponseDto> getCurrentUserRole() {
        return apiResponseManager.success(authenticationService.getCurrentUserRole());
    }

    /**
     * 로그아웃 API.
     * 로그아웃 후 JWT 토큰을 블랙리스트에 추가합니다.
     * 실제 로그아웃 처리는 SecurityConfig에 의해 CustomLogoutHandler에서 처리합니다.
     *
     * @return 로그아웃 결과
     */
    @Operation(summary = "로그아웃", description = "로그아웃 후 JWT 토큰을 블랙리스트에 추가하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto> logout() {
        return apiResponseManager.ok();
    }

    /**
     * 비밀번호 초기화 API.
     * 사용자의 비밀번호를 초기화합니다.
     *
     * @param resetPasswordDto 비밀번호 초기화 요청 데이터
     * @return 초기화 결과
     */
    @Operation(summary = "비밀번호 초기화", description = "사용자의 비밀번호를 초기화하는 API")
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(
            @Valid @RequestBody MemberDto.ResetPassword resetPasswordDto
    ) {
        authenticationService.resetPassword(resetPasswordDto);
        return apiResponseManager.ok();
    }

    /**
     * 비밀번호 변경 API.
     * 기존 계정 정보를 확인 후 새로운 비밀번호로 변경합니다.
     *
     * @param updatePasswordDto 비밀번호 변경 요청 데이터
     * @return 변경 결과
     */
    @Operation(summary = "비밀번호 변경", description = "기존 계정 정보를 확인 후 새로운 비밀번호로 변경하는 API")
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponseDto> updatePassword(
            @Valid @RequestBody MemberDto.UpdatePassword updatePasswordDto
    ) {
        authenticationService.updatePassword(updatePasswordDto);
        return apiResponseManager.ok();
    }
}