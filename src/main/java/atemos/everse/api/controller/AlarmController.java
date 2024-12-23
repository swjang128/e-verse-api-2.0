package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.domain.AlarmType;
import atemos.everse.api.dto.AlarmDto;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람 API 컨트롤러.
 * 이 클래스는 알람과 관련된 API 엔드포인트를 정의합니다.
 * 알람 조회 및 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "알람 API", description = "알람 API 모음")
public class AlarmController {
    private final ApiResponseManager apiResponseManager;
    private final AlarmService alarmService;

    /**
     * 대시보드의 알람에 표시할 데이터 조회 API
     * 대시보드의 알람에 표시할 내용을 조회할 수 있습니다.
     * - AI 구독을 하는 경우에는 모든 type을 가져오기
     * - AI 구독을 하지 않는 경우에는 아래 type만 가져오기
     *     - MAXIMUM_ENERGY_USAGE
     *     - MINIMUM_ENERGY_USAGE
     *
     * @param companyId 업체 ID
     * @param type 알람 유형
     * @param isRead 읽음 여부
     * @param expirationDate 알람 만료일시
     * @param startDateTime 알람 생성일시 검색 시작일시
     * @param endDateTime 알람 생성일시 검색 종료일시
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조회된 알람 리스트
     */
    @Operation(summary = "대시보드의 알람에 표시할 데이터 조회", description = "대시보드의 알람에 표시할 데이터 조회 API")
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "알람 ID") @RequestParam(required = false) List<Long> alarmId,
            @Parameter(description = "유형") @RequestParam(required = false) List<AlarmType> type,
            @Parameter(description = "읽음 여부") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "알람 만료일시") @RequestParam(required = false) LocalDateTime expirationDate,
            @Parameter(description = "알람 생성일시 검색 시작일시", example = "2024-06-03T00:00:00") @RequestParam(required = false) LocalDateTime startDateTime,
            @Parameter(description = "알람 생성일시 검색 종료일시", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime endDateTime,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(alarmService.read(
                AlarmDto.ReadAlarmRequest.builder()
                        .alarmId(alarmId)
                        .companyId(companyId)
                        .type(type)
                        .isRead(isRead)
                        .expirationDate(expirationDate)
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 실시간 알람 스트림을 제공하는 SSE 엔드포인트.
     * 클라이언트와의 SSE 연결을 통해 실시간으로 알람을 전송합니다.
     *
     * @return SSE 연결을 위한 SseEmitter 객체
     */
    @Operation(summary = "실시간 알람 스트림", description = "실시간으로 알람을 스트리밍하는 SSE 엔드포인트")
    @GetMapping(value = "/stream/{companyId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlarm(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "읽음 여부", example = "false") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "알람 만료일시") @RequestParam(required = false) LocalDateTime expirationDate
    ) {
        return alarmService.streamAlarm(AlarmDto.ReadAlarmRequest.builder()
                .companyId(companyId)
                .isRead(isRead)
                .expirationDate(expirationDate)
                .build());
    }

    /**
     * 기간 내 이상 탐지 관련 알람 내역 조회 (이상탐지 화면에서 사용)
     * 기간 내 이상 탐지 관련 알람 내역 조회할 수 있습니다.
     *
     * @param companyId 업체 ID
     * @param startDateTime 알람 생성일시 검색 시작일
     * @param endDateTime 알람 생성일시 검색 종료일
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조회된 기간 내 이상 탐지 관련 알람 내역
     */
    @Operation(summary = "기간 내 이상 탐지 관련 알람 내역 조회", description = "기간 내 이상 탐지 관련 알람 내역 조회하는 API")
    @PreAuthorize("@securityService.isSelf(#companyId) or hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/anomaly/{companyId}")
    public ResponseEntity<ApiResponseDto> readAnomalyAlarms(
            @Parameter(description = "업체 ID", example = "1") @PathVariable Long companyId,
            @Parameter(description = "알람 생성일시 검색 시작일시", example = "2024-06-03T00:00:00") @RequestParam(required = false) LocalDateTime startDateTime,
            @Parameter(description = "알람 생성일시 검색 종료일시", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime endDateTime,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        // Pageable 객체 생성, null인 경우 unpaged로 처리됨
        var pageable = (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged();
        return apiResponseManager.success(alarmService.read(
                AlarmDto.ReadAlarmRequest.builder()
                        .companyId(companyId)
                        .type(List.of(AlarmType.MAXIMUM_ENERGY_USAGE, AlarmType.MINIMUM_ENERGY_USAGE))
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .page(page)
                        .size(size)
                        .build(),
                pageable));
    }

    /**
     * 알람 수정 API.
     * 기존 알람을 수정합니다.
     *
     * @param alarmId 알람 ID
     * @param updateAlarmDto 수정할 알람 정보
     * @return 수정된 알람 정보
     */
    @Operation(summary = "알람 수정", description = "기존 알람을 수정하는 API")
    @PatchMapping("/{alarmId}")
    public ResponseEntity<ApiResponseDto> update(
            @PathVariable Long alarmId,
            @Valid @RequestBody AlarmDto.UpdateAlarm updateAlarmDto
    ) {
        return apiResponseManager.success(alarmService.update(alarmId, updateAlarmDto));
    }
    
    /**
     * 알람 정보를 삭제하는 메서드.
     * 특정 알람 ID를 기반으로 알람을 삭제합니다.
     *
     * @param alarmId 알람 ID
     * @return 삭제 결과
     */
    @Operation(summary = "알람 삭제", description = "알람 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "알람 ID", example = "2") @PathVariable Long alarmId
    ) {
        alarmService.delete(alarmId);
        return apiResponseManager.ok();
    }
}