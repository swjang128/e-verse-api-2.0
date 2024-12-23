package atemos.everse.api.entity;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.domain.IotType;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * IoT 장비 정보를 나타내는 엔티티 클래스입니다.
 * 이 엔티티는 IoT 장비의 기본 정보와 관련된 다양한 필드를 포함합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Iot {
    /**
     * IoT 장비의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 IoT 장비가 속한 업체입니다.
     * - 지연 로딩으로 설정되어 필요할 때만 로딩됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * IoT 장비의 시리얼 넘버입니다.
     * - 최소 2자, 최대 30자까지 허용됩니다.
     */
    @Column(nullable = false, length = 30)
    @Size(min = 2, max = 30)
    private String serialNumber;
    /**
     * IoT 장비의 상태입니다.
     * - 예: NORMAL, ERROR
     */
    @Column(nullable = false, length = 6)
    @Enumerated(EnumType.STRING)
    private IotStatus status;
    /**
     * IoT 장비의 유형입니다.
     * - 예: "AIR_CONDITIONER", "MOTOR", "RADIATOR", "ETC"
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IotType type;
    /**
     * IoT 장비의 설치 위치입니다.
     * - 최대 50자까지 허용됩니다.
     */
    @Column(length = 50)
    private String location;
    /**
     * IoT 장비의 단가입니다.
     * - 기본값은 0입니다.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    @PositiveOrZero
    private BigDecimal price = BigDecimal.valueOf(0.0);
    /**
     * IoT 장비가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;
    /**
     * IoT 장비 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}