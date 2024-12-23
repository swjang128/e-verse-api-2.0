package atemos.everse.api.entity;

import atemos.everse.api.domain.PaymentMethod;
import atemos.everse.api.domain.PaymentStatus;
import atemos.everse.api.domain.SubscriptionServiceList;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 결제 내역을 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `payment` 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    /**
     * 결제 내역의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 결제 내역과 관련된 업체입니다.
     * - 업체와 결제 내역은 다대일 관계입니다.
     * - 업체는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 결제 내역과 관련된 서비스 사용 내역입니다.
     * - 서비스 사용 내역과 결제 내역은 다대일 관계입니다.
     * - 서비스 사용 내역은 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metered_usage_id")
    private MeteredUsage meteredUsage;
    /**
     * 결제 내역과 관련된 해당 일자에 구독하던 서비스 목록입니다.
     * - 구독하던 서비스 목록은 null이 될 수 있습니다.
     */
    @ElementCollection(targetClass = SubscriptionServiceList.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "payment_subscription_services", joinColumns = @JoinColumn(name = "payment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_service")
    private List<SubscriptionServiceList> subscriptionServiceList;
    /**
     * 해당 업체의 데이터베이스 저장소 사용량입니다.
     * - 단위는 Byte 단위입니다.
     * - 해당 시점의 데이터베이스 저장소 사용량을 담습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    @PositiveOrZero
    private Long storageUsage = 0L;
    /**
     * 결제 방법을 나타냅니다.
     * - 예: "CARD" (카드 결제), "TRANSFER" (이체)
     * - 최대 8자까지 허용됩니다.
     */
    @Column(nullable = false, length = 8)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    /**
     * 지불할 금액을 나타냅니다.
     * - 예: 50000.00
     * - 0 이상의 실수만 허용됩니다.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    @PositiveOrZero
    private BigDecimal amount = BigDecimal.valueOf(0.0);
    /**
     * 결제 상태를 나타냅니다.
     * - 예: "OUTSTANDING" (미결제), "COMPLETE" (완료)
     * - 최대 12자까지 허용됩니다.
     */
    @Column(nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    /**
     * 결제 내역의 기준 사용 날짜를 나타냅니다.
     * - 예: 2024-08-01
     */
    @Column(nullable = false)
    private LocalDate usageDate;
    /**
     * 결제 예정일을 나타냅니다.
     * - 결제 시스템이 다음 결제 날짜를 관리하는데 사용됩니다.
     * - 예: 2024-08-25
     */
    @Column(nullable = false)
    private LocalDate scheduledPaymentDate;
    /**
     * 결제 정보가 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;
    /**
     * 결제 정보가 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}