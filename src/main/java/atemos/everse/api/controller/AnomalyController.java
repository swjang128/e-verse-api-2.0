package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.AnomalyDto;
import atemos.everse.api.service.AnomalyService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 이상탐지 API 컨트롤러.
 * 이 클래스는 이상탐지 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/anomaly")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "이상탐지 API", description = "이상탐지 API 모음")
public class AnomalyController {
    private final ApiResponseManager apiResponseManager;
    private final AnomalyService anomalyService;

    /**
     * 이상탐지 등록 API.
     * 새로운 이상탐지 정보를 등록합니다.
     *
     * @param createAnomalyDto 등록할 이상탐지 정보
     * @return 등록된 이상탐지 정보
     */
    @Operation(summary = "이상탐지 등록", description = "이상탐지를 등록하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @RequestBody AnomalyDto.CreateAnomaly createAnomalyDto
    ) {
        return apiResponseManager.success(anomalyService.create(createAnomalyDto));
    }

    /**
     * 이상탐지 조회 API.
     * 조건에 맞는 이상탐지 정보를 조회합니다.
     *
     * @param anomalyId 이상탐지 ID
     * @param companyId 업체 ID
     * @param minimumLowestHourlyEnergyUsage 이상탐지에 등록된 시간당 최저 에너지 사용량의 최소값
     * @param maximumLowestHourlyEnergyUsage 이상탐지에 등록된 시간당 최저 에너지 사용량의 최대값
     * @param minimumHighestHourlyEnergyUsage 이상탐지에 등록된 시간당 최고 에너지 사용량의 최소값
     * @param maximumHighestHourlyEnergyUsage 이상탐지에 등록된 시간당 최고 에너지 사용량의 최대값
     * @param available 활성화 여부
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 이상탐지 목록
     */
    @Operation(summary = "조건에 맞는 이상탐지 조회", description = "조건에 맞는 이상탐지 정보를 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable List<Long> companyId,
            @Parameter(description = "이상탐지 ID") @RequestParam(required = false) List<Long> anomalyId,
            @Parameter(description = "이상탐지에 등록된 시간당 최저 에너지 사용량의 최소값") @RequestParam(required = false) BigDecimal minimumLowestHourlyEnergyUsage,
            @Parameter(description = "이상탐지에 등록된 시간당 최저 에너지 사용량의 최대값") @RequestParam(required = false) BigDecimal maximumLowestHourlyEnergyUsage,
            @Parameter(description = "이상탐지에 등록된 시간당 최고 에너지 사용량의 최소값") @RequestParam(required = false) BigDecimal minimumHighestHourlyEnergyUsage,
            @Parameter(description = "이상탐지에 등록된 시간당 최고 에너지 사용량의 최대값") @RequestParam(required = false) BigDecimal maximumHighestHourlyEnergyUsage,
            @Parameter(description = "활성화 여부") @RequestParam(required = false) Boolean available,
            @Parameter(description = "페이지 번호") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(anomalyService.read(
                AnomalyDto.ReadAnomalyRequest.builder()
                        .anomalyId(anomalyId)
                        .companyId(companyId)
                        .minimumLowestHourlyEnergyUsage(minimumLowestHourlyEnergyUsage)
                        .maximumLowestHourlyEnergyUsage(maximumLowestHourlyEnergyUsage)
                        .minimumHighestHourlyEnergyUsage(minimumHighestHourlyEnergyUsage)
                        .maximumHighestHourlyEnergyUsage(maximumHighestHourlyEnergyUsage)
                        .available(available)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 이상탐지 수정 API.
     * 특정 이상탐지 ID에 대한 이상탐지 정보를 수정합니다.
     *
     * @param anomalyId 수정할 이상탐지 ID
     * @param updateAnomalyDto 수정할 이상탐지 정보
     * @return OK 상태
     */
    @Operation(summary = "이상탐지 수정", description = "이상탐지 정보를 수정하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PatchMapping("/{anomalyId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "이상탐지 ID", example = "1") @PathVariable Long anomalyId,
            @Valid @RequestBody AnomalyDto.UpdateAnomaly updateAnomalyDto
    ) {
        return apiResponseManager.success(anomalyService.update(anomalyId, updateAnomalyDto));
    }

    /**
     * 이상탐지 삭제 API.
     * 특정 이상탐지 ID에 대한 이상탐지 정보를 삭제합니다.
     *
     * @param anomalyId 삭제할 이상탐지 ID
     * @return OK 상태
     */
    @Operation(summary = "이상탐지 삭제", description = "이상탐지 정보를 삭제하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{anomalyId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "이상탐지 ID", example = "2") @PathVariable Long anomalyId
    ) {
        anomalyService.delete(anomalyId);
        return apiResponseManager.ok();
    }
}