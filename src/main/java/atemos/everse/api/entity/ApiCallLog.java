package atemos.everse.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * API 호출 로그를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ApiCallLog {
    /**
     * 로그 ID (기본키)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이벤트가 발생한 사용자를 나타냅니다.
     * - 연관된 사용자를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    /**
     * 호출이 발생한 업체를 나타냅니다.
     * - 연관된 업체를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * API 호출 경로를 나타냅니다.
     * - 예: "/atemos/energy"
     */
    @Column(nullable = false)
    private String apiPath;
    /**
     * HTTP 메서드 (GET, POST 등)를 나타냅니다.
     * - 예: "GET", "POST"
     */
    @Column(nullable = false)
    private String httpMethod;
    /**
     * 요청 시간을 자동으로 기록합니다.
     * - 업데이트되지 않는 값입니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant requestTime;
    /**
     * API 응답 상태 코드를 나타냅니다.
     * - 예: 200, 404, 500
     */
    @Column(nullable = false)
    private Integer statusCode;
    /**
     * 클라이언트의 IP 주소를 나타냅니다.
     * - 예: "127.0.0.1"
     */
    @Column(nullable = false, length = 45)
    private String clientIp;
    /**
     * 추가적인 메타데이터를 저장합니다.
     * - 예: 요청 헤더, 파라미터 등
     */
    @Column(columnDefinition = "TEXT")
    private String metaData;
    /**
     * 과금 여부를 나타냅니다.
     * - 예: true (과금), false (비과금)
     */
    @Column(nullable = false)
    private Boolean isCharge;
}