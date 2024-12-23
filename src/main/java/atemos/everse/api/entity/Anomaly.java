package atemos.everse.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Anomaly 설정 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `anomaly` 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Anomaly {
    /**
     * Anomaly 설정의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Anomaly 설정과 관련된 업체 정보를 나타냅니다.
     * - 업체와 Anomaly은 다대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 에너지 사용량의 최소치입니다.
     * - 예: 100
     * - 기본값은 100입니다.
     * - 0 이상의 값만 허용됩니다.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @PositiveOrZero
    private BigDecimal lowestHourlyEnergyUsage;
    /**
     * 에너지 사용량의 최대치입니다.
     * - 예: 10000
     * - 기본값은 10000입니다.
     * - 0 이상의 값만 허용됩니다.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @PositiveOrZero
    private BigDecimal highestHourlyEnergyUsage;
    /**
     * Anomaly 설정의 활성화 여부를 나타냅니다.
     * - 예: true (활성화), false (비활성화)
     */
    @Column(nullable = false)
    private Boolean available;
    /**
     * Anomaly 설정이 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false) // 생성일은 업데이트 시 변경되지 않습니다.
    private Instant createdDate;
    /**
     * Anomaly 설정이 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}