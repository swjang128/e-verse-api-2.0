package atemos.everse.api.entity;

import atemos.everse.api.domain.IotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * IoT 상태 이력을 기록하는 엔티티 클래스입니다.
 * 이 엔티티는 IoT 상태 이력을 저장합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IotStatusHistory {
    /**
     * IoT 장비 이력의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 해당 이력 기록이 참조하는 IoT 장비입니다.
     * - 지연 로딩으로 설정되어 필요할 때만 로딩됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iot_id")
    private Iot iot;
    /**
     * IoT 장비의 상태입니다.
     * - 예: NORMAL, ERROR
     */
    @Column(nullable = false, length = 6)
    @Enumerated(EnumType.STRING)
    private IotStatus status;
    /**
     * 이 이력 기록이 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdDate;
}