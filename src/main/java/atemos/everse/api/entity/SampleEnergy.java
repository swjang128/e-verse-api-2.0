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
 * 샘플 에너지 사용량 및 관련 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SampleEnergy {
    /**
     * 샘플 에너지 사용량의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 샘플 에너지 사용량입니다.
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