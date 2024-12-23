package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 스토리지 API 컨트롤러.
 * 이 클래스는 해당 업체가 사용 중인 데이터베이스 스토리지 조회 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "스토리지 API", description = "스토리지 API 모음")
public class StorageController {
    private final ApiResponseManager apiResponseManager;
    private final StorageService storageService;

    /**
     * 업체가 사용 중인 데이터베이스 사용량을 조회하는 API
     * @param companyId 조회할 업체의 ID
     * @return 업체가 사용 중인 테이블별 데이터 사용량 조회
     */
    @Operation(summary = "업체가 사용 중인 데이터베이스 사용량 조회",
            description = "업체가 사용 중인 데이터베이스 사용량을 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> getDataUsageByCompanyId(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId
    ) {
        // 업체가 사용 중인 테이블별 데이터 사용량 조회
        return apiResponseManager.success(storageService.getDataUsageByCompanyId(companyId));
    }
}