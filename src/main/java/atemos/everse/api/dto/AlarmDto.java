package atemos.everse.api.dto;

import atemos.everse.api.domain.AlarmPriority;
import atemos.everse.api.domain.AlarmType;
import atemos.everse.api.entity.Alarm;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 알람과 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class AlarmDto {
    /**
     * 알람을 생성할 때 필요한 정보를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateAlarm {
        /**
         * 업체 ID
         */
        private Long companyId;
        /**
         * 알람 유형 (예: 최대 에너지 사용량, 최소 에너지 사용량 등)
         */
        private AlarmType type;
        /**
         * 알림 여부
         */
        private Boolean notify;
        /**
         * 읽음 여부
         */
        private Boolean isRead;
        /**
         * 알람 우선순위 (예: HIGH, MEDIUM, LOW)
         */
        private AlarmPriority priority;
        /**
         * 알람 메시지
         */
        private String message;
        /**
         * 알람 만료일시
         */
        private LocalDateTime expirationDate;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAlarmRequest {
        /**
         * 알림 ID (기본키)입니다.
         */
        @Positive
        private List<Long> alarmId;
        /**
         * 이 알람이 속한 업체를 나타냅니다.
         */
        @Positive
        private Long companyId;
        /**
         * 알람의 유형을 나타냅니다.
         */
        private List<AlarmType> type;
        /**
         * 알람의 수신 여부를 나타냅니다.
         */
        private Boolean notify;
        /**
         * 알람이 읽혔는지 여부를 나타냅니다.
         */
        private Boolean isRead;
        /**
         * 알람의 우선순위를 나타냅니다.
         */
        private List<AlarmPriority> priority;
        /**
         * 알람 메시지입니다.
         * - 예: "에너지 사용량이 최대 설정값보다 많습니다."
         */
        @Size(max = 255)
        private String message;
        /**
         * 알람의 만료 일시를 나타냅니다.
         * - 만료 일시가 설정되어 있지 않을 수도 있습니다.
         */
        private LocalDateTime expirationDate;
        /**
         * 알람 검색 시작 일시입니다.
         */
        private LocalDateTime startDateTime;
        /**
         * 알람 검색 종료 일시입니다.
         */
        private LocalDateTime endDateTime;
        /**
         * 페이지 번호를 나타냅니다. 0 이상의 정수를 갖습니다.
         * 페이지 번호 + 1이 페이지 번호가 됩니다. (ex. 0 = 1페이지)
         * - 예: 0
         */
        @PositiveOrZero
        private Integer page;
        /**
         * 페이지당 row의 개수를 나타냅니다. 1 이상의 자연수를 갖습니다.
         */
        @Positive
        private Integer size;
    }

    /**
     * 알람 조회 결과를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAlarmResponse {
        /**
         * 알람 ID
         */
        private Long alarmId;
        /**
         * 업체 ID
         */
        private Long companyId;
        /**
         * 업체 이름
         */
        private String companyName;
        /**
         * 알람 유형 (예: 최대 에너지 사용량, 최소 에너지 사용량 등)
         */
        private AlarmType type;
        /**
         * 알림 여부
         */
        private Boolean notify;
        /**
         * 읽음 여부
         */
        private Boolean isRead;
        /**
         * 알람 우선순위 (예: HIGH, MEDIUM, LOW)
         */
        private AlarmPriority priority;
        /**
         * 알람 메시지
         */
        private String message;
        /**
         * 알람 만료일시
         */
        private LocalDateTime expirationDate;
        /**
         * 알람 생성일시
         */
        private LocalDateTime createdDate;
        /**
         * 알람 수정일시
         */
        private LocalDateTime modifiedDate;

        /**
         * Alarm 엔티티를 기반으로 ReadAlarmResponse를 생성하는 생성자
         *
         * @param alarm Alarm 엔티티 객체
         */
        public ReadAlarmResponse(Alarm alarm, ZoneId zoneId) {
            this.alarmId = alarm.getId();
            this.companyId = alarm.getCompany().getId();
            this.companyName = alarm.getCompany().getName();
            this.type = alarm.getType();
            this.notify = alarm.getNotify();
            this.isRead = alarm.getIsRead();
            this.priority = alarm.getPriority();
            this.message = alarm.getMessage();
            this.expirationDate = alarm.getExpirationDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.createdDate = alarm.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = alarm.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
        }
    }

    /**
     * 알람 목록과 페이지 정보를 포함하는 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAlarmPageResponse {
        /**
         * 알람 목록
         */
        private List<AlarmDto.ReadAlarmResponse> alarmList;
        /**
         * 전체 알람 수
         */
        private long totalElements;
        /**
         * 전체 페이지 수
         */
        private int totalPages;
    }

    /**
     * 알람을 수정할 때 필요한 정보를 담는 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateAlarm {
        /**
         * 업체 ID (양수값이어야 함)
         */
        @Schema(description = "업체 ID", defaultValue = "1")
        @Positive
        private Long companyId;
        /**
         * 알람 유형 (예: 최대 에너지 사용량, 최소 에너지 사용량 등)
         */
        @Schema(description = "알람 유형", defaultValue = "MAXIMUM_ENERGY_USAGE")
        @Size(max = 20)
        @Enumerated(EnumType.STRING)
        private AlarmType type;
        /**
         * 알림 여부
         */
        @Schema(description = "알림 여부", defaultValue = "true")
        private Boolean notify;
        /**
         * 읽음 여부
         */
        @Schema(description = "읽음 여부", defaultValue = "false")
        private Boolean isRead;
        /**
         * 알람 우선순위 (예: HIGH, MEDIUM, LOW)
         */
        @Schema(description = "알람 우선순위", defaultValue = "MAXIMUM_ENERGY_USAGE")
        @Size(max = 8)
        @Enumerated(EnumType.STRING)
        private AlarmPriority priority;
        /**
         * 알람 메시지 (최대 255자)
         */
        @Schema(description = "알람 메시지", defaultValue = "사용량")
        @Size(max = 255)
        private String message;
        /**
         * 알람 만료일시
         */
        @Schema(description = "알람 만료일", defaultValue = "2024-06-03T00:00:00")
        private LocalDateTime expirationDate;
    }
}