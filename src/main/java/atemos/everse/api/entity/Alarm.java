package atemos.everse.api.entity;

import atemos.everse.api.domain.AlarmPriority;
import atemos.everse.api.domain.AlarmType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 알람을 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alarm {
    /**
     * 알람 ID (기본키)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 알람이 속한 업체를 나타냅니다.
     * - 연관된 업체를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 알람의 유형을 나타냅니다.
     * - 예: "MAXIMUM_ENERGY_USAGE", "MINIMUM_ENERGY_USAGE"
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AlarmType type;
    /**
     * 알람의 수신 여부를 나타냅니다.
     * - 기본값은 false입니다.
     */
    @Column(nullable = false)
    private Boolean notify;
    /**
     * 알람이 읽혔는지 여부를 나타냅니다.
     * - 기본값은 false입니다.
     */
    @Column(nullable = false)
    private Boolean isRead;
    /**
     * 알람의 우선순위를 나타냅니다.
     * - 예: "HIGH", "MEDIUM", "LOW"
     */
    @Column(nullable = false, length = 6)
    @Enumerated(EnumType.STRING)
    private AlarmPriority priority;
    /**
     * 알람 메시지입니다.
     * - 예: "에너지 사용량이 최대 설정값보다 많습니다."
     */
    @Column(nullable = false)
    private String message;
    /**
     * 알람의 만료 일시를 나타냅니다.
     * - 만료 일시가 설정되어 있지 않을 수도 있습니다.
     */
    @Column
    private Instant expirationDate;
    /**
     * 알람 생성 일시입니다.
     * - 업데이트되지 않는 값입니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;
    /**
     * 알람 수정 일시입니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}