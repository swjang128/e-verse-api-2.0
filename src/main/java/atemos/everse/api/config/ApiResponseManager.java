package atemos.everse.api.config;

import atemos.everse.api.dto.ApiResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * ApiResponseManager는 API 호출의 응답을 관리하는 클래스입니다.
 * 각 메서드는 다양한 상황에 맞는 응답을 생성하고, 로그를 기록합니다.
 */
@Slf4j
@Component
@AllArgsConstructor
public class ApiResponseManager {
    private final ApiLogComponent apiLogComponent;

    /**
     * API를 정상 호출하였고, 리턴할 데이터가 없는 경우의 응답을 생성합니다.
     *
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> ok() {
        logResponse(HttpStatus.OK);
        return buildResponse(HttpStatus.OK, null);
    }

    /**
     * API를 정상 호출하였고, 리턴할 데이터가 있는 경우의 응답을 생성합니다.
     *
     * @param data 리턴할 데이터
     * @return 상태 코드와 메시지, 데이터를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> success(Object data) {
        logResponse(HttpStatus.OK);
        return buildResponse(HttpStatus.OK, data);
    }

    /**
     * 인증/인가 API를 정상 호출하였고, 사용자 데이터를 함께 받습니다.
     *
     * @param data 사용자 데이터
     * @return 상태 코드와 메시지, 추가 정보를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> login(Object data, String requestUri) {
        logAuthentication(requestUri, data);
        return buildResponse(HttpStatus.OK, data);
    }

    /**
     * API 호출 후 기능 작동 중 에러가 발생한 경우의 응답을 생성합니다.
     *
     * @param status HTTP 상태 코드
     * @param message 에러 메시지
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> error(HttpStatus status, String message) {
        logResponse(status);
        return buildResponse(status, message);
    }

    /**
     * 공통 로깅 로직을 처리하는 메서드.
     *
     * @param status HTTP 상태 코드
     */
    private void logResponse(HttpStatus status) {
        apiLogComponent.logRequest(status.value());
    }

    /**
     * 인증/인가 로깅 로직을 처리하는 메서드.
     *
     * @param requestUri 인증 경로
     * @param data 사용자 데이터
     */
    private void logAuthentication(String requestUri, Object data) {
        apiLogComponent.saveAuthenticationLog(HttpStatus.OK.value(), requestUri.replaceFirst("/atemos", ""), data);
    }

    /**
     * 공통 응답 빌드를 처리하는 메서드.
     *
     * @param status HTTP 상태 코드
     * @param data   응답에 포함할 데이터
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    private ResponseEntity<ApiResponseDto> buildResponse(HttpStatus status, Object data) {
        return ResponseEntity.status(status)
                .body(ApiResponseDto.builder()
                        .status(status.value())
                        .message(status.getReasonPhrase())
                        .data(data)
                        .build());
    }
}