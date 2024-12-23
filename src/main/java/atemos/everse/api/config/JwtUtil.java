package atemos.everse.api.config;

import atemos.everse.api.entity.Member;
import atemos.everse.api.repository.BlacklistedTokenRepository;
import atemos.everse.api.repository.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스입니다.
 * Access Token과 Refresh Token을 생성할 수 있으며, 토큰의 유효성을 확인하는 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final MemberRepository memberRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final EncryptUtil encryptUtil;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;
    /**
     * Secret Key를 생성하고 HMAC-SHA256 알고리즘을 사용합니다.
     */
    @PostConstruct
    public void initJwtUtil() {
        // secret이 null이거나 길이가 충분하지 않은지 확인
        if (secret == null || secret.length() < 32) { // 32 bytes = 256 bits
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Refresh Token의 만료 기간을 반환합니다.
     *
     * @return Refresh Token 만료 기간 (밀리초)
     */
    public long getRefreshTokenExpirationInMillis() {
        return refreshTokenExpiration;
    }

    /**
     * Access Token을 생성합니다.
     *
     * @param claims 토큰에 포함할 클레임 (추가 데이터)
     * @param subject 토큰의 대상 사용자 (주로 사용자 이름)
     * @return 생성된 Access Token
     */
    public String generateAccessToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * @param subject 토큰의 대상 사용자 (주로 사용자 이름)
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 사용자 이름을 추출합니다.
     * 만료된 토큰이라도 사용자 이름을 추출할 수 있도록 처리합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출된 사용자 이름 또는 null
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 클레임에서 사용자 이름 추출
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Access Token 또는 Refresh Token의 유효성을 검증합니다.
     * 만료된 토큰은 별도로 처리할 수 있도록 옵션을 추가했습니다.
     *
     * @param token        JWT 토큰
     * @param allowExpired 만료된 토큰도 검증할지 여부
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token, boolean allowExpired) {
        // 블랙리스트 토큰에 있는지 확인
        if (blacklistedTokenRepository.existsByToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This token has been blacklisted.");
        }
        try {
            // 토큰을 파싱하고 유효성 검증을 수행합니다.
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            // 토큰이 유효하고 만료되지 않았으므로 true 반환
            return true;
        } catch (ExpiredJwtException e) {
            if (allowExpired) {
                log.info("Expired token is allowed for processing: {}", e.getMessage());
                // 만료된 토큰이지만 허용하는 경우 true 반환
                return true;
            }
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT token for user {}: {}", extractUsername(token), e.getMessage());
            return false;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation error: " + e.getMessage(), e);
        }
    }

    /**
     * HttpServletRequest에서 토큰 정보를 가져옵니다.
     *
     * @param request HttpServletRequest 정보
     * @return JWT 토큰
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        // 헤더에서 토큰 추출
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        // 쿠키에서 토큰 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 현재 접속한 사용자 정보를 가져오기
     * @return 현재 접속한 사용자 정보
     */
    public Member getCurrentMember() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        // principal이 Member가 아닌 경우, 적절히 처리
        if (principal instanceof Member member) {
            return member;
        } else if (principal instanceof String username) {
            // username을 이메일 혹은 사용자 이름으로 간주하고 DB에서 Member를 조회합니다.
            return memberRepository.findByEmail(encryptUtil.encrypt(username))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No such member."));
        } else {
            log.error("Unexpected principal type: {}", principal.getClass().getName());
            throw new ClassCastException("Principal is not of expected type Member or String");
        }
    }
}