package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.config.NoLogging;
import atemos.everse.api.dto.ApiCallLogDto;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.ApiCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ApiCallLog API 컨트롤러.
 * 이 클래스는 ApiCallLog와 관련된 API 엔드포인트를 정의합니다.
 * ApiCallLog의 조회, 유료 API 호출 횟수 조회, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-call-log")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ApiCallLog API", description = "ApiCallLog API 모음")
public class ApiCallLogController {
    private final ApiResponseManager apiResponseManager;
    private final ApiCallLogService apiCallLogService;

    /**
     * ApiCallLog 정보를 조회하는 메서드.
     * 다양한 조건에 따라 ApiCallLog를 조회할 수 있습니다.
     *
     * @param apiCallLogId ApiCallLog ID
     * @param companyId 업체 ID
     * @param statusCode Http 상태 코드
     * @param clientIp 요청 IP
     * @param httpMethod Http 메서드
     * @param isCharge 과금 여부
     * @param startDate API 호출 날짜(시작일)
     * @param endDate API 호출 날짜(종료일)
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조회된 ApiCallLog 리스트
     */
    @Operation(summary = "ApiCallLog 조회", description = "ApiCallLog 정보를 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @NoLogging
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "ApiCallLog ID") @RequestParam(required = false) List<Long> apiCallLogId,
            @Parameter(description = "Http Status Code") @RequestParam(required = false) Integer statusCode,
            @Parameter(description = "요청 IP") @RequestParam(required = false) String clientIp,
            @Parameter(description = "Http Method") @RequestParam(required = false) List<String> httpMethod,
            @Parameter(description = "과금 여부", example = "true") @RequestParam(required = false) Boolean isCharge,
            @Parameter(description = "API 호출 날짜(시작일)", example = "2024-06-03T00:00:00") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "API 호출 날짜(종료일)", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(apiCallLogService.read(
                ApiCallLogDto.ReadApiCallLogRequest.builder()
                        .apiCallLogId(apiCallLogId)
                        .companyId(companyId)
                        .statusCode(statusCode)
                        .clientIp(clientIp)
                        .httpMethod(httpMethod)
                        .isCharge(isCharge)
                        .startDate(startDate)
                        .endDate(endDate)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 월별, 일별 유료 API Call 횟수를 조회하는 메서드.
     * 특정 업체의 월별 및 일별 유료 API 호출 횟수를 조회합니다.
     *
     * @param companyId 업체 ID
     * @param targetDate 기준 날짜
     * @return 유료 API Call 횟수
     */
    @Operation(summary = "월별, 일별 유료 API Call 횟수를 조회", description = "월별, 일별 유료 API Call 횟수를 조회하는 API")
    @NoLogging
    @GetMapping("/charges/{companyId}")
    public ResponseEntity<ApiResponseDto> readChargeableApiCallCount(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "기준 날짜", example = "2024-08-05") @RequestParam(required = false) LocalDate targetDate
    ) {
        return apiResponseManager.success(apiCallLogService.readChargeableApiCallCount(
                ApiCallLogDto.ReadApiCallLogRequest.builder()
                        .companyId(companyId)
                        .targetDate(targetDate)
                        .build()));
    }

    /**
     * 기간 내 ApiCallLog 정보를 삭제하는 메서드.
     * 주어진 기간 내의 ApiCallLog 정보를 삭제합니다.
     *
     * @param deleteApiCallLogRequestDto 삭제할 ApiCallLog 정보
     * @return 삭제 결과
     */
    @Operation(summary = "기간 내 ApiCallLog 삭제", description = "기간 내 ApiCallLog 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @DeleteMapping
    public ResponseEntity<ApiResponseDto> delete(@RequestBody ApiCallLogDto.DeleteApiCallLogRequest deleteApiCallLogRequestDto) {
        apiCallLogService.delete(deleteApiCallLogRequestDto);
        return apiResponseManager.ok();
    }
}