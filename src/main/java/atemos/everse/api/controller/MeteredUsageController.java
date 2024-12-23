package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.MeteredUsageDto;
import atemos.everse.api.service.MeteredUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 서비스 사용 요금 API 컨트롤러.
 * 이 클래스는 서비스 사용 요금 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/metered-usage")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "서비스 사용 요금 API", description = "서비스 사용 요금 API 모음")
public class MeteredUsageController {
    private final ApiResponseManager apiResponseManager;
    private final MeteredUsageService meteredUsageService;

    /**
     * 서비스 사용 요금 조회 API.
     * 조건에 맞는 서비스 사용 요금 정보를 조회합니다.
     *
     * @param meteredUsageId 서비스 사용 요금 ID
     * @param companyId 업체 ID
     * @param usageMonth 서비스 사용 정보을 조회할 연도와 월
     * @param minimumApiCallCount 유료 API Call 횟수의 최소값
     * @param maximumApiCallCount 유료 API Call 횟수의 최대값
     * @param minimumStorageUsage 스토리지 사용 용량 (Byte 단위)의 최소값
     * @param maximumStorageUsage 스토리지 사용 용량 (Byte 단위)의 최대값
     * @param minimumIotInstallationCount IoT 설비 설치 개수의 최소값
     * @param maximumIotInstallationCount IoT 설비 설치 개수의 최대값
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 서비스 사용 요금 목록
     */
    @Operation(summary = "조건에 맞는 서비스 사용 요금 조회", description = "조건에 맞는 서비스 사용 요금 정보를 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "서비스 사용 요금 ID") @RequestParam(required = false) List<Long> meteredUsageId,
            @Parameter(description = "서비스 사용 정보을 조회할 연도와 월") @RequestParam(required = false) LocalDate usageMonth,
            @Parameter(description = "유료 API Call 횟수의 최소값") @RequestParam(required = false) Long minimumApiCallCount,
            @Parameter(description = "유료 API Call 횟수의 최대값") @RequestParam(required = false) Long maximumApiCallCount,
            @Parameter(description = "스토리지 사용 용량 (Byte 단위)의 최소값") @RequestParam(required = false) Long minimumStorageUsage,
            @Parameter(description = "스토리지 사용 용량 (Byte 단위)의 최대값") @RequestParam(required = false) Long maximumStorageUsage,
            @Parameter(description = "IoT 설비 설치 개수의 최소값") @RequestParam(required = false) Integer minimumIotInstallationCount,
            @Parameter(description = "IoT 설비 설치 개수의 최대값") @RequestParam(required = false) Integer maximumIotInstallationCount,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        // 서비스 사용 요금 조회 목록 및 페이징 객체 리턴
        return apiResponseManager.success(meteredUsageService.read(
                MeteredUsageDto.ReadMeteredUsageRequest.builder()
                        .meteredUsageId(meteredUsageId)
                        .companyId(companyId)
                        .usageMonth(usageMonth)
                        .minimumApiCallCount(minimumApiCallCount)
                        .maximumApiCallCount(maximumApiCallCount)
                        .minimumStorageUsage(minimumStorageUsage)
                        .maximumStorageUsage(maximumStorageUsage)
                        .minimumIotInstallationCount(minimumIotInstallationCount)
                        .maximumIotInstallationCount(maximumIotInstallationCount)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 서비스 사용 요금 수정 API.
     * 특정 서비스 사용 요금 ID에 대한 서비스 사용 요금 정보를 수정합니다.
     *
     * @param meteredUsageId 수정할 서비스 사용 요금 ID
     * @param updateMeteredUsageDto 수정할 서비스 사용 요금 정보
     * @return 수정한 서비스 사용 요금 정보
     */
    @Operation(summary = "서비스 사용 요금 수정", description = "서비스 사용 요금 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{meteredUsageId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "서비스 사용 요금 ID") @PathVariable Long meteredUsageId,
            @Valid @RequestBody MeteredUsageDto.UpdateMeteredUsage updateMeteredUsageDto
    ) {
        return apiResponseManager.success(meteredUsageService.update(meteredUsageId, updateMeteredUsageDto));
    }

    /**
     * 서비스 사용 요금 삭제 API.
     * 특정 서비스 사용 요금 ID에 대한 서비스 사용 요금 정보를 삭제합니다.
     *
     * @param meteredUsageId 삭제할 서비스 사용 요금 ID
     * @return OK 상태
     */
    @Operation(summary = "서비스 사용 요금 삭제", description = "서비스 사용 요금 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{meteredUsageId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "서비스 사용 요금 ID") @PathVariable Long meteredUsageId
    ) {
        meteredUsageService.delete(meteredUsageId);
        return apiResponseManager.ok();
    }
}