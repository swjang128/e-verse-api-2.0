package atemos.everse.api.config;

import atemos.everse.api.domain.SampleData;
import atemos.everse.api.entity.Member;
import atemos.everse.api.repository.BlacklistedTokenRepository;
import atemos.everse.api.service.AuthenticationServiceImpl;
import atemos.everse.api.service.MemberService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 요청 필터 클래스.
 * HTTP 요청에서 JWT 토큰을 추출하고 검증하여 Spring Security의 인증 컨텍스트를 설정합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final MemberService memberService;
    private final AuthenticationServiceImpl authenticationService;
    private final EncryptUtil encryptUtil;

    @Setter
    private UserDetailsService userDetailsService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    // Swagger UI와 같은 특정 경로는 필터링에서 제외
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/atemos/swagger-ui/**", "/atemos/v3/api-docs/**", "/atemos/swagger-resources/**",
            "/atemos/webjars/**", "/atemos/configuration/**", "/atemos/auth/login", "/atemos/auth/login/no-2fa",
            "/atemos/auth/2fa", "/atemos/auth/renew", "/atemos/auth/reset-password", "/atemos/auth/update-password",
            "/atemos/company/list", "/atemos/country/list");

    // Caffeine 캐시 설정
    private final Cache<String, Boolean> blacklistTokenCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Member> memberCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * HTTP 요청을 필터링하여 JWT 토큰을 검증하고, 인증 정보를 설정합니다.
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param chain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        // 예외 처리할 URI는 필터링을 하지 않고 체인으로 넘어감
        if (EXCLUDED_PATHS.stream().anyMatch(request.getRequestURI()::startsWith)) {
            chain.doFilter(request, response);
            return;
        }
        // JWT 토큰을 HTTP 요청 헤더에서 추출
        var token = jwtUtil.extractTokenFromRequest(request);
        // 로컬 프로파일인 경우, 토큰이 없으면 샘플 데이터를 사용하여 토큰 생성
        if ("local".equals(activeProfile) && token == null) {
            log.info("Local profile detected with no token. Generating test token.");
            // MemberInfo 데이터를 사용하여 클레임 생성
            Map<String, Object> claims = Map.of("role", "ADMIN");
            var subject = SampleData.Member.KOREA_ADMIN.getEmail();
            token = jwtUtil.generateAccessToken(claims, subject);
        }
        if (token != null) {
            // BlacklistedTokenRepository 캐시에서 먼저 확인 후 없으면 DB에서 조회
            Boolean isBlacklisted = blacklistTokenCache.getIfPresent(token);
            if (isBlacklisted == null) {
                isBlacklisted = blacklistedTokenRepository.existsByToken(token);
                blacklistTokenCache.put(token, isBlacklisted);
            }
            // 블랙리스트 토큰인 경우
            if (isBlacklisted) {
                log.warn("This token is blacklisted: {}", token);
                response.sendError(HttpStatus.FORBIDDEN.value(), "This token is blacklisted.");
                return;
            }
            // 토큰 검증
            if (jwtUtil.validateToken(token, false)) {
                var username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 사용자 정보를 캐시에서 먼저 조회 후 없으면 DB에서 조회
                    String encryptedUsername = encryptUtil.encrypt(username);
                    Member member = memberCache.getIfPresent(encryptedUsername);
                    if (member == null) {
                        member = memberService.loadMemberByEmail(username);
                        memberCache.put(encryptedUsername, member);
                    }
                    // 계정 상태 체크
                    authenticationService.validateAccountStatus(member);
                    // 권한 부여
                    var authorities = member.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .toList();
                    // 사용자 정보를 바탕으로 인증 객체 생성
                    var authenticationToken = new UsernamePasswordAuthenticationToken(username, token, authorities);
                    // SecurityContext에 인증 객체 설정
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        // 필터 체인 계속 진행
        chain.doFilter(request, response);
    }
}