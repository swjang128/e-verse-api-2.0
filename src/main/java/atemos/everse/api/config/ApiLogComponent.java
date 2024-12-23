package atemos.everse.api.config;

import atemos.everse.api.entity.ApiCallLog;
import atemos.everse.api.entity.AuthenticationLog;
import atemos.everse.api.repository.ApiCallLogRepository;
import atemos.everse.api.repository.AuthenticationLogRepository;
import atemos.everse.api.repository.MemberRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * API 요청 및 인증 로그를 관리하는 컴포넌트 클래스.
 * 이 클래스는 API 호출과 관련된 로그를 데이터베이스에 기록하는 역할을 합니다.
 */
@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLogComponent {
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ApiCallLogRepository apiCallLogRepository;
    private final AuthenticationLogRepository authenticationLogRepository;
    private final HttpServletRequest request;
    private final EncryptUtil encryptUtil;

    /**
     * API 호출 또는 인증/인가 호출 로그를 분기하는 메서드.
     * 요청 URI에 따라 API 호출 로그 또는 인증 로그를 기록합니다.
     *
     * @param status HTTP 응답 상태 코드
     */
    public void logRequest(int status) {
        var requestUri = request.getRequestURI().replaceFirst("/atemos", "");
        if (requestUri.startsWith("/auth")) {
            // 인증 관련 요청일 경우 인증 로그 기록
            saveAuthenticationLog(status, requestUri, null);
        } else {
            // 일반 API 호출일 경우 API 호출 로그 기록
            saveApiCallLog(status, requestUri);
        }
    }

    /**
     * API 호출 로그를 기록하는 메서드.
     * 클라이언트 IP, HTTP 메서드, 요청 URI 등의 정보를 로깅합니다.
     *
     * @param status    HTTP 응답 상태 코드
     * @param requestUri 요청 URI
     */
    public void saveApiCallLog(int status, String requestUri) {
        // 로깅이 필요 없는 경우 early return
        if (isNoLoggingRequired()) return;
        // 과금 대상 여부 확인
        var isCharge = isChargeable();
        // 메타데이터 생성
        var metadata = createMetadata();
        // 로그 빌더 초기화
        var logBuilder = ApiCallLog.builder()
                .apiPath(requestUri)
                .httpMethod(request.getMethod())
                .requestTime(Instant.now())
                .statusCode(status)
                .clientIp(request.getRemoteAddr())
                .metaData(metadata)
                .isCharge(isCharge);
        // 요청에서 JWT 토큰을 추출하고, 사용자 정보를 추가
        var token = jwtUtil.extractTokenFromRequest(request);
        if (StringUtils.isNotEmpty(token)) {
            var email = jwtUtil.extractUsername(token);
            memberRepository.findByEmail(encryptUtil.encrypt(email))
                    .ifPresent(member -> {
                        logBuilder.member(member);
                        logBuilder.company(member.getCompany());
                    });
        }
        // 로그를 데이터베이스에 저장
        apiCallLogRepository.save(logBuilder.build());
        log.info("Called API: method={}, URI={}, status={}", request.getMethod(), requestUri, status);
    }

    /**
     * 인증/인가 로그를 기록하는 메서드.
     * 요청 URI와 상태 코드 등을 기반으로 인증 관련 로그를 기록합니다.
     *
     * @param status    HTTP 응답 상태 코드
     * @param requestUri 요청 URI
     * @param tokenMap  받아온 토큰 정보 또는 데이터
     */
    public void saveAuthenticationLog(int status, String requestUri, Object tokenMap) {
        // 인증 로그 빌더 초기화
        var authenticationLogBuilder = AuthenticationLog.builder()
                .apiPath(requestUri)
                .requestTime(Instant.now())
                .statusCode(status)
                .clientIp(request.getRemoteAddr())
                .metaData(createMetadata());
        // tokenMap이 존재할 경우 토큰 정보를 활용해 사용자 정보 추가
        if (tokenMap instanceof Map<?, ?> tokenData) {
            if (tokenData.containsKey("accessToken")) {
                var accessToken = (String) tokenData.get("accessToken");
                var email = jwtUtil.extractUsername(accessToken);
                memberRepository.findByEmail(encryptUtil.encrypt(email))
                        .ifPresent(member -> {
                            authenticationLogBuilder.member(member);
                            authenticationLogBuilder.company(member.getCompany());
                        });
            }
        } else {
            // tokenMap이 없을 경우, 요청에서 JWT 토큰을 추출
            var token = jwtUtil.extractTokenFromRequest(request);
            if (StringUtils.isNotEmpty(token)) {
                var email = jwtUtil.extractUsername(token);
                memberRepository.findByEmail(encryptUtil.encrypt(email))
                        .ifPresent(member -> {
                            authenticationLogBuilder.member(member);
                            authenticationLogBuilder.company(member.getCompany());
                        });
            }
        }
        // 인증 로그를 데이터베이스에 저장
        authenticationLogRepository.save(authenticationLogBuilder.build());
        log.info("Called Authentication API: method={}, URI={}, status={}", request.getMethod(), requestUri, status);
    }

    /**
     * 요청이 로깅이 필요 없는지 확인하는 메서드.
     * 특정 메서드에 @NoLogging 어노테이션이 있는 경우, 로깅을 생략합니다.
     *
     * @return 로깅이 필요 없는 경우 true, 그렇지 않으면 false
     */
    private boolean isNoLoggingRequired() {
        // 현재 요청을 처리하는 메서드에 @NoLogging 어노테이션이 있는지 확인
        return getHandlerMethod()
                .map(method -> method.isAnnotationPresent(NoLogging.class))
                .orElse(false);
    }

    /**
     * 요청이 과금 대상인지 확인하는 메서드.
     * 특정 메서드에 @Chargeable 어노테이션이 있는 경우, 과금 여부를 확인합니다.
     *
     * @return 과금 대상인 경우 true, 그렇지 않으면 false
     */
    private boolean isChargeable() {
        // 현재 요청을 처리하는 메서드에 @Chargeable 어노테이션이 있는지 확인
        return getHandlerMethod()
                .map(method -> method.isAnnotationPresent(Chargeable.class) && method.getAnnotation(Chargeable.class).value())
                .orElse(false);
    }

    /**
     * 메타데이터를 생성하는 메서드.
     * 사용자 정보, 요청 헤더 등을 포함하여 메타데이터 문자열을 생성합니다.
     *
     * @return 생성된 메타데이터 문자열
     */
    private String createMetadata() {
        // 사용자 이름을 가져옴. 인증되지 않은 사용자일 경우 "anonymous"로 설정
        var user = Optional.ofNullable(request.getUserPrincipal())
                .map(Principal::getName)
                .orElse("anonymous");
        // User-Agent 헤더 값을 가져옴
        var userAgent = request.getHeader("User-Agent");
        // 메타데이터 문자열 생성
        return "User: " + user + ", User-Agent: " + userAgent;
    }

    /**
     * 현재 요청을 처리하는 핸들러 메서드를 가져오는 메서드.
     * 요청을 처리하는 핸들러 메서드 정보를 Optional로 반환합니다.
     *
     * @return 핸들러 메서드를 Optional로 반환
     */
    private Optional<Method> getHandlerMethod() {
        // 요청의 핸들러 메서드를 가져옴
        return Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE))
                .filter(HandlerMethod.class::isInstance)
                .map(HandlerMethod.class::cast)
                .map(HandlerMethod::getMethod);
    }
}