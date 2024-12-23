package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.ChatbotDto;
import atemos.everse.api.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * 챗봇 API 컨트롤러.
 * 이 클래스는 시연용 챗봇 관리와 관련된 API 엔드포인트를 정의합니다.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/chatbot")
@Tag(name = "챗봇 API", description = "챗봇 API 모음")
public class ChatbotController {
    private final ApiResponseManager apiResponseManager;
    private final ChatbotService chatbotService;

    /**
     * 시연용 챗봇 엔드포인트.
     * 사용자의 질문을 받아서 챗봇 응답을 반환합니다.
     *
     * @param request 챗봇 요청 DTO
     * @return 챗봇 응답 DTO
     */
    @Operation(summary = "시연용 챗봇 기능 호출", description = "사용자의 질문을 받아 챗봇 응답을 반환합니다.")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> chatbot(
            @Valid @RequestBody ChatbotDto.ChatbotRequest request) {
        ChatbotDto.ChatbotResponse response = chatbotService.chatbot(request);
        return apiResponseManager.success(response);
    }
}