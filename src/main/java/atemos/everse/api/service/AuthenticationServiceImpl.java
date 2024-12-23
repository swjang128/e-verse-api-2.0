package atemos.everse.api.service;

import atemos.everse.api.config.EncryptUtil;
import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.MemberStatus;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.entity.*;
import atemos.everse.api.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static atemos.everse.api.domain.MemberStatus.ACTIVE;
import static atemos.everse.api.domain.MemberStatus.PASSWORD_RESET;

/**
 * AuthenticationServiceImpl는 사용자 인증 및 권한 관련 로직을 처리하는 서비스 클래스입니다.
 * 사용자 로그인, 비밀번호 재설정, 토큰 발급 등의 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MenuRepository menuRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EncryptUtil encryptUtil;
    private final ObjectMapper objectMapper;

    /**
     * 2차 인증을 위한 인증 번호를 발송합니다.
     *
     * @param authCodeRequestDto 2차 인증 번호를 발송할 이메일이 포함된 DTO 객체
     */
    @Override
    public void sendTwoFactorAuthCode(MemberDto.AuthCodeRequest authCodeRequestDto) {
        // 사용자가 존재하는지 조회
        var member = memberRepository.findByEmail(encryptUtil.encrypt(authCodeRequestDto.getEmail().trim()))
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        // 최근 3분 내에 발송된 건을 조회(3분 내에 기존 발송한 건이 있다면 429 Exception 처리)
        Optional<TwoFactorAuth> latestRequest = twoFactorAuthRepository
                .findFirstByMemberAndCreatedDateAfterOrderByCreatedDateDesc(member, Instant.now().minus(Duration.ofMinutes(3)));
        // 인증 번호를 3분 이내에 다시 호출했을 때 429 응답
        if (latestRequest.isPresent()) {
            Instant blockedUntil = latestRequest.get().getCreatedDate().plus(Duration.ofMinutes(3));
            if (Instant.now().isBefore(blockedUntil)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You have requested authentication codes too frequently.");
            }
        }
        // 인증 번호 생성 (6자리 숫자)
        var authCode = generateRandomNumericCode(6);
        // TwoFactorAuth 엔티티 저장
        var twoFactorAuth = TwoFactorAuth.builder()
                .member(member)
                .authCode(authCode)
                .build();
        twoFactorAuthRepository.save(twoFactorAuth);
        // 이메일로 인증 번호 발송
        try {
            var subject = "[E-Verse] Two-Factor Authentication Code";
            var message = String.format("""
                <html><body>
                <p>Hello, %s.</p>
                <br>
                <p>Your two-factor authentication code is as follows:</p>
                <br>
                <p style="font-size: 1.5em; color: blue; font-weight: bold;">%s</p>
                <br>
                <p>Thank you.</p>
                </body></html>
                """, encryptUtil.decrypt(member.getName()), authCode);
            emailService.sendEmail(encryptUtil.decrypt(member.getEmail()), subject, message);
        } catch (MessagingException e) {
            log.error("Failed to send 2FA email: {}", e.getMessage());
        }
    }

    /**
     * 사용자 로그인 처리
     *
     * @param loginRequestDto 로그인 요청 정보를 포함하는 데이터 전송 객체
     * @param require2FA 2차 인증 필요 여부
     * @return Access Token 및 Refresh Token이 담긴 Map 객체
     */
    @Override
    public MemberDto.LoginResponse login(MemberDto.LoginRequest loginRequestDto, boolean require2FA) {
        // 사용자 및 2차 인증 번호 검증
        var member = authenticateAndValidateMember(loginRequestDto, require2FA);
        // Access Token, Refresh Token 리턴
        return MemberDto.LoginResponse.builder()
                .accessToken(generateTokensAndSaveRefreshToken(member))
                .refreshToken(jwtUtil.generateRefreshToken(encryptUtil.decrypt(member.getEmail())))
                .build();
    }

    /**
     * 현재 사용자의 정보 조회
     *
     * @return 현재 사용자의 정보가 담긴 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public MemberDto.EverseUserInfo getCurrentUserInfo() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        var token = (String) authentication.getCredentials();
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token is missing or empty.");
        }
        jwtUtil.validateToken(token, false);
        return buildUserInfoForJwtClaims(memberRepository.findByEmail(encryptUtil.encrypt(jwtUtil.extractUsername(token)))
                .orElseThrow(() -> new EntityNotFoundException("No such member.")));
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인한 사용자의 권한, 접근 가능한 메뉴를 조회합니다.
     *
     * @return 사용자 정보 객체입니다. 사용자의 권한, 접근 가능한 메뉴를 포함합니다.
     */
    @Override
    public MemberDto.EverseUserInfo getCurrentUserRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        var token = (String) authentication.getCredentials();
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token is missing or empty.");
        }
        jwtUtil.validateToken(token, false);
        return buildUserRoleForJwtClaims(memberRepository.findByEmail(encryptUtil.encrypt(jwtUtil.extractUsername(token)))
                .orElseThrow(() -> new EntityNotFoundException("No such member.")));
    }

    /**
     * 비밀번호 초기화 처리
     *
     * @param resetPasswordDto 비밀번호 초기화를 위한 DTO
     */
    @Override
    @Transactional
    public void resetPassword(MemberDto.ResetPassword resetPasswordDto) {
        var member = memberRepository.findByEmail(encryptUtil.encrypt(resetPasswordDto.getEmail().trim()))
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        validateAccountStatus(member);
        var newPasswordLength = 8;
        var newPassword = generateRandomNumericCode(newPasswordLength);
        log.info("**** Reset Password: {}", newPassword);
        updatePasswordAndStatus(member, newPassword, PASSWORD_RESET);
        try {
            var subject = "[E-Verse] Password Reset Notification";
            var message = String.format("""
            <html><body>
            <p>Hello,</p>
            <br>
            <p>Your password has been successfully reset. Your new password is:</p>
            <br>
            <p style="font-size: 1.5em; color: blue; font-weight: bold;">%s</p>
            <br>
            <p>For security reasons, please make sure to log in and change your password immediately.</p>
            <br>
            <p>Thank you,</p>
            <p>The ATEMoS Team</p>
            </body></html>
        """, newPassword);
            emailService.sendEmail(encryptUtil.decrypt(member.getEmail()), subject, message);
        } catch (MessagingException e) {
            log.error("Failed to send reset password email: {}", e.getMessage());
        }
    }

    /**
     * 비밀번호 변경 처리
     *
     * @param updatePasswordDto 비밀번호 변경을 위한 DTO
     */
    @Override
    @Transactional
    public void updatePassword(MemberDto.UpdatePassword updatePasswordDto) {
        var member = memberRepository.findByEmail(encryptUtil.encrypt(updatePasswordDto.getEmail()))
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        if (member.getStatus() != PASSWORD_RESET) {
            validateAccountStatus(member);
        }
        if (!passwordEncoder.matches(updatePasswordDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("Your email or password does not match.");
        }
        updatePasswordAndStatus(member, updatePasswordDto.getNewPassword(), ACTIVE);
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발급
     * 만료된 Access Token이라도 새로운 Access Token을 발급하도록 처리
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 Access Token이 담긴 Map 객체
     */
    @Override
    @Transactional
    public MemberDto.LoginResponse renewAccessToken(String refreshToken, HttpServletRequest request) {
        // 만료되거나 잘못된 Refresh Token인지 확인
        var validToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.isExpired())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token."));
        // 해당 토큰으로 사용자 정보 조회
        var member = memberRepository.findByEmail(encryptUtil.encrypt(validToken.getUsername()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such member."));
        // 사용자 정보를 Map으로 변환
        Map<String, Object> claims = objectMapper.convertValue(buildUserInfoForJwtClaims(member), new TypeReference<>() {});
        // 새로운 Access Token를 생성하여 리턴
        return MemberDto.LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(claims, encryptUtil.decrypt(member.getEmail())))
                .build();
    }

    /**
     * 사용자 계정 상태 검증
     *
     * @param member 검증할 사용자 객체
     */
    public void validateAccountStatus(Member member) {
        switch (member.getStatus()) {
            case INACTIVE, SUSPENDED, DELETED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account is not active.");
            case LOCKED -> throw new ResponseStatusException(HttpStatus.LOCKED, "Your account is locked. Please reset your password.");
            case PASSWORD_RESET -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Your password has been reset. Please change your password before logging in.");
            case WITHDRAWN -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Your account is withdrawn.");
            case ACTIVE -> {}
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected account status.");
        }
    }

    /**
     * 사용자 인증 및 검증 처리
     *
     * @param loginRequestDto 사용자 인증에 필요한 데이터
     * @return 인증된 사용자 객체
     */
    private Member authenticateAndValidateMember(MemberDto.LoginRequest loginRequestDto, boolean require2FA) {
        // 사용자가 존재하는지 검증
        var member = memberRepository.findByEmail(encryptUtil.encrypt(loginRequestDto.getEmail().trim()))
                .orElseThrow(() -> new BadCredentialsException("Please check your email or password or country."));
        // 사용자 계정 상태 검증
        validateAccountStatus(member);
        // 비밀번호가 일치하지 않을 때
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            // 비밀번호를 틀린 횟수 1회 추가
            member.setFailedLoginAttempts(member.getFailedLoginAttempts() + 1);
            // 비밀번호를 틀린 횟수가 5회 이상일 경우 계정 LOCKED
            if (member.getFailedLoginAttempts() >= 5) {
                member.setStatus(MemberStatus.LOCKED);
                memberRepository.save(member);
                throw new ResponseStatusException(HttpStatus.LOCKED, "Your account is locked due to too many failed login attempts. please reset your password.");
            }
            memberRepository.saveAndFlush(member);
            throw new BadCredentialsException("Please check your email or password or country.");
        }
        // 사용자가 속한 업체의 국가 ID와 로그인에서 받아온 국가 ID가 일치하는지 확인
        if (!member.getCompany().getCountry().getId().equals(loginRequestDto.getCountryId())) {
            throw new BadCredentialsException("Please check your email or password or country.");
        }
        // 2차 인증이 필요한 경우에만 2차 인증 로직 수행
        if (require2FA) {
            // 인증 코드 유효 기간 설정 (분 단위)
            final int CODE_VALIDITY_MINUTES = 5;
            // 가장 최근에 발송된 검증되지 않은 인증 정보 가져오기
            var twoFactorAuth = twoFactorAuthRepository
                    .findFirstByMemberAndIsVerifiedFalseOrderByCreatedDateDesc(member)
                    .orElseThrow(() -> new EntityNotFoundException("No authentication code found. Please request a new code."));
            // 인증 코드의 유효 기간 확인 및 예외 처리
            Optional.of(twoFactorAuth)
                    .filter(auth -> Duration.between(auth.getCreatedDate(), Instant.now()).toMinutes() < CODE_VALIDITY_MINUTES)
                    .orElseThrow(() -> new BadCredentialsException("Authentication code has expired. Please request a new one."));
            // 인증 코드 검증 및 처리
            if (twoFactorAuth.getAuthCode().equals(loginRequestDto.getAuthCode().trim())) {
                twoFactorAuth.setVerified(true);
            } else {
                twoFactorAuth.setFailedAttempts(twoFactorAuth.getFailedAttempts() + 1);
                throw new BadCredentialsException("Please check the authentication code sent to %s.".formatted(encryptUtil.decrypt(member.getEmail())));
            }
            // 변경된 TwoFactorAuth 저장
            twoFactorAuthRepository.save(twoFactorAuth);
        }
        // 로그인에 성공하면 비밀번호 틀린 횟수를 0으로 초기화
        member.setFailedLoginAttempts(0);
        memberRepository.saveAndFlush(member);
        return member;
    }

    /**
     * Access Token 및 Refresh Token 생성 및 저장
     *
     * @param member 사용자 객체
     * @return 생성된 Access Token
     */
    private String generateTokensAndSaveRefreshToken(Member member) {
        // 사용자 정보 DTO를 Map으로 변환
        Map<String, Object> claims = objectMapper.convertValue(buildUserInfoForJwtClaims(member), new TypeReference<>() {});
        // 토큰 생성
        var decryptedEmail = encryptUtil.decrypt(member.getEmail());
        // Refresh Token 저장
        var refreshToken = RefreshToken.builder()
                .token(jwtUtil.generateRefreshToken(decryptedEmail))
                .username(decryptedEmail)
                .expiryDate(Instant.now()
                        .plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpirationInMillis())))
                .build();
        refreshTokenRepository.save(refreshToken);
        // 새로운 Access Token 생성 후 리턴
        return jwtUtil.generateAccessToken(claims, decryptedEmail);
    }

    /**
     * 사용자 비밀번호 및 상태 업데이트
     *
     * @param member 사용자 객체
     * @param newPassword 새 비밀번호
     * @param newStatus 새 상태
     */
    private void updatePasswordAndStatus(Member member, String newPassword, MemberStatus newStatus) {
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setStatus(newStatus);
        member.setFailedLoginAttempts(0);
        memberRepository.save(member);
    }

    /**
     * 랜덤 숫자 인증 코드 생성
     *
     * @param length 생성할 코드의 길이
     * @return 랜덤 생성된 숫자 문자열
     */
    private String generateRandomNumericCode(int length) {
        var digits = "0123456789";
        var random = new SecureRandom();
        var code = new char[length];
        for (int i = 0; i < length; i++) {
            code[i] = digits.charAt(random.nextInt(digits.length()));
        }
        return new String(code);
    }

    /**
     * 업체 접근 권한 검증
     *
     * @param entityCompanyId 접근하려는 엔티티의 업체 ID
     */
    public void validateCompanyAccess(Long entityCompanyId) {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var username = (principal instanceof String) ? (String) principal
                : ((org.springframework.security.core.userdetails.User) principal).getUsername();
        var currentUser = memberRepository.findByEmail(encryptUtil.encrypt(username))
                .map(this::buildUserInfoForJwtClaims)
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        if (!currentUser.getRole().equals(MemberRole.ADMIN) && !currentUser.getCompanyId().equals(entityCompanyId)) {
            throw new AccessDeniedException("You do not have permission to perform this operation on this company.");
        }
    }

    /**
     * 사용자 정보를 JWT Claims에 맞게 생성하는 메서드
     *
     * @param member 사용자 엔티티
     * @return 사용자 정보 DTO
     */
    private MemberDto.EverseUserInfo buildUserInfoForJwtClaims(Member member) {
        // 접근 가능한 메뉴를 필터링
        Set<Long> accessibleMenus = menuRepository.findAllByAccessibleRolesContains(member.getRole()).stream()
                .filter(menu -> {
                    if (member.getRole() == MemberRole.ADMIN) {
                        return true; // ADMIN 권한은 모든 메뉴 접근 가능
                    }
                    SubscriptionServiceList requiredSubscription = menu.getRequiredSubscription();
                    if (requiredSubscription == null) {
                        return true; // 구독이 필요 없는 메뉴
                    }
                    // 구독이 현재 유효한지 확인(해당 국가의 타임존에 맞게 조회)
                    Long count = subscriptionRepository.countValidSubscription(member.getCompany(), requiredSubscription, LocalDate.now(member.getCompany().getCountry().getZoneId()));
                    return count != null && count > 0;
                })
                .map(Menu::getId)
                .collect(Collectors.toSet());
        // 사용자 정보 DTO 생성
        return MemberDto.EverseUserInfo.builder()
                .memberId(member.getId())
                .email(encryptUtil.decrypt(member.getEmail()))
                .name(encryptUtil.decrypt(member.getName()))
                .phone(encryptUtil.decrypt(member.getPhone()))
                .role(member.getRole())
                .companyId(member.getCompany().getId())
                .companyName(member.getCompany().getName())
                .companyType(member.getCompany().getType())
                .accessibleMenuIds(accessibleMenus)
                .build();
    }
    
    /**
     * 사용자의 권한과 접근 가능한 메뉴를 JWT Claims에 맞게 생성하는 메서드
     *
     * @param member 사용자 엔티티
     * @return 사용자 정보 DTO
     */
    private MemberDto.EverseUserInfo buildUserRoleForJwtClaims(Member member) {
        // 접근 가능한 메뉴를 필터링
        Set<Long> accessibleMenus = menuRepository.findAllByAccessibleRolesContains(member.getRole()).stream()
                .filter(menu -> {
                    if (member.getRole() == MemberRole.ADMIN) {
                        return true; // ADMIN 권한은 모든 메뉴 접근 가능
                    }
                    SubscriptionServiceList requiredSubscription = menu.getRequiredSubscription();
                    if (requiredSubscription == null) {
                        return true; // 구독이 필요 없는 메뉴
                    }
                    // 구독이 현재 유효한지 확인(해당 국가의 타임존에 맞게 조회)
                    Long count = subscriptionRepository.countValidSubscription(member.getCompany(), requiredSubscription, LocalDate.now(member.getCompany().getCountry().getZoneId()));
                    return count != null && count > 0;
                })
                .map(Menu::getId)
                .collect(Collectors.toSet());
        // 사용자의 권한과 접근 가능한 메뉴가 담긴 DTO 생성 후 리턴
        return MemberDto.EverseUserInfo.builder()
                .role(member.getRole())
                .accessibleMenuIds(accessibleMenus)
                .build();
    }
}