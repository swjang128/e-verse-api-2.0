package atemos.everse.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.ZoneId;

/**
 * 국가의 정보를 담는 엔티티입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Country {
    /**
     * 국가의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 국가의 이름입니다.
     * 유일키 속성을 갖습니다.
     * 예시: "Korea", "USA", "Thailand", "Vietnam".
     */
    @Column(unique = true, nullable = false, length = 20)
    private String name;
    /**
     * 국가의 언어 코드를 나타냅니다.
     * 유일키 속성을 갖습니다.
     * 예시: "ko-KR", "en-US", "th-TH", "vi-VN".
     */
    @Column(name = "language_code", unique = true, nullable = false, length = 5)
    private String languageCode;
    /**
     * 국가의 타임존 정보입니다.
     * 예시: "Asia/Seoul", "America/New_York".
     */
    @Column(nullable = false, length = 50)
    private String timeZone;
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
    /**
     * 국가의 타임존을 ZoneId 객체로 반환합니다.
     * @return ZoneId 객체
     */
    public ZoneId getZoneId() {
        return ZoneId.of(this.timeZone);
    }
}
