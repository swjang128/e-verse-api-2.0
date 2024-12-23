package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.domain.PaymentMethod;
import atemos.everse.api.domain.PaymentStatus;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.PaymentDto;
import atemos.everse.api.service.PaymentService;
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
import java.time.LocalDate;
import java.util.List;

/**
 * 결제 API 컨트롤러.
 * 이 클래스는 결제 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "결제 API", description = "결제 API 모음")
public class PaymentController {
    private final ApiResponseManager apiResponseManager;
    private final PaymentService paymentService;

    /**
     * 결제 조회 API.
     * 특정 조건에 해당하는 결제 정보를 조회합니다.
     *
     * @param paymentId 결제 ID
     * @param companyId 업체 ID
     * @param meteredUsageId 서비스 사용 내역 ID
     * @param subscriptionServiceList 구독한 서비스 목록
     * @param method 결제 방법
     * @param minimumAmount 지불할 금액의 조회 최소값
     * @param maximumAmount 지불할 금액의 조회 최대값
     * @param status 결제 상태
     * @param usageDateStart 결제 내역의 기준 사용 날짜에 대한 조회 시작일
     * @param usageDateEnd 결제 내역의 기준 사용 날짜에 대한 조회 종료일
     * @param scheduledPaymentDateStart 결제 예정일에 대한 조회 시작일
     * @param scheduledPaymentDateEnd 결제 예정일에 대한 조회 종료일
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 결제 정보
     */
    @Operation(summary = "결제 조회", description = "결제 정보를 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable List<Long> companyId,
            @Parameter(description = "결제 ID") @RequestParam(required = false) List<Long> paymentId,
            @Parameter(description = "서비스 사용 내역 ID") @RequestParam(required = false) List<Long> meteredUsageId,
            @Parameter(description = "구독 서비스 목록") @RequestParam(required = false) List<SubscriptionServiceList> subscriptionServiceList,
            @Parameter(description = "결제 방법") @RequestParam(required = false) List<PaymentMethod> method,
            @Parameter(description = "지불할 금액의 조회 최소값") @RequestParam(required = false) BigDecimal minimumAmount,
            @Parameter(description = "지불할 금액의 조회 최대값") @RequestParam(required = false) BigDecimal maximumAmount,
            @Parameter(description = "결제 상태") @RequestParam(required = false) List<PaymentStatus> status,
            @Parameter(description = "결제 내역의 기준 사용 날짜에 대한 조회 시작일") @RequestParam(required = false) LocalDate usageDateStart,
            @Parameter(description = "결제 내역의 기준 사용 날짜에 대한 조회 종료일") @RequestParam(required = false) LocalDate usageDateEnd,
            @Parameter(description = "결제 예정일에 대한 조회 시작일", example = "2024-07-01") @RequestParam(required = false) LocalDate scheduledPaymentDateStart,
            @Parameter(description = "결제 예정일에 대한 조회 종료일", example = "2026-12-31") @RequestParam(required = false) LocalDate scheduledPaymentDateEnd,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(paymentService.read(
                PaymentDto.ReadPaymentRequest.builder()
                        .paymentId(paymentId)
                        .companyId(companyId)
                        .meteredUsageId(meteredUsageId)
                        .subscriptionServiceList(subscriptionServiceList)
                        .method(method)
                        .minimumAmount(minimumAmount)
                        .maximumAmount(maximumAmount)
                        .status(status)
                        .usageDateStart(usageDateStart)
                        .usageDateEnd(usageDateEnd)
                        .scheduledPaymentDateStart(scheduledPaymentDateStart)
                        .scheduledPaymentDateEnd(scheduledPaymentDateEnd)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 결제 수정 API.
     * 특정 결제 ID에 대한 결제 정보를 수정합니다.
     *
     * @param paymentId 수정할 결제 ID
     * @param updatePaymentDto 수정할 결제 정보
     * @return 수정된 결제 정보
     */
    @Operation(summary = "결제 수정", description = "결제 정보를 수정하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PatchMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "결제 ID", example = "1") @PathVariable Long paymentId,
            @Valid @RequestBody PaymentDto.UpdatePayment updatePaymentDto
    ) {
        return apiResponseManager.success(paymentService.update(paymentId, updatePaymentDto));
    }

    /**
     * 결제 삭제 API.
     * 특정 결제 ID에 대한 결제 정보를 삭제합니다.
     *
     * @param paymentId 삭제할 결제 ID
     * @return OK 상태
     */
    @Operation(summary = "결제 삭제", description = "결제 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "결제 ID", example = "2") @PathVariable Long paymentId
    ) {
        paymentService.delete(paymentId);
        return apiResponseManager.ok();
    }
}