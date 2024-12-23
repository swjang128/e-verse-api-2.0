package atemos.everse.api.entity;

import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.SubscriptionServiceList;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

/**
 * 메뉴 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `menu` 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Menu {
    /**
     * 메뉴의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 메뉴의 이름입니다.
     * - 반드시 입력해야 하며, 유일해야 합니다.
     * - 길이는 1자 이상 50자 이하입니다.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    /**
     * 메뉴에 대한 접근 API URL입니다.
     * - 반드시 입력해야 하며, 유일해야 합니다.
     * - 길이는 최대 30자입니다.
     */
    @Column(nullable = false, unique = true, length = 30)
    private String url;
    /**
     * 메뉴에 대한 설명입니다.
     * - 반드시 입력해야 하며, 길이는 1자 이상 255자 이하입니다.
     */
    @Column(nullable = false)
    private String description;
    /**
     * 메뉴 사용 여부를 나타냅니다.
     * - true이면 메뉴가 활성화되고, false이면 비활성화됩니다.
     */
    @Column(nullable = false)
    private Boolean available;
    /**
     * 이 메뉴에 접근 가능한 역할 목록입니다.
     * - 여러 역할이 하나의 메뉴에 접근할 수 있습니다.
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = MemberRole.class)
    @CollectionTable(name = "menu_roles", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<MemberRole> accessibleRoles;
    /**
     * 메뉴의 깊이(Depth)를 나타냅니다.
     * - 루트 메뉴의 깊이는 0으로 시작하며, 하위 메뉴는 상위 메뉴의 깊이 + 1이 됩니다.
     */
    @Column(nullable = false)
    private Integer depth;
    /**
     * 상위 메뉴에 대한 참조입니다.
     * - 최상위 메뉴의 경우, 이 필드는 null이 됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;
    /**
     * 하위 메뉴 목록에 대한 참조입니다.
     * - 이 메뉴의 하위 메뉴들입니다.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Menu> children;
    /**
     * 이 메뉴에 접근하기 위해 필요한 구독 서비스입니다.
     * - 특정 서비스에 구독한 사용자만 이 메뉴에 접근할 수 있습니다.
     * - 이 필드는 null일 수 있으며, null인 경우 구독이 없어도 접근 가능합니다.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private SubscriptionServiceList requiredSubscription;
    /**
     * 메뉴가 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false) // 생성일은 업데이트 시 변경되지 않습니다.
    private Instant createdDate;
    /**
     * 메뉴가 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
}