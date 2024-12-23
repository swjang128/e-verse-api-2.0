package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.MemberStatus;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.service.MemberService;
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
 * 사용자 관리 API 컨트롤러.
 * 이 클래스는 사용자 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "사용자 관리 API", description = "사용자 관리 API 모음")
public class MemberController {
    private final ApiResponseManager apiResponseManager;
    private final MemberService memberService;

    /**
     * 사용자 등록 API.
     * 새 사용자를 등록합니다.
     *
     * @param createMemberDto 등록할 사용자 정보
     * @return 등록된 사용자 정보
     */
    @Operation(summary = "사용자 등록", description = "사용자 정보를 등록하는 API")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody MemberDto.CreateMember createMemberDto
    ) {
        return apiResponseManager.success(memberService.create(createMemberDto));
    }

    /**
     * 조건에 맞는 사용자 목록 조회 API.
     * 주어진 조건에 맞는 사용자 목록을 조회합니다.
     *
     * @param companyId 업체 ID
     * @param memberId 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param phone 사용자 연락처
     * @param role 사용자 권한
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @param masking 마스킹 여부
     * @return 조건에 맞는 사용자 목록
     */
    @Operation(summary = "조건에 맞는 사용자 목록 조회", description = "조건에 맞는 사용자 목록을 조회하는 API")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID") @RequestParam(required = false) List<Long> companyId,
            @Parameter(description = "사용자 ID") @RequestParam(required = false) List<Long> memberId,
            @Parameter(description = "이름") @RequestParam(required = false) String name,
            @Parameter(description = "이메일") @RequestParam(required = false) String email,
            @Parameter(description = "연락처") @RequestParam(required = false) String phone,
            @Parameter(description = "권한") @RequestParam(required = false) List<MemberRole> role,
            @Parameter(description = "상태") @RequestParam(required = false) List<MemberStatus> status,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size,
            @Parameter(description = "마스킹 여부", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean masking
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        // 조건에 맞는 사용자 목록 및 페이징 객체 리턴
        return apiResponseManager.success(memberService.read(
                MemberDto.ReadMemberRequest.builder()
                        .memberId(memberId)
                        .companyId(companyId)
                        .name(name)
                        .email(email)
                        .phone(phone)
                        .role(role)
                        .status(status)
                        .page(page)
                        .size(size)
                        .masking(masking)
                        .build(),
                pageable));
    }

    /**
     * 사용자 정보 수정 API.
     * 특정 사용자의 정보를 수정합니다. (관리자 권한 필요)
     *
     * @param memberId 사용자 ID
     * @param updateMemberDto 수정할 사용자 정보
     * @return 수정된 사용자 정보
     */
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or @securityService.isSelf(#memberId)")
    @PatchMapping("/{memberId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "사용자 ID", example = "1", required = true) @PathVariable Long memberId,
            @RequestBody MemberDto.UpdateMember updateMemberDto
    ) {
        return apiResponseManager.success(memberService.update(memberId, updateMemberDto));
    }

    /**
     * 사용자 삭제 API.
     * 특정 사용자를 삭제합니다.
     *
     * @param memberId 사용자 ID
     * @return 삭제 완료 응답
     */
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제하는 API")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "사용자 ID", example = "1", required = true) @PathVariable Long memberId
    ) {
        memberService.delete(memberId);
        return apiResponseManager.ok();
    }
}