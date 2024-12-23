package atemos.everse.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * AI 예측 에너지 사용량 및 관련 정보를 나타내는 엔티티 클래스입니다.
 * 이 클래스는 회사의 AI 예측 에너지 사용량 등을 관리합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ai_forecast_energy")
public class AIForecastEnergy {
    /**
     * AI 예측 에너지 사용량의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * AI 예측 에너지를 사용하는 업체입니다.
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
     * AI 예측 기준일시입니다.
     * - 예: 2024-07-23T09:00:00
     */
    @Column
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