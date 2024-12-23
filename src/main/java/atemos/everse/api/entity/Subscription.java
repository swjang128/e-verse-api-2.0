package atemos.everse.api.entity;

import atemos.everse.api.domain.SubscriptionServiceList;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 서비스 구독 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `subscription` 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Subscription {
    /**
     * 구독 정보의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 구독 정보가 속한 업체입니다.
     * - 구독 정보와 업체는 다대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    /**
     * 구독한 서비스입니다.
     * - 예: "AI_ENERGY_USAGE_FORECAST"
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Size(max = 30)
    private SubscriptionServiceList service;
    /**
     * 구독 시작 날짜입니다.
     * - 반드시 입력해야 하며, YYYY-MM-DD 형식입니다.
     */
    @Column(nullable = false)
    private LocalDate startDate;
    /**
     * 구독 종료 날짜입니다.
     * - 구독이 종료되지 않았다면 null일 수 있습니다.
     */
    @Column
    private LocalDate endDate;
    /**
     * row가 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;
    /**
     * row가 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}