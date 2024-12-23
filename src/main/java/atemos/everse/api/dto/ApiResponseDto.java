package atemos.everse.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * API 응답을 표준화하기 위한 데이터 전송 객체(DTO)입니다.
 */
@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto {
    /**
     * 응답 상태 코드
     * - 200: 성공
     * - 400: 잘못된 요청
     * - 500: 서버 오류 등
     */
    private Integer status;
    /**
     * 응답 메시지
     * - 성공 시: "요청이 성공적으로 처리되었습니다."
     * - 오류 시: 오류의 상세 설명
     */
    private String message;
    /**
     * 응답 데이터
     * - 요청 처리 결과로 반환되는 데이터 객체
     * - 예를 들어, 조회된 데이터나 성공 여부를 포함할 수 있음
     */
    private Object data;
}