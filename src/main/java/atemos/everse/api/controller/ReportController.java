package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.config.Chargeable;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.IotStatusHistoryDto;
import atemos.everse.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReportController는 에너지 사용량, 요금 및 IoT 상태 등의 데이터를 엑셀로 다운로드하는 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "엑셀 리포트 API", description = "에너지 및 IoT 상태 데이터를 엑셀로 다운로드하는 API 모음")
public class ReportController {
    private final ApiResponseManager apiResponseManager;
    private final ReportService reportService;

    /**
     * 기간 내 에너지 사용량 및 요금 데이터를 모두 포함한 엑셀 파일을 다운로드하는 API.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량 및 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate (옵션) 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체로 엑셀 파일을 클라이언트로 전송합니다.
     */
    @Operation(summary = "에너지 사용량 및 요금 엑셀 다운로드",
            description = "기간 내 에너지 사용량 및 요금을 포함한 데이터를 엑셀 파일로 다운로드하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/energy/{companyId}")
    public ResponseEntity<ApiResponseDto> reportEnergyUsageAndBill(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId,
            @Parameter(description = "기간 조회 시작일", example = "2024-06-03") @RequestParam LocalDate startDate,
            @Parameter(description = "기간 조회 종료일", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response
    ) {
        reportService.reportEnergyUsageAndBill(companyId, startDate, endDate, response);
        return apiResponseManager.ok();
    }

    /**
     * 기간 내 에너지 사용량 데이터를 엑셀 파일로 다운로드하는 API.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate (옵션) 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체로 엑셀 파일을 클라이언트로 전송합니다.
     */
    @Operation(summary = "에너지 사용량 엑셀 다운로드",
            description = "기간 내 에너지 사용량 데이터를 엑셀 파일로 다운로드하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/energy/usage/{companyId}")
    public ResponseEntity<ApiResponseDto> reportEnergyUsage(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId,
            @Parameter(description = "기간 조회 시작일", example = "2024-06-03") @RequestParam LocalDate startDate,
            @Parameter(description = "기간 조회 종료일", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response
    ) {
        reportService.reportEnergyUsage(companyId, startDate, endDate, response);
        return apiResponseManager.ok();
    }

    /**
     * 기간 내 에너지 요금 데이터를 엑셀 파일로 다운로드하는 API.
     *
     * @param companyId 업체 ID입니다. 에너지 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate (옵션) 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체로 엑셀 파일을 클라이언트로 전송합니다.
     */
    @Operation(summary = "에너지 요금 엑셀 다운로드",
            description = "기간 내 에너지 요금 데이터를 엑셀 파일로 다운로드하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/energy/bill/{companyId}")
    public ResponseEntity<ApiResponseDto> reportEnergyBill(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable Long companyId,
            @Parameter(description = "기간 조회 시작일", example = "2024-06-03") @RequestParam LocalDate startDate,
            @Parameter(description = "기간 조회 종료일", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response
    ) {
        reportService.reportEnergyBill(companyId, startDate, endDate, response);
        return apiResponseManager.ok();
    }

    /**
     * 특정 업체의 특정 기간 내 IoT 상태 이력을 엑셀 파일로 다운로드하는 API.
     * 데이터는 시간별로 집계되어 엑셀 파일로 제공됩니다.
     *
     * @param companyId 업체 ID
     * @param startDate 조회 시작 날짜
     * @param endDate   조회 종료 날짜
     * @param response  HTTP 응답 객체 (엑셀 파일 전송에 사용)
     */
    @Operation(summary = "특정 업체의 특정 기간 내 IoT 상태 이력 엑셀 리포트 다운로드",
            description = "특정 업체의 특정 기간 내 IoT 상태 이력을 시간별로 집계하여 엑셀 파일로 다운로드합니다.")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/iot-history/{companyId}")
    public ResponseEntity<ApiResponseDto> reportIotStatusHistory(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "조회 시작 날짜", example = "2024-06-03") @RequestParam() LocalDate startDate,
            @Parameter(description = "조회 종료 날짜", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response
    ) {
        reportService.reportIotStatusHistory(IotStatusHistoryDto.ReadIotHistoryRequest.builder()
                        .companyId(companyId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build(),
                response);
        return apiResponseManager.ok();
    }

    /**
     * 기간 내 이상 탐지 관련 알람 내역 엑셀 다운로드 (이상 탐지 화면에서 사용).
     *
     * @param companyId 업체 ID
     * @param startDateTime 알람 생성일시 검색 시작일
     * @param endDateTime 알람 생성일시 검색 종료일
     * @param response HTTP 응답 객체로 엑셀 파일을 클라이언트로 전송합니다.
     */
    @Operation(summary = "기간 내 이상 탐지 관련 알람 내역을 엑셀로 다운로드", description = "기간 내 이상 탐지 관련 알람 내역을 엑셀로 다운로드하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or @securityService.isSelf(#companyId)")
    @Chargeable(true)
    @GetMapping("/anomaly/{companyId}")
    public ResponseEntity<ApiResponseDto> reportAnomalyAlarms(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "알람 생성일시 검색 시작 시각", example = "2024-06-03T00:00:00") @RequestParam() LocalDateTime startDateTime,
            @Parameter(description = "알람 생성일시 검색 종료 시각", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime endDateTime,
            HttpServletResponse response
    ) {
        reportService.reportAnomalyAlarms(companyId, startDateTime, endDateTime, response);
        return apiResponseManager.ok();
    }
}
