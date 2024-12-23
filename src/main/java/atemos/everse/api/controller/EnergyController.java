package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.config.Chargeable;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.EnergyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 에너지 사용량 및 요금 API 컨트롤러.
 * 이 클래스는 에너지 사용량과 요금 조회 및 관련 보고서를 제공하는 API 엔드포인트를 정의합니다.
 * 통합된 엔드포인트를 통해 실시간, 월별, 기간 내 에너지 사용량과 요금을 조회하거나 엑셀 보고서를 다운로드할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/energy")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "에너지 사용량 및 요금 API", description = "에너지 사용량 및 요금 API 모음")
public class EnergyController {
    private final ApiResponseManager apiResponseManager;
    private final EnergyService energyService;

    /**
     * 기간 내 업체의 에너지 사용량과 요금을 조회하는 API.
     * 사용자는 시작일과 종료일을 지정하여 특정 기간 내의 데이터를 조회할 수 있습니다.
     *
     * @param companyId 업체 ID
     * @param startDate 기간 시작일
     * @param endDate (옵션) 기간 종료일, 없으면 시작일과 동일하게 처리
     * @return 기간 내 업체의 에너지 사용량 및 요금 정보
     */
    @Operation(summary = "기간 내 에너지 사용량 및 요금 조회",
            description = "기간 내 업체의 에너지 사용량 및 요금을 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> readEnergy(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId,
            @Parameter(description = "기간 조회 시작일", example = "2024-06-03") @RequestParam LocalDate startDate,
            @Parameter(description = "기간 조회 종료일", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate
    ) {
        return apiResponseManager.success(energyService.readEnergy(companyId, startDate, endDate));
    }

    /**
     * 업체의 실시간 및 전월 에너지 사용량과 요금을 조회하는 API.
     * 이 엔드포인트는 대시보드에서 실시간과 전월의 에너지 데이터를 보여줍니다.
     *
     * @param companyId 업체 ID
     * @return 업체의 실시간 및 전월 에너지 사용량과 요금 정보
     */
    @Operation(summary = "업체의 실시간 및 전월 에너지 사용량과 요금을 조회",
            description = "업체의 실시간 및 전월 에너지 사용량과 요금을 조회하는 API")
    @GetMapping("/realtime/{companyId}")
    public ResponseEntity<ApiResponseDto> readRealTimeEnergy(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId
    ) {
        return apiResponseManager.success(energyService.getRealTimeAndLastMonthEnergy(companyId));
    }

    /**
     * 이번 달 및 저번 달 업체의 에너지 사용량과 요금을 조회하는 API.
     * 이 엔드포인트는 AI 분석용으로 현재 달과 저번 달의 에너지 데이터를 비교합니다.
     *
     * @param companyId 업체 ID
     * @return 이번 달 및 저번 달 업체의 에너지 사용량과 요금 정보
     */
    @Operation(summary = "이번 달 업체의 에너지 사용량 및 요금을 조회",
            description = "이번 달 및 저번 달 업체의 에너지 사용량과 요금을 조회하는 API")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/monthly/{companyId}")
    public ResponseEntity<ApiResponseDto> readMonthlyEnergy(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId
    ) {
        return apiResponseManager.success(energyService.getThisAndLastMonthEnergy(companyId));
    }
}
