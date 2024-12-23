package atemos.everse.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 에너지 사용량 및 관련 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 회사의 에너지 사용량 등을 관리합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Energy {
    /**
     * 에너지 사용량의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 에너지를 수집하는 IoT 장비입니다.
     * - 지연 로딩을 사용하여 필요할 때만 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iot_id")
    private Iot iot;
    /**
     * 에너지 사용량입니다.
     * - 예: 15.22 (kWh)
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    @PositiveOrZero
    private BigDecimal facilityUsage = BigDecimal.valueOf(0.0);
    /**
     * 기준 시각입니다.
     * 이 필드는 수동으로 설정하는 시각입니다.
     */
    @Column(nullable = false)
    private LocalDateTime referenceTime;
    /**
     * 데이터 생성 일시입니다.
     * - 수정할 수 없습니다.
     */
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdDate;
    /**
     * 데이터 수정 일시입니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}