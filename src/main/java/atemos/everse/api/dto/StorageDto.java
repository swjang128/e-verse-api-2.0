package atemos.everse.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Storage 정보 조회 응답 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class StorageDto {
    /**
     * 해당 업체가 사용하는 테이블 별 데이터 사용량입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TableStorageUsage {
        /**
         * 테이블 이름
         */
        private String tableName;
        /**
         * 데이터 사용량
         */
        private Long sizeInBytes;
    }

    /**
     * 스토리지 정보를 조회했을 때 최종 응답 DTO입니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StorageResponse {
        /**
         * 업체 ID
         */
        private Long companyId;
        /**
         * 업체명
         */
        private String companyName;
        /**
         * 테이블별 데이터 사용량 목록
         */
        private List<StorageDto.TableStorageUsage> tableStorageUsageList;
        /**
         * 전체 사용량
         */
        private Long totalStorageUsage;
    }
}