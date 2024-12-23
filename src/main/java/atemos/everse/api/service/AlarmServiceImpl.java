package atemos.everse.api.service;

import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.domain.AlarmType;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.AlarmDto;
import atemos.everse.api.dto.SubscriptionDto;
import atemos.everse.api.repository.AlarmRepository;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.SubscriptionRepository;
import atemos.everse.api.specification.AlarmSpecification;
import atemos.everse.api.specification.SubscriptionSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AlarmServiceImpl는 알람과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 알람 조회, 수정, 삭제 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {
    private final PlatformTransactionManager transactionManager;
    private final AlarmRepository alarmRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final SubscriptionRepository  subscriptionRepository;
    private final JwtUtil jwtUtil;

    /**
     * 대시보드의 알람에 표시할 데이터 조회 API
     * 대시보드의 알람에 표시할 내용을 조회할 수 있습니다.
     * - AI 구독을 하는 경우에는 DTO에 있는 type들을 가져오기
     * - AI 구독을 하지 않는 경우에는 아래 type은 제외하기
     *     - AI_PREDICTION_BILL_EXCEEDED
     *
     * @param readAlarmRequestDto 알람 정보 조회 조건을 포함하는 DTO
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 알람 목록과 관련된 추가 정보를 포함하는 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public AlarmDto.ReadAlarmPageResponse read(AlarmDto.ReadAlarmRequest readAlarmRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var member = jwtUtil.getCurrentMember();
        // 지연 로딩 문제를 해결하기 위해 Company와 Country를 명시적으로 로드
        var company = companyRepository.findById(member.getCompany().getId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        Hibernate.initialize(company.getCountry());
        var zoneId = company.getCountry().getZoneId();
        // AI 구독 여부 확인
        boolean isAiSubscribed = !subscriptionRepository.findAll(SubscriptionSpecification.findWith(
                SubscriptionDto.ReadSubscriptionRequest.builder()
                        .companyId(company.getId())
                        .serviceList(List.of(SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST))
                        .searchDate(LocalDate.now())
                        .build(),
                zoneId))
                .isEmpty();
        // AI 구독을 하지 않는 경우 특정 알람 타입(AI_PREDICTION_BILL_EXCEEDED)을 제외
        if (!isAiSubscribed && readAlarmRequestDto.getType() != null) {
            readAlarmRequestDto.setType(
                    readAlarmRequestDto.getType().stream()
                            .filter(type -> type != AlarmType.AI_PREDICTION_BILL_EXCEEDED)
                            .toList()
            );
        }
        // 알람 조회 및 반환
        var alarmPage = alarmRepository.findAll(AlarmSpecification.findWith(readAlarmRequestDto, zoneId), pageable);
        var alarmList = alarmPage.getContent().stream()
                .map(alarm -> new AlarmDto.ReadAlarmResponse(alarm, zoneId))
                .toList();
        return AlarmDto.ReadAlarmPageResponse.builder()
                .alarmList(alarmList)
                .totalElements(alarmPage.getTotalElements())
                .totalPages(alarmPage.getTotalPages())
                .build();
    }

    /**
     * 실시간 알람을 스트리밍하는 메서드.
     * 이 메서드는 서버와 클라이언트 간의 SSE 연결을 통해 실시간 알람을 주기적으로 전송합니다.
     * 주기적으로 최신 알람을 조회하여 클라이언트로 전송합니다.
     *
     * @return SSE 연결을 위한 SseEmitter 객체
     */
    @Override
    public SseEmitter streamAlarm(AlarmDto.ReadAlarmRequest readAlarmRequestDto) {
        // 타임아웃을 30분으로 설정
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        // 현재 SecurityContext를 저장
        var context = SecurityContextHolder.getContext();
        // TransactionTemplate 생성
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        // ExecutorService 생성
        var executor = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
        // 0초 후에 시작하여 5초 간격으로 작업 실행
        var scheduledFuture = executor.scheduleAtFixedRate(() -> {
            try {
                // 스레드 내에서 SecurityContext 수동 설정
                SecurityContextHolder.setContext(context);
                // TransactionTemplate을 사용하여 트랜잭션 시작
                transactionTemplate.execute(status -> {
                    try {
                        // 알람을 조회하고, 클라이언트로 전송
                        var alarms = read(readAlarmRequestDto, Pageable.unpaged());
                        emitter.send(SseEmitter.event()
                                .data(alarms)
                                .name("alarm"));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                        executor.shutdown();
                    }
                    return null;
                });
            } catch (Exception e) {
                // 오류 발생 시 SSE 연결을 종료하고, 스케줄러를 중지
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 55, TimeUnit.MINUTES);
        // 클라이언트가 연결을 종료했을 때 스케줄러를 중지
        emitter.onCompletion(() -> {
            scheduledFuture.cancel(true);
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            scheduledFuture.cancel(true);
            executor.shutdown();
            emitter.complete();
        });
        return emitter;
    }

    /**
     * 알람 수정
     *
     * @param id 수정할 알람의 ID
     * @param updateAlarmDto 알람 수정 정보를 포함하는 DTO
     * @return 수정된 알람 정보를 담고 있는 객체
     */
    @Override
    @Transactional
    public AlarmDto.ReadAlarmResponse update(Long id, AlarmDto.UpdateAlarm updateAlarmDto) {
        // ID로 Alarm 조회, 없으면 예외 발생
        var alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No such alarm."));
        // 호출한 사용자의 권한 확인
        authenticationService.validateCompanyAccess(alarm.getCompany().getId());
        // ZoneId 설정 (기존 Company의 ZoneId)
        ZoneId zoneId = alarm.getCompany().getCountry().getZoneId();
        // DTO에서 전달된 값들로 Alarm 정보 업데이트
        if (updateAlarmDto.getCompanyId() != null) {
            var company = companyRepository.findById(updateAlarmDto.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("No such company."));
            alarm.setCompany(company);
            // Company가 변경된 경우 새로운 ZoneId를 가져옴
            zoneId = company.getCountry().getZoneId();
        }
        Optional.ofNullable(updateAlarmDto.getType()).ifPresent(alarm::setType);
        Optional.ofNullable(updateAlarmDto.getNotify()).ifPresent(alarm::setNotify);
        Optional.ofNullable(updateAlarmDto.getIsRead()).ifPresent(alarm::setIsRead);
        Optional.ofNullable(updateAlarmDto.getPriority()).ifPresent(alarm::setPriority);
        Optional.ofNullable(updateAlarmDto.getMessage()).ifPresent(alarm::setMessage);
        // 만약 expirationDate가 존재한다면, 이를 업체의 타임존에 맞게 UTC로 변환하여 저장
        if (updateAlarmDto.getExpirationDate() != null) {
            var utcExpirationDate = updateAlarmDto.getExpirationDate()
                    .atZone(zoneId)
                    .toInstant();
            alarm.setExpirationDate(utcExpirationDate);
        }
        // 수정된 Alarm 엔티티 저장 후 응답 객체 생성 및 반환
        return new AlarmDto.ReadAlarmResponse(alarmRepository.save(alarm), zoneId);
    }

    /**
     * 알람 삭제
     *
     * @param id 삭제할 알람의 ID
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        // 알람 ID로 삭제, 없으면 예외 발생
        var alarm = alarmRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No such alarm."));
        alarmRepository.delete(alarm);
    }
}