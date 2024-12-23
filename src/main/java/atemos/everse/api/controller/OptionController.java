package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 설정 API 컨트롤러.
 * 이 클래스는 서비스 전반적인 설정 조회 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/option")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "설정 API", description = "설정 API 모음")
public class OptionController {
    private final ApiResponseManager apiResponseManager;
    private final OptionService optionService;

    /**
     * 현재 서비스의 전반적인 설정 상태를 조회합니다.
     * @return 현재 서비스의 전반적인 설정 상태
     */
    @Operation(summary = "현재 서비스의 전반적인 설정 상태를 조회",
            description = "현재 서비스의 전반적인 설정 상태를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read() {
        // 업체가 사용 중인 테이블별 데이터 사용량 조회
        return apiResponseManager.success(optionService.read());
    }
}