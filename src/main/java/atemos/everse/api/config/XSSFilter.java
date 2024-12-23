package atemos.everse.api.config;

import atemos.everse.api.dto.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * XSSFilter는 클라이언트 요청에 포함된 XSS 공격 코드를 필터링하는 역할을 수행합니다.
 * 주로 JSON 데이터와 HTTP 요청 파라미터에서 XSS 공격을 방지합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class XSSFilter implements Filter {
    private final ApiResponseManager apiResponseManager;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 필터 초기화 메서드. 필터가 초기화될 때 필요한 처리를 여기에 작성할 수 있습니다.
     *
     * @param filterConfig 필터 구성 정보
     * @throws ServletException 필터 초기화 중 예외 발생 시
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 (필요시)
    }

    /**
     * 클라이언트의 요청을 필터링하여 XSS 공격을 방지하는 메인 메서드입니다.
     * JSON 형식의 요청 데이터를 처리하고, XSS 공격 코드가 포함된 필드를 필터링합니다.
     *
     * @param request  클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param chain    필터 체인
     * @throws IOException      입출력 예외 발생 시
     * @throws ServletException 서블릿 처리 중 예외 발생 시
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest &&
                "application/json".equalsIgnoreCase(request.getContentType())) {
            // HttpServletRequestWrapper를 사용해 요청 본문을 한 번 읽고 이를 캐시한 다음 이후에 다시 읽을 수 있도록 요청 본문을 재사용
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);
            // JSON 본문에서 XSS 필터링 적용
            String sanitizedJson = sanitizeXSSFromJson(wrappedRequest.getCachedBody());
            // 요청 본문과 XSS 필터링된 JSON 문자열의 공백을 제거
            String originalBody = wrappedRequest.getCachedBody().replaceAll("\\s+", "");
            String sanitizedBody = sanitizedJson.replaceAll("\\s+", "");
            // XSS 공격이 감지된 경우
            if (!sanitizedBody.equals(originalBody)) {
                // XSS 공격 감지 시 ApiResponseManager를 사용하여 응답 처리
                ResponseEntity<ApiResponseDto> errorResponse = apiResponseManager.error(
                        HttpStatus.BAD_REQUEST, "XSS attack detected."
                );
                // ApiResponseDto를 JSON으로 변환하여 응답
                String jsonResponse = objectMapper.writeValueAsString(errorResponse.getBody());
                // 응답 설정
                ((HttpServletResponse) response).setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(jsonResponse);
                return;
            }
            // 필터링된 JSON 데이터를 기반으로 새로운 요청 객체 생성
            wrappedRequest.setCachedBody(sanitizedJson.getBytes(StandardCharsets.UTF_8));
            chain.doFilter(wrappedRequest, response);
        } else {
            // JSON 데이터가 아닐 경우 그대로 필터 체인에 넘김
            chain.doFilter(request, response);
        }
    }

    /**
     * 필터 종료 처리 메서드. 필터가 종료될 때 필요한 처리를 여기에 작성할 수 있습니다.
     */
    @Override
    public void destroy() {
        // 필터 종료 처리 (필요시)
    }

    /**
     * JSON 본문에서 텍스트 필드를 XSS 공격으로부터 보호하기 위해 필터링하는 메서드입니다.
     * JSON 본문을 파싱하여 각 필드를 검사하고, XSS 패턴을 감지하여 제거합니다.
     *
     * @param json 클라이언트 요청에서 전달된 JSON 본문
     * @return 필터링된 JSON 문자열
     * @throws IOException JSON 파싱 중 오류 발생 시
     */
    private String sanitizeXSSFromJson(String json) throws IOException {
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(json);
        // 각 JSON 필드에 대해 필터링 수행
        rootNode.fields().forEachRemaining(entry -> {
            if (entry.getValue().isTextual()) {
                String sanitizedValue = sanitizeXSS(entry.getValue().asText());
                rootNode.put(entry.getKey(), sanitizedValue);
            }
        });
        // 필터링된 JSON 데이터를 문자열로 변환하여 반환
        return objectMapper.writeValueAsString(rootNode);
    }

    // XSS 방지용 정규 표현식 패턴 목록
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<(script|iframe|object|embed|applet|form|input|button|textarea).*?>", Pattern.CASE_INSENSITIVE), // 위험한 HTML 태그 필터링
            Pattern.compile("(javascript:|vbscript:|\\son\\w+)", Pattern.CASE_INSENSITIVE), // 자바스크립트 및 VB스크립트 이벤트 필터링
            Pattern.compile("src\\s*=\\s*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE) // src 속성 필터링
    };

    /**
     * 전달된 문자열에서 XSS 패턴을 검사하고, 해당 패턴이 감지되면 필터링하여 안전한 문자열로 변환합니다.
     *
     * @param value 필터링할 문자열 값
     * @return 필터링된 문자열 값
     */
    private String sanitizeXSS(String value) {
        if (value != null) {
            String originalValue = value;
            // XSS 방지 패턴을 적용하여 문자열 필터링
            for (Pattern scriptPattern : XSS_PATTERNS) {
                value = scriptPattern.matcher(value).replaceAll("");
            }
            // 필터링된 값이 원본 값과 다를 경우 경고 로그 출력
            if (!originalValue.equals(value)) {
                log.warn("XSS attack detected. Original: {}, Sanitized: {}", originalValue, value);
            }
        }
        return value;
    }

    /**
     * 요청 본문을 캐시하여 여러 번 읽을 수 있도록 하는 HttpServletRequestWrapper입니다.
     */
    @Setter
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = requestInputStream.readAllBytes();
        }

        public String getCachedBody() {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedBodyServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    /**
     * 캐시된 바이트 배열을 사용하여 ServletInputStream을 제공하는 클래스입니다.
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final InputStream inputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() == 0;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}