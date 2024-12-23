package atemos.everse.api.entity;

import atemos.everse.api.domain.CompanyType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 업체 정보를 나타내는 엔티티 클래스입니다.
 * 이 엔티티는 업체의 기본 정보와 관련된 다양한 필드를 포함합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Company {
    /**
     * 업체의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 업체가 속한 국가입니다.
     * - 국가는 지연 로딩으로 설정되어 필요할 때만 로딩됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;
    /**
     * 업체명입니다.
     * - 최대 50자까지 허용됩니다.
     */
    @Column(nullable = false, length = 50)
    private String name;
    /**
     * 업체 유형입니다.
     * - 예: "FEMS", "BEMS" 등
     */
    @Column(nullable = false, length = 4)
    @Enumerated(EnumType.STRING)
    private CompanyType type;
    /**
     * 업체의 이메일 주소입니다.
     * - 유일성을 보장합니다.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String email;
    /**
     * 업체의 전화번호입니다.
     * - 11자리 숫자 형식입니다.
     */
    @Column(nullable = false, unique = true, length = 11)
    private String tel;
    /**
     * 업체의 팩스 번호입니다.
     * - 11자리 숫자 형식입니다.
     */
    @Column(nullable = false, length = 11)
    private String fax;
    /**
     * 업체의 주소입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String address;
    /**
     * 업체가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;
    /**
     * 업체 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}