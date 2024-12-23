package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.SubscriptionDto;
import atemos.everse.api.service.SubscriptionService;
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
 * 구독 API 컨트롤러.
 * 이 클래스는 구독 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "구독 API", description = "구독 API 모음")
public class SubscriptionController {
    private final ApiResponseManager apiResponseManager;
    private final SubscriptionService subscriptionService;

    /**
     * 구독 정보 등록 API.
     * 새로운 구독 정보를 등록합니다.
     *
     * @param createSubscriptionDto 등록할 구독 정보
     * @return 등록된 구독 정보
     */
    @Operation(summary = "구독 정보 등록", description = "구독 정보를 등록하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody SubscriptionDto.CreateSubscription createSubscriptionDto
    ) {
        return apiResponseManager.success(subscriptionService.create(createSubscriptionDto));
    }

    /**
     * 구독 조회 API.
     * 조건에 맞는 구독 정보를 조회합니다.
     *
     * @param subscriptionId 구독 ID
     * @param companyId 업체 ID
     * @param serviceList 구독한 서비스 목록
     * @param searchDate 구독 여부를 확인할 날짜
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 구독 목록
     */
    @Operation(summary = "조건에 맞는 구독 조회", description = "조건에 맞는 구독 정보를 조회하는 API")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "구독 ID") @RequestParam(required = false) List<Long> subscriptionId,
            @Parameter(description = "구독한 서비스 목록") @RequestParam(required = false) List<SubscriptionServiceList> serviceList,
            @Parameter(description = "구독 여부를 확인할 날짜") @RequestParam(required = false) LocalDate searchDate,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(subscriptionService.read(
                SubscriptionDto.ReadSubscriptionRequest.builder()
                        .subscriptionId(subscriptionId)
                        .companyId(companyId)
                        .serviceList(serviceList)
                        .searchDate(searchDate)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 구독 수정 API.
     * 특정 구독 ID에 대한 구독 정보를 수정합니다.
     *
     * @param subscriptionId 수정할 구독 ID
     * @param updateSubscriptionDto 수정할 구독 정보
     * @return 수정된 구독 정보
     */
    @Operation(summary = "구독 수정", description = "구독 정보를 수정하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PatchMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "구독 ID", example = "1") @PathVariable Long subscriptionId,
            @Valid @RequestBody SubscriptionDto.UpdateSubscription updateSubscriptionDto
    ) {
        return apiResponseManager.success(subscriptionService.update(subscriptionId, updateSubscriptionDto));
    }

    /**
     * 구독 취소 API.
     * 특정 구독 ID에 대한 구독 정보를 취소합니다.
     *
     * @param subscriptionId 취소할 구독 ID
     * @return OK 상태
     */
    @Operation(summary = "구독 취소", description = "구독을 취소하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PatchMapping("/cancel/{subscriptionId}")
    public ResponseEntity<ApiResponseDto> cancelSubscription(
            @Parameter(description = "구독 ID", example = "1") @PathVariable Long subscriptionId
    ) {
        subscriptionService.cancelSubscription(subscriptionId);
        return apiResponseManager.ok();
    }

    /**
     * 구독 삭제 API.
     * 특정 구독 ID에 대한 구독 정보를 삭제합니다.
     *
     * @param subscriptionId 삭제할 구독 ID
     * @return OK 상태
     */
    @Operation(summary = "구독 삭제", description = "구독 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "구독 ID") @PathVariable Long subscriptionId
    ) {
        subscriptionService.delete(subscriptionId);
        return apiResponseManager.ok();
    }
}