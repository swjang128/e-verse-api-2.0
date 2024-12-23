package atemos.everse.api.entity;

import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static atemos.everse.api.domain.MemberStatus.ACTIVE;

/**
 * 사용자 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `member` 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_email", columnList = "email")
})
@DynamicUpdate
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {
    /**
     * 사용자의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 사용자가 속한 업체입니다.
     * - 사용자과 업체는 다대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 사용자의 이름입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false)
    private String name;
    /**
     * 사용자의 이메일 주소입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false, unique = true)
    private String email;
    /**
     * 사용자의 전화번호입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false, unique = true)
    private String phone;
    /**
     * 사용자의 비밀번호입니다.
     * - 반드시 입력해야 하며, 보안상의 이유로 setter 메소드가 제공됩니다.
     * - 길이는 최대 60자입니다.
     */
    @Setter
    @Column(nullable = false, length = 60)
    private String password;
    /**
     * 비밀번호가 틀린 횟수를 기록합니다.
     * - 기본값은 0으로 설정됩니다.
     * - 5회 이상 틀리면 Status를 LOCKED로 변경합니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    /**
     * 사용자의 역할을 정의합니다.
     * - 역할은 문자열로 저장되며, 예를 들어 ADMIN, USER 등이 있습니다.
     * - 필수 항목입니다.
     */
    @Column(nullable = false, length = 7)
    @Enumerated(EnumType.STRING)
    private MemberRole role;
    /**
     * 계정의 상태입니다.
     * - 기본적으로 ACTIVE로 저장됩니다.
     */
    @Column(nullable = false, length = 14)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MemberStatus status = ACTIVE;
    /**
     * 사용자 정보가 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(updatable = false) // 생성일은 업데이트 시 변경되지 않습니다.
    private Instant createdDate;
    /**
     * 사용자 정보가 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private Instant modifiedDate;
    /**
     * 사용자의 권한 정보를 반환합니다.
     * 스프링 시큐리티에서 사용됩니다.
     *
     * @return 권한 정보 컬렉션
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}