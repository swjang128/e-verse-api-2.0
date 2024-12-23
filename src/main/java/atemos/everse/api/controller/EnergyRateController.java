package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.EnergyRateDto;
import atemos.everse.api.service.EnergyRateService;
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
 * 에너지 요금 API 컨트롤러.
 * 이 클래스는 에너지 요금 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/rate")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "에너지 요금 API", description = "에너지 요금 API 모음")
public class EnergyRateController {
    private final ApiResponseManager apiResponseManager;
    private final EnergyRateService energyRateService;

    /**
     * 에너지 요금을 생성하는 API.
     *
     * @param createEnergyRateDto 생성할 에너지 요금의 정보가 담긴 DTO
     * @return 생성된 에너지 요금의 정보를 담은 응답 객체
     */
    @Operation(summary = "에너지 요금 생성", description = "에너지 요금을 생성하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody EnergyRateDto.CreateEnergyRate createEnergyRateDto
    ) {
        return apiResponseManager.success(energyRateService.create(createEnergyRateDto));
    }

    /**
     * 조건에 맞는 에너지 요금을 조회하는 API.
     *
     * @param energyRateId 에너지 요금 ID (옵션)
     * @param countryId 국가 ID (옵션)
     * @param minimumIndustrialRate 최소 산업용 전력 요금 (옵션)
     * @param maximumIndustrialRate 최대 산업용 전력 요금 (옵션)
     * @param minimumCommercialRate 최소 상업용 전력 요금 (옵션)
     * @param maximumCommercialRate 최대 상업용 전력 요금 (옵션)
     * @param minimumPeakMultiplier 최소 피크 시간대 요금 증감율 (옵션)
     * @param maximumPeakMultiplier 최대 피크 시간대 요금 증감율 (옵션)
     * @param minimumMidPeakMultiplier 최소 경피크 시간대 요금 증감율 (옵션)
     * @param maximumMidPeakMultiplier 최대 경피크 시간대 요금 증감율 (옵션)
     * @param minimumOffPeakMultiplier 최소 비피크(할인) 시간대 요금 증감율 (옵션)
     * @param maximumOffPeakMultiplier 최대 비피크(할인) 시간대 요금 증감율 (옵션)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 당 row 개수 (기본값: 10)
     * @return 조건에 맞는 에너지 요금 목록과 페이지 정보를 담은 응답 객체
     */
    @Operation(summary = "조건에 맞는 에너지 요금 조회", description = "조건에 맞는 에너지 요금을 조회하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "에너지 요금 ID") @RequestParam(required = false) List<Long> energyRateId,
            @Parameter(description = "국가 ID") @RequestParam(required = false) List<Long> countryId,
            @Parameter(description = "최소 산업용 전력 요금") @RequestParam(required = false) BigDecimal minimumIndustrialRate,
            @Parameter(description = "최대 산업용 전력 요금") @RequestParam(required = false) BigDecimal maximumIndustrialRate,
            @Parameter(description = "최소 상업용 전력 요금") @RequestParam(required = false) BigDecimal minimumCommercialRate,
            @Parameter(description = "최대 상업용 전력 요금") @RequestParam(required = false) BigDecimal maximumCommercialRate,
            @Parameter(description = "최소 피크 시간대 요금 증감율") @RequestParam(required = false) BigDecimal minimumPeakMultiplier,
            @Parameter(description = "최대 피크 시간대 요금 증감율") @RequestParam(required = false) BigDecimal maximumPeakMultiplier,
            @Parameter(description = "최소 경피크 시간대 요금 증감율") @RequestParam(required = false) BigDecimal minimumMidPeakMultiplier,
            @Parameter(description = "최대 경피크 시간대 요금 증감율") @RequestParam(required = false) BigDecimal maximumMidPeakMultiplier,
            @Parameter(description = "최소 비피크(할인) 시간대 요금 증감율") @RequestParam(required = false) BigDecimal minimumOffPeakMultiplier,
            @Parameter(description = "최대 비피크(할인) 시간대 요금 증감율") @RequestParam(required = false) BigDecimal maximumOffPeakMultiplier,
            @Parameter(description = "페이지 번호") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 row 개수") @RequestParam(required = false) Integer size
    ) {
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(energyRateService.read(EnergyRateDto.ReadEnergyRateRequest.builder()
                .energyRateId(energyRateId)
                .countryId(countryId)
                .minimumIndustrialRate(minimumIndustrialRate)
                .maximumIndustrialRate(maximumIndustrialRate)
                .minimumCommercialRate(minimumCommercialRate)
                .maximumCommercialRate(maximumCommercialRate)
                .minimumPeakMultiplier(minimumPeakMultiplier)
                .maximumPeakMultiplier(maximumPeakMultiplier)
                .minimumMidPeakMultiplier(minimumMidPeakMultiplier)
                .maximumMidPeakMultiplier(maximumMidPeakMultiplier)
                .minimumOffPeakMultiplier(minimumOffPeakMultiplier)
                .maximumOffPeakMultiplier(maximumOffPeakMultiplier)
                .page(page)
                .size(size)
                .build(), pageable));
    }

    /**
     * 시간별 에너지 요금을 조회하는 API.
     *
     * @param companyId 조회할 회사의 ID
     * @return 시간별 에너지 요금을 담은 응답 객체
     */
    @Operation(summary = "시간별 에너지 요금 조회", description = "피크, 경피크, 비피크 요금을 기준으로 시간별 에너지 요금을 조회")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/hourly-rates/{companyId}")
    public ResponseEntity<ApiResponseDto> readHourlyRates(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId
    ) {
        return apiResponseManager.success(energyRateService.readHourlyRates(companyId));
    }

    /**
     * 에너지 요금을 수정하는 API.
     *
     * @param energyRateId 수정할 에너지 요금의 ID
     * @param updateEnergyRateDto 수정할 에너지 요금의 정보가 담긴 DTO
     * @return 수정된 에너지 요금의 정보를 담은 응답 객체
     */
    @Operation(summary = "에너지 요금 수정", description = "에너지 요금을 수정하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{energyRateId}")
    public ResponseEntity<ApiResponseDto> update(
            @PathVariable Long energyRateId,
            @Valid @RequestBody EnergyRateDto.UpdateEnergyRate updateEnergyRateDto
    ) {
        return apiResponseManager.success(energyRateService.update(energyRateId, updateEnergyRateDto));
    }

    /**
     * 에너지 요금을 삭제하는 API.
     *
     * @param energyRateId 삭제할 에너지 요금의 ID
     * @return 삭제 완료 메시지를 담은 응답 객체
     */
    @Operation(summary = "에너지 요금 삭제", description = "에너지 요금을 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{energyRateId}")
    public ResponseEntity<ApiResponseDto> delete(@PathVariable Long energyRateId) {
        energyRateService.delete(energyRateId);
        return apiResponseManager.ok();
    }
}