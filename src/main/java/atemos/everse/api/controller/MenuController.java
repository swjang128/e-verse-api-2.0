package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.config.NoLogging;
import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.MenuDto;
import atemos.everse.api.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴 관리 API 컨트롤러.
 * 이 클래스는 메뉴 관리를 위한 API를 제공합니다.
 * ADMIN 사용자만 호출할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "메뉴 관리 API", description = "메뉴 관리 API 모음")
public class MenuController {
    private final ApiResponseManager apiResponseManager;
    private final MenuService menuService;

    /**
     * 메뉴 등록 API.
     * 메뉴를 등록합니다.
     *
     * @param createMenuDto 등록할 메뉴 정보
     * @return 등록된 메뉴 정보
     */
    @Operation(summary = "메뉴 등록", description = "메뉴를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody MenuDto.CreateMenu createMenuDto
    ) {
        return apiResponseManager.success(menuService.create(createMenuDto));
    }

    /**
     * 조건에 맞는 메뉴 조회 API.
     * 시스템에 등록된 조건에 맞는 메뉴 정보를 조회합니다.
     *
     * @param menuId 메뉴 ID
     * @param name 메뉴 이름
     * @param url 메뉴 URL
     * @param description 메뉴 설명
     * @param available 사용 여부
     * @param parentId 상위 메뉴 ID
     * @param roles 접근 권한 리스트
     * @return 조건에 맞는 메뉴 정보
     */
    @Operation(summary = "조건에 맞는 메뉴 조회", description = "조건에 맞는 메뉴들을 조회하는 API")
    @NoLogging
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "메뉴 ID") @RequestParam(required = false) List<Long> menuId,
            @Parameter(description = "메뉴 이름") @RequestParam(required = false) String name,
            @Parameter(description = "URL") @RequestParam(required = false) String url,
            @Parameter(description = "설명") @RequestParam(required = false) String description,
            @Parameter(description = "사용 여부") @RequestParam(required = false) Boolean available,
            @Parameter(description = "상위 메뉴 ID") @RequestParam(required = false) List<Long> parentId,
            @Parameter(description = "접근 권한") @RequestParam(required = false) List<MemberRole> roles
    ) {
        return apiResponseManager.success(menuService.read(
                MenuDto.ReadMenuRequest.builder()
                        .menuId(menuId)
                        .name(name)
                        .url(url)
                        .description(description)
                        .available(available)
                        .parentId(parentId)
                        .roles(roles)
                        .build()));
    }

    /**
     * 메뉴 수정 API.
     * 기존 메뉴를 수정합니다.
     *
     * @param menuId 메뉴 ID
     * @param updateMenuDto 수정할 메뉴 정보
     * @return 수정된 메뉴 정보
     */
    @Operation(summary = "메뉴 수정", description = "기존 메뉴를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @PatchMapping("/{menuId}")
    public ResponseEntity<ApiResponseDto> update(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuDto.UpdateMenu updateMenuDto
    ) {
        return apiResponseManager.success(menuService.update(menuId, updateMenuDto));
    }

    /**
     * 메뉴 삭제 API.
     * 기존 메뉴를 삭제합니다.
     *
     * @param menuId 메뉴 ID
     * @return 삭제 완료 응답
     */
    @Operation(summary = "메뉴 삭제", description = "기존 메뉴를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponseDto> delete(@PathVariable Long menuId) {
        menuService.delete(menuId);
        return apiResponseManager.ok();
    }
}