package atemos.everse.api.service;

import atemos.everse.api.dto.ChatbotDto;

/**
 * ChatbotService는 시연용 챗봇과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface ChatbotService {
    /**
     * 사용자의 질문을 처리하여 응답을 생성합니다.
     *
     * @param request 챗봇 요청 DTO
     * @return 챗봇 응답 DTO
     */
    ChatbotDto.ChatbotResponse chatbot(ChatbotDto.ChatbotRequest request);
}