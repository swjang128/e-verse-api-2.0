package atemos.everse.api.service;

import atemos.everse.api.dto.AlarmDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AlarmService는 알람과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 이 인터페이스는 알람의 생성, 조회, 수정 및 삭제와 관련된 기능을 정의합니다. 각 메소드는
 * 알람 관련 데이터 전송 객체(DTO)를 사용하여 필요한 작업을 수행하며, 결과를 적절한 형태로 반환합니다.
 */
public interface AlarmService {
    /**
     * 대시보드의 알람에 표시할 데이터 조회 API
     * 대시보드의 알람에 표시할 내용을 조회할 수 있습니다.
     * - AI 구독을 하는 경우에는 모든 type을 가져오기
     * - AI 구독을 하지 않는 경우에는 아래 type만 가져오기
     *     - MAXIMUM_ENERGY_USAGE
     *     - MINIMUM_ENERGY_USAGE
     *
     * @param readAlarmRequestDto 알람 정보 조회 조건을 포함하는 DTO
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 알람 목록과 관련된 추가 정보를 포함하는 응답 객체
     */
    AlarmDto.ReadAlarmPageResponse read(AlarmDto.ReadAlarmRequest readAlarmRequestDto, Pageable pageable);
    /**
     * 실시간 알람을 스트리밍하는 메서드.
     * 클라이언트와의 SSE 연결을 통해 실시간으로 알람을 전송합니다.
     *
     * @return SSE 연결을 위한 SseEmitter 객체
     */
    SseEmitter streamAlarm(AlarmDto.ReadAlarmRequest readAlarmRequestDto);
    /**
     * 알람 수정
     *
     * @param id 수정할 알람의 ID
     * @param updateAlarmDto 알람 수정 정보를 포함하는 데이터 전송 객체
     * @return 수정된 알람 정보를 담고 있는 객체입니다.
     */
    AlarmDto.ReadAlarmResponse update(Long id, AlarmDto.UpdateAlarm updateAlarmDto);
    /**
     * 특정 ID의 알람을 삭제합니다.
     *
     * @param id 삭제할 알람의 고유 ID입니다.
     */
    void delete(Long id);
}