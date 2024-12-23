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
 * Oracle HeatWave에서 학습된 모델을 기반으로 예측된 에너지 사용량을 저장하는 엔티티 클래스입니다.
 * 실제 HeatWave 세팅이 되면 AIForecastEnergy 엔티티를 남기고 이 엔티티는 삭제해주세요!!
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "energy_usage_forecast_model")
public class EnergyUsageForecastModel {
    /**
     * 에너지 예측 모델의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 에너지 예측을 생성한 업체입니다.
     * - 지연 로딩을 사용하여 필요할 때만 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "company_id")
    private Company company;
    /**
     * AI 예측 에너지 사용량입니다.
     * - 예: 17.89 (kWh)
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    @PositiveOrZero
    private BigDecimal forecastUsage = BigDecimal.valueOf(0.0);
    /**
     * 예측이 적용된 시간입니다.
     * - 예: 2024-07-23T09:00:00
     */
    @Column(nullable = false)
    private LocalDateTime forecastTime;
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