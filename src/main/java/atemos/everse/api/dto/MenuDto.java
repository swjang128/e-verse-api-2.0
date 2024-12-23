package atemos.everse.api.dto;

import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 메뉴 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class MenuDto {
    /**
     * 새로운 메뉴 생성을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateMenu {
        /**
         * 메뉴의 이름을 나타냅니다.
         * - 예: "샘플"
         * - 길이는 1자 이상 50자 이하입니다.
         */
        @Schema(description = "메뉴명", example = "샘플")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 메뉴에 대한 접근 API URL을 나타냅니다.
         * - 예: "/sample"
         */
        @Schema(description = "메뉴 접근 API Url", example = "/sample")
        @Size(max = 30)
        private String url;
        /**
         * 메뉴에 대한 설명을 나타냅니다.
         * - 예: "샘플 화면입니다"
         * - 길이는 1자 이상 255자 이하입니다.
         */
        @Schema(description = "메뉴에 대한 설명", example = "샘플 화면입니다")
        @Size(min = 1, max = 255)
        private String description;
        /**
         * 메뉴 사용 여부를 나타냅니다.
         * - 예: true
         * - 기본값은 true입니다.
         */
        @Schema(description = "메뉴 사용 여부", example = "true")
        private Boolean available;
        /**
         * 메뉴에 접근 가능한 역할 목록을 나타냅니다.
         * - 예: [ADMIN, MANAGER]
         */
        @Schema(description = "접근 가능한 역할 목록", example = "[\"ADMIN\", \"MANAGER\"]")
        private Set<MemberRole> accessibleRoles;
        /**
         * 상위 메뉴의 ID를 나타냅니다.
         * - 최상위 메뉴일 경우 null이 될 수 있습니다.
         */
        @Schema(description = "상위 메뉴 ID")
        @Positive
        private Long parentId;
    }

    /**
     * 메뉴 조회 요청을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMenuRequest {
        /**
         * 메뉴의 ID로 필터링할 수 있습니다.
         * - 예: 1
         */
        @Positive
        private List<Long> menuId;
        /**
         * 메뉴의 이름으로 필터링할 수 있습니다.
         * - 예: "sample"
         */
        @Size(max = 50)
        private String name;
        /**
         * 메뉴 URL로 필터링할 수 있습니다.
         * - 예: "/sample"
         */
        @Size(max = 30)
        private String url;
        /**
         * 메뉴 설명으로 필터링할 수 있습니다.
         * - 예: "Sample Page로 이동"
         */
        @Size(max = 255)
        private String description;
        /**
         * 메뉴 사용 여부로 필터링할 수 있습니다.
         * - 예: true 또는 false
         */
        private Boolean available;
        /**
         * 상위 메뉴 ID로 필터링할 수 있습니다.
         * - 예: [1]
         */
        private List<Long> parentId;
        /**
         * 접근 권한 리스트입니다.
         */
        private List<MemberRole> roles;
    }

    /**
     * 메뉴 조회 응답을 위한 DTO입니다.
     */
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMenuResponse {
        /**
         * 메뉴 ID를 나타냅니다.
         * - 예: 1
         */
        private Long menuId;
        /**
         * 메뉴의 이름을 나타냅니다.
         * - 예: "sample"
         */
        private String name;
        /**
         * 메뉴에 대한 접근 API URL을 나타냅니다.
         * - 예: "/sample"
         */
        private String url;
        /**
         * 메뉴에 대한 설명을 나타냅니다.
         * - 예: "Sample 페이지로 이동"
         */
        private String description;
        /**
         * 메뉴 사용 여부를 나타냅니다.
         * - 예: true 또는 false
         */
        private Boolean available;
        /**
         * 데이터 생성일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime createdDate;
        /**
         * 데이터 수정일을 나타냅니다.
         * - 예: "2024-07-22T14:30:00"
         */
        private LocalDateTime modifiedDate;
        /**
         * 상위 메뉴의 ID를 나타냅니다.
         * - 최상위 메뉴일 경우 null이 될 수 있습니다.
         */
        private Long parentId;
        /**
         * 하위 메뉴 목록을 나타냅니다.
         * - 예: [1, 2, 3]
         */
        private Set<ReadMenuResponse> children;
        /**
         * 메뉴의 깊이(Depth)를 나타냅니다.
         * - 루트 메뉴는 0이며, 하위 메뉴는 상위 메뉴의 깊이 + 1이 됩니다.
         */
        private Integer depth;
        /**
         * Menu 엔티티를 기반으로 DTO를 생성하는 생성자입니다.
         */
        public ReadMenuResponse(Menu menu, ZoneId zoneId) {
            this.menuId = menu.getId();
            this.name = menu.getName();
            this.url = menu.getUrl();
            this.description = menu.getDescription();
            this.available = menu.getAvailable();
            this.createdDate = menu.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.modifiedDate = menu.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
            this.parentId = menu.getParent() != null ? menu.getParent().getId() : null;
            this.depth = menu.getDepth();
            this.children = menu.getChildren() != null ? menu.getChildren().stream()
                    .map(childMenu -> new ReadMenuResponse(childMenu, zoneId))
                    .collect(Collectors.toSet()) : null;
        }
    }

    /**
     * 메뉴를 수정하기 위한 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateMenu {
        /**
         * 메뉴의 이름을 나타냅니다.
         * - 예: "샘플"
         * - 길이는 1자 이상 50자 이하입니다.
         */
        @Schema(description = "메뉴명", example = "샘플")
        @Size(min = 1, max = 50)
        private String name;
        /**
         * 메뉴에 대한 접근 API URL을 나타냅니다.
         * - 예: "/sample"
         */
        @Schema(description = "메뉴 접근 API Url", example = "/sample")
        @Size(max = 30)
        private String url;
        /**
         * 메뉴에 대한 설명을 나타냅니다.
         * - 예: "샘플 화면입니다"
         * - 길이는 1자 이상 255자 이하입니다.
         */
        @Schema(description = "메뉴에 대한 설명", example = "샘플 화면입니다")
        @Size(min = 1, max = 255)
        private String description;
        /**
         * 메뉴 사용 여부를 나타냅니다.
         * - 예: true
         * - 기본값은 true입니다.
         */
        @Schema(description = "메뉴 사용 여부", example = "true")
        private Boolean available;
        /**
         * 메뉴에 접근 가능한 역할 목록을 나타냅니다.
         * - 예: [ADMIN, MANAGER]
         */
        @Schema(description = "접근 가능한 역할 목록", example = "[\"ADMIN\", \"MANAGER\"]")
        private Set<MemberRole> accessibleRoles;
        /**
         * 상위 메뉴의 ID를 나타냅니다.
         * - 최상위 메뉴일 경우 null이 될 수 있습니다.
         */
        @Schema(description = "상위 메뉴 ID")
        @Positive
        private Long parentId;
    }
}