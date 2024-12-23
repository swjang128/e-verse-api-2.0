package atemos.everse.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챗봇과 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class ChatbotDto {
    /**
     * 챗봇 질문 정보가 담긴 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatbotRequest {
        /**
         * 업체 ID
         * - 양수값
         */
        @Positive
        @Schema(description = "업체 ID", example = "1")
        private Long companyId;
        /**
         * 사용자의 질문
         * - 빈 값일 수 없음
         * - 정규식을 통해 특정 형식의 질문만 허용
         */
        @NotBlank
        @Pattern(
                regexp = "\\d{4}년\\s*(0?[1-9]|1[0-2])월 에너지 사용량을 보여줘",
                message = "질문 형식이 올바르지 않습니다. 예: '2024년 09월 에너지 사용량을 보여줘'"
        )
        @Schema(description = "사용자의 질문", example = "2024년 09월 에너지 사용량을 보여줘")
        private String question;
    }

    /**
     * 챗봇 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatbotResponse {
        /**
         * 챗봇 응답 메시지
         */
        private String response;
    }
}