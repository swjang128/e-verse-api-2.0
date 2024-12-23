package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.domain.IotType;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.IotDto;
import atemos.everse.api.service.IotService;
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
 * IoT 장비 관리 API 컨트롤러.
 * 이 클래스는 IoT 장비의 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/iot")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "IoT 장비 API", description = "IoT 장비 API 모음")
public class IotController {
    private final ApiResponseManager apiResponseManager;
    private final IotService iotService;

    /**
     * IoT 장비 등록 API.
     * 새로운 IoT 장비를 등록합니다.
     *
     * @param createIotDto 등록할 IoT 장비의 정보
     * @return 등록된 IoT 장비의 정보
     */
    @Operation(summary = "IoT 등록", description = "IoT를 등록하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody IotDto.CreateIot createIotDto
    ) {
        return apiResponseManager.success(iotService.create(createIotDto));
    }

    /**
     * IoT 장비 조회 API.
     * 주어진 조건에 맞는 IoT 장비를 조회합니다.
     *
     * @param iotId IoT 장비 ID
     * @param companyId 업체 ID
     * @param serialNumber IoT 장비의 시리얼 넘버
     * @param type IoT 장비의 유형
     * @param location IoT 장비의 위치
     * @param minimumPrice 단가의 최소치
     * @param maximumPrice 단가의 최대치
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 IoT 장비의 리스트
     */
    @Operation(summary = "조건에 맞는 IoT 조회", description = "조건에 맞는 IoT를 조회하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "IoT ID") @RequestParam(required = false) List<Long> iotId,
            @Parameter(description = "업체 ID") @PathVariable List<Long> companyId,
            @Parameter(description = "시리얼 넘버") @RequestParam(required = false) String serialNumber,
            @Parameter(description = "유형") @RequestParam(required = false) List<IotType> type,
            @Parameter(description = "위치") @RequestParam(required = false) String location,
            @Parameter(description = "단가(최소치)") @RequestParam(required = false) BigDecimal minimumPrice,
            @Parameter(description = "단가(최대치)") @RequestParam(required = false) BigDecimal maximumPrice,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(iotService.read(
                IotDto.ReadIotRequest.builder()
                        .iotId(iotId)
                        .companyId(companyId)
                        .serialNumber(serialNumber)
                        .type(type)
                        .location(location)
                        .minimumPrice(minimumPrice)
                        .maximumPrice(maximumPrice)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * IoT 장비 정보 수정 API.
     * 주어진 IoT 장비 ID에 대한 정보를 수정합니다.
     *
     * @param iotId 수정할 IoT 장비의 ID
     * @param updateIotDto 수정할 IoT 장비의 정보
     * @return 수정된 IoT 장비의 정보
     */
    @Operation(summary = "IoT 수정", description = "IoT 정보를 수정하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PatchMapping("/{iotId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "IoT ID", example = "1") @PathVariable() Long iotId,
            @Valid @RequestBody IotDto.UpdateIot updateIotDto
    ) {
        return apiResponseManager.success(iotService.update(iotId, updateIotDto));
    }

    /**
     * IoT 장비 삭제 API.
     * 주어진 IoT 장비 ID에 해당하는 장비를 삭제합니다.
     *
     * @param iotId 삭제할 IoT 장비의 ID
     * @return 삭제 완료 응답
     */
    @Operation(summary = "IoT 삭제", description = "IoT 정보를 삭제하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{iotId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "IoT ID", example = "2") @PathVariable() Long iotId
    ) {
        iotService.delete(iotId);
        return apiResponseManager.ok();
    }
}