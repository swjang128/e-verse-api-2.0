package atemos.everse.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 파일 관련 데이터 전송 객체(DTO) 클래스입니다.
 * 이 클래스는 업로드된 JSON 파일에서 추출된 데이터를 표현합니다.
 */
public class FileDto {
    /**
     * 추출된 파일 데이터 항목을 나타내는 정적 클래스입니다.
     * 각 데이터 항목은 이름, 값 및 타임스탬프를 포함합니다.
     */
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtractFile {
        private String itemName;
        private BigDecimal itemValue;
        private BigDecimal increaseFromPrevious;
        private LocalDateTime timestamp;
    }
}