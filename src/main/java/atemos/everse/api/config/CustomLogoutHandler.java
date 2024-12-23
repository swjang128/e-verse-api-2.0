package atemos.everse.api.config;

import atemos.everse.api.entity.BlacklistedToken;
import atemos.everse.api.repository.BlacklistedTokenRepository;
import atemos.everse.api.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그아웃 처리를 담당하는 컴포넌트입니다.
 * JWT 토큰을 블랙리스트에 추가하고, 관련된 리프레시 토큰을 삭제합니다.
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ApiLogComponent apiLogComponent;

    /**
     * 로그아웃 처리를 수행합니다. 요청에서 JWT 토큰을 추출하고, 해당 토큰을 블랙리스트에 추가합니다.
     *
     * @param request        JWT 토큰을 추출할 HttpServletRequest
     * @param response       현재 HTTP 응답 객체
     * @param authentication 현재 인증 정보를 담고 있는 Authentication 객체
     */
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var token = jwtUtil.extractTokenFromRequest(request);
        if (token != null) {
            // 블랙리스트에 이미 존재하는지 확인
            boolean alreadyBlacklisted = blacklistedTokenRepository.existsByToken(token);
            if (!alreadyBlacklisted) {
                // JWT 토큰을 블랙리스트에 추가
                blacklistedTokenRepository.save(
                        BlacklistedToken.builder()
                                .token(token)
                                .build());
            }
            // 관련된 리프레시 토큰 삭제
            refreshTokenRepository.deleteByUsername(jwtUtil.extractUsername(token));
            // 로그아웃 관련 인증 로그 기록
            apiLogComponent.saveAuthenticationLog(HttpServletResponse.SC_OK, request.getRequestURI().replaceFirst("/atemos", ""), null);
        }
    }
}
