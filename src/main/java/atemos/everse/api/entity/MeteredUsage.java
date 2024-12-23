package atemos.everse.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 서비스 사용 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `metered_usage` 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeteredUsage {
    /**
     * 서비스 사용 정보의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 서비스 사용 정보가 속한 업체입니다.
     * - 서비스 사용 정보과 업체는 다대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    /**
     * 서비스 사용 정보의 기준 날짜입니다.
     * 해당 일을 기준으로 API Call 개수, 스토리지 사용 용량, IoT 설비 설치 개수, AI 예측 서비스 사용 여부 컬럼의 데이터가 담깁니다.
     * - 반드시 입력해야 하며, YYYY-MM-DD로 들어갑니다.
     */
    @Column(nullable = false)
    private LocalDate usageDate;
    /**
     * 유료 API Call 횟수입니다.
     * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
     */
    @Column(nullable = false)
    @Builder.Default
    @PositiveOrZero
    private Long apiCallCount = 0L;
    /**
     * IoT 설비 설치 개수
     * - 반드시 입력해야 하며, 0 이상의 양수가 들어갑니다.
     */
    @Column(nullable = false)
    @Builder.Default
    @PositiveOrZero
    private Integer iotInstallationCount = 0;
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