package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.config.Chargeable;
import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.domain.IotType;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.IotStatusHistoryDto;
import atemos.everse.api.service.IotStatusHistoryService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * IoT 상태 이력 API 컨트롤러.
 * 이 클래스는 IoT 장비의 상태 이력을 조회하기 위한 API를 제공합니다.
 * MANAGER 또는 ADMIN 권한을 가진 사용자만 접근할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/iot/history")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "IoT 상태 이력 API", description = "IoT 상태 이력 API 모음")
public class IotStatusHistoryController {
    private final ApiResponseManager apiResponseManager;
    private final IotStatusHistoryService iotStatusHistoryService;

    /**
     * 조건에 맞는 IoT 상태 이력을 조회하는 API. (설비 현황 - facilities List 영역)
     * 다양한 필터 조건을 사용하여 IoT 상태 이력을 검색할 수 있습니다.
     *
     * @param iotHistoryId        IoT 상태 이력 ID
     * @param iotId               IoT ID
     * @param companyId           업체 ID
     * @param serialNumber        시리얼 넘버
     * @param type                IoT 장비 유형
     * @param status              IoT 상태
     * @param location            설치 위치
     * @param minimumFacilityUsage 가동량 최소치
     * @param maximumFacilityUsage 가동량 최대치
     * @param minimumPrice        단가 최소치
     * @param maximumPrice        단가 최대치
     * @param startDate           조회 시작 날짜
     * @param endDate             조회 종료 날짜
     * @param page                페이지 번호
     * @param size                페이지당 데이터 개수
     * @param isHourly            시간별 데이터를 포함할지 여부
     * @return IoT 상태 이력 데이터와 함께 응답을 반환합니다.
     */
    @Operation(summary = "조건에 맞는 IoT 상태 이력 조회", description = "조건에 맞는 IoT 현황을 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Chargeable(true)
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "IoT 상태 이력 ID") @RequestParam(required = false) List<Long> iotHistoryId,
            @Parameter(description = "IoT ID") @RequestParam(required = false) List<Long> iotId,
            @Parameter(description = "시리얼 넘버") @RequestParam(required = false) String serialNumber,
            @Parameter(description = "유형") @RequestParam(required = false) List<IotType> type,
            @Parameter(description = "상태") @RequestParam(required = false) List<IotStatus> status,
            @Parameter(description = "위치") @RequestParam(required = false) String location,
            @Parameter(description = "가동량(최소치)") @RequestParam(required = false) BigDecimal minimumFacilityUsage,
            @Parameter(description = "가동량(최대치)") @RequestParam(required = false) BigDecimal maximumFacilityUsage,
            @Parameter(description = "단가(최소치)") @RequestParam(required = false) BigDecimal minimumPrice,
            @Parameter(description = "단가(최대치)") @RequestParam(required = false) BigDecimal maximumPrice,
            @Parameter(description = "조회 시작 날짜", example = "2024-06-03") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size,
            @Parameter(description = "시간별 데이터를 포함할지 여부", example = "false") @RequestParam(required = false, defaultValue = "false") Boolean isHourly
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        // 조건에 맞는 IoT 상태 이력 조회
        return apiResponseManager.success(iotStatusHistoryService.read(
                IotStatusHistoryDto.ReadIotHistoryRequest.builder()
                        .iotHistoryId(iotHistoryId)
                        .iotId(iotId)
                        .companyId(companyId)
                        .serialNumber(serialNumber)
                        .type(type)
                        .status(status)
                        .location(location)
                        .minimumFacilityUsage(minimumFacilityUsage)
                        .maximumFacilityUsage(maximumFacilityUsage)
                        .minimumPrice(minimumPrice)
                        .maximumPrice(maximumPrice)
                        .startDate(startDate)
                        .endDate(endDate)
                        .page(page)
                        .size(size)
                        .isHourly(isHourly)
                        .build(),
                pageable));
    }

    /**
     * 오늘 특정 업체의 시간별 IoT 상태 이력 조회 API.(대시보드의 IoT 상태 이력 조회 영역)
     * 조회된 데이터는 시간대별로 그룹화되어 반환됩니다.
     *
     * @param companyId 업체 ID
     * @return 오늘 특정 업체의 시간별 IoT 상태 이력 데이터
     */
    @Operation(summary = "오늘 특정 업체의 시간별 IoT 상태 이력 조회",
            description = "오늘 특정 업체의 시간별 IoT 상태 이력을 조회하는 API")
    @GetMapping("/realtime/{companyId}")
    public ResponseEntity<ApiResponseDto> readByRealtime(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId
    ) {
        // 오늘 날짜를 startDate와 endDate로 설정하여 서비스 호출
        return apiResponseManager.success(iotStatusHistoryService.readByCompanyId(
                IotStatusHistoryDto.ReadIotHistoryRequest.builder()
                        .companyId(companyId)
                        .build()));
    }
}