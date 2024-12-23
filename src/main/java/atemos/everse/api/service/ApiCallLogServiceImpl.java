package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.dto.ApiCallLogDto;
import atemos.everse.api.repository.ApiCallLogRepository;
import atemos.everse.api.specification.ApiCallLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;

/**
 * ApiCallLogServiceImpl는 API 호출 로그와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * API 호출 로그의 조회, 월별/일별 유료 호출 횟수 조회, 로그 삭제 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiCallLogServiceImpl implements ApiCallLogService {
    private final ApiCallLogRepository apiCallLogRepository;
    private final JwtUtil jwtUtil;

    /**
     * 조건에 맞는 ApiCallLog를 조회합니다.
     *
     * @param readApiCallLogRequestDto ApiCallLog 조회 조건을 포함하는 데이터 전송 객체
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 ApiCallLog 목록과 관련된 추가 정보를 포함하는 응답 객체
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ApiCallLogDto.ReadApiCallLogPageResponse read(ApiCallLogDto.ReadApiCallLogRequest readApiCallLogRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 ApiCallLog 목록 조회
        var apiCallLogPage = apiCallLogRepository.findAll(ApiCallLogSpecification.findWith(readApiCallLogRequestDto), pageable);
        // ApiCallLog 응답 DTO로 변환할 때 zoneId를 사용하여 LocalDateTIme으로 변환
        var apiCallLogList = apiCallLogPage.getContent().stream()
                .map(apiCallLog -> new ApiCallLogDto.ReadApiCallLogResponse(apiCallLog, zoneId))
                .toList();
        // 응답 객체 반환
        return new ApiCallLogDto.ReadApiCallLogPageResponse(
                apiCallLogList,
                apiCallLogPage.getTotalElements(),
                apiCallLogPage.getTotalPages());
    }

    /**
     * 월별 및 일별 유료 API 호출 횟수를 조회합니다.
     *
     * @param readApiCallLogRequestDto 월별 및 일별 유료 API 호출 횟수 조회 조건을 포함하는 데이터 전송 객체
     * @return 조회된 월별 및 일별 유료 API 호출 횟수를 포함하는 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public ApiCallLogDto.ReadChargeableApiCallCountResponse readChargeableApiCallCount(ApiCallLogDto.ReadApiCallLogRequest readApiCallLogRequestDto) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var member = jwtUtil.getCurrentMember();
        var zoneId = member.getCompany().getCountry().getZoneId();
        // 해당 날짜에 호출된 유료 API Call 건수 조회 (대상의 zoneId에 맞추기)
        var targetDate = readApiCallLogRequestDto.getTargetDate();
        var dailyChargeableApiCalls = apiCallLogRepository.countByCompanyIdAndIsChargeAndRequestTimeBetween(
                readApiCallLogRequestDto.getCompanyId(),
                true,
                targetDate.atStartOfDay(zoneId).toInstant(),
                targetDate.atTime(23, 59, 59).atZone(zoneId).toInstant()
        );
        // 해당 월에 호출된 유료 API Call 건수 조회
        var yearMonth = YearMonth.from(targetDate);
        var monthlyChargeableApiCalls = apiCallLogRepository.countByCompanyIdAndIsChargeAndRequestTimeBetween(
                readApiCallLogRequestDto.getCompanyId(),
                true,
                yearMonth.atDay(1).atStartOfDay(zoneId).toInstant(),
                yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zoneId).toInstant()
        );
        // 응답 객체 반환
        return ApiCallLogDto.ReadChargeableApiCallCountResponse.builder()
                .dailyChargeableApiCalls(dailyChargeableApiCalls)
                .monthlyChargeableApiCalls(monthlyChargeableApiCalls)
                .build();
    }

    /**
     * 특정 기간까지의 ApiCallLog를 삭제합니다.
     *
     * @param deleteApiCallLogRequestDto ApiCallLog 삭제 요청을 위한 데이터 전송 객체
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(ApiCallLogDto.DeleteApiCallLogRequest deleteApiCallLogRequestDto) {
        // 특정 기간 이전의 ApiCallLog를 조회 및 삭제
        var requestTime = Instant.parse(deleteApiCallLogRequestDto.getRequestTime());
        var logsToDelete = apiCallLogRepository.findByRequestTimeBefore(requestTime);
        logsToDelete.forEach(log -> {
            if (apiCallLogRepository.existsById(log.getId())) {
                apiCallLogRepository.delete(log);
            }
        });
    }
}