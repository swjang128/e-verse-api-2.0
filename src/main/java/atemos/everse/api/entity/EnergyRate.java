package atemos.everse.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 국가별 전력 요금 증감율을 나타내는 엔티티입니다.
 * 피크, 경피크, 비피크 시간대에 대한 요금 비율을 관리합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "energy_rate", uniqueConstraints = @UniqueConstraint(columnNames = "country_id"))
public class EnergyRate {
    /**
     * 요금 엔티티의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 해당 요금이 적용되는 국가입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
    /**
     * 산업용 전력 사용 요금입니다.
     * 예시: 0.7823(kWh당 0.78$를 나타냄).
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal industrialRate;
    /**
     * 상업용 전력 사용 요금입니다.
     * 예시: 0.6319 (kWh당 0.63$을 나타냄).
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal commercialRate;
    /**
     * 피크 시간대 요금 증감율입니다.
     * 예시: 1.5 (기본 요금의 1.5배를 의미)
     */
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal peakMultiplier;
    /**
     * 경피크(중간 피크) 시간대 요금 증감율입니다.
     * 예시: 1.2 (기본 요금의 1.2배를 의미)
     */
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal midPeakMultiplier;
    /**
     * 비피크(할인) 시간대 요금 증감율입니다.
     * 예시: 0.8 (기본 요금의 0.8배를 의미)
     */
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal offPeakMultiplier;
    /**
     * 피크 시간대 리스트입니다.
     * 예: [10, 11, 17, 18, 19]
     */
    @ElementCollection
    @CollectionTable(name = "peak_hours", joinColumns = @JoinColumn(name = "energy_rate_id"))
    @Column(name = "hour")
    private List<Integer> peakHours;
    /**
     * 경피크 시간대 리스트입니다.
     * 예: [8, 9, 12, 13, 14, 15, 16]
     */
    @ElementCollection
    @CollectionTable(name = "mid_peak_hours", joinColumns = @JoinColumn(name = "energy_rate_id"))
    @Column(name = "hour")
    private List<Integer> midPeakHours;
    /**
     * 비피크 시간대 리스트입니다.
     * 예: [0, 1, 2, 3, 4, 5, 6, 7, 20, 21, 22, 23]
     */
    @ElementCollection
    @CollectionTable(name = "off_peak_hours", joinColumns = @JoinColumn(name = "energy_rate_id"))
    @Column(name = "hour")
    private List<Integer> offPeakHours;
    /**
     * 데이터 생성 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;
    /**
     * 데이터 수정 일시입니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}