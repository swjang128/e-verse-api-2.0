package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.CountryDto;
import atemos.everse.api.service.CountryService;
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

import java.util.List;

/**
 * 국가 API 컨트롤러.
 * 이 클래스는 국가 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/country")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "국가 API", description = "국가 API 모음")
public class CountryController {
    private final ApiResponseManager apiResponseManager;
    private final CountryService countryService;

    /**
     * 국가 등록 API.
     * 새로운 국가 정보를 등록합니다.
     *
     * @param createCountryDto 등록할 국가 정보
     * @return 등록된 국가 정보
     */
    @Operation(summary = "국가 등록", description = "국가를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody CountryDto.CreateCountry createCountryDto
    ) {
        return apiResponseManager.success(countryService.create(createCountryDto));
    }

    /**
     * 국가 조회 API.
     * 조건에 맞는 국가 정보를 조회합니다.
     *
     * @param countryId 국가 ID
     * @param name 국가의 이름
     * @param languageCode 국가의 언어 코드
     * @param timeZone 국가의 타임존
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 국가 목록
     */
    @Operation(summary = "조건에 맞는 국가 조회", description = "조건에 맞는 국가 정보를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "국가 ID") @RequestParam(required = false) List<Long> countryId,
            @Parameter(description = "국가의 이름") @RequestParam(required = false) String name,
            @Parameter(description = "국가의 언어 코드") @RequestParam(required = false) String languageCode,
            @Parameter(description = "국가의 타임존") @RequestParam(required = false) String timeZone,
            @Parameter(description = "페이지 번호") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(countryService.read(
                CountryDto.ReadCountryRequest.builder()
                        .countryId(countryId)
                        .name(name)
                        .languageCode(languageCode)
                        .timeZone(timeZone)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 모든 국가 목록 조회 API.
     * 모든 국가 목록을 조회합니다.
     *
     * @return 모든 국가 목록
     */
    @Operation(summary = "모든 국가 목록 조회", description = "모든 국가 목록을 조회하는 API")
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto> readAll() {
        return apiResponseManager.success(countryService.readAll());
    }

    /**
     * 국가 수정 API.
     * 특정 국가 ID에 대한 국가 정보를 수정합니다.
     *
     * @param countryId 수정할 국가 ID
     * @param updateCountryDto 수정할 국가 정보
     * @return OK 상태
     */
    @Operation(summary = "국가 수정", description = "국가 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{countryId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "국가 ID", example = "1") @PathVariable Long countryId,
            @Valid @RequestBody CountryDto.UpdateCountry updateCountryDto
    ) {
        return apiResponseManager.success(countryService.update(countryId, updateCountryDto));
    }

    /**
     * 국가 삭제 API.
     * 특정 국가 ID에 대한 국가 정보를 삭제합니다.
     *
     * @param countryId 삭제할 국가 ID
     * @return OK 상태
     */
    @Operation(summary = "국가 삭제", description = "국가 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{countryId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "국가 ID", example = "2") @PathVariable Long countryId
    ) {
        countryService.delete(countryId);
        return apiResponseManager.ok();
    }
}