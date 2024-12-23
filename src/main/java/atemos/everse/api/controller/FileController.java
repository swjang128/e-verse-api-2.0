package atemos.everse.api.controller;

import atemos.everse.api.config.ApiResponseManager;
import atemos.everse.api.dto.ApiResponseDto;
import atemos.everse.api.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일을 업로드하고 업로드한 파일의 데이터에서 필요한 데이터만 추출하여 다운로드할 수 있는 API 컨트롤러.
 * 이 클래스는 파일 업로드와 업로드한 파일의 데이터에서 필요한 데이터를 추출하는 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "파일 업로드 및 데이터 추출 API", description = "파일 업로드 및 데이터 추출 API 모음")
public class FileController {
    private final ApiResponseManager apiResponseManager;
    private final FileService fileService;

    /**
     * 업로드된 파일을 서버에 저장하는 API.
     *
     * @param file 업로드할 MultipartFile 객체. 파일의 크기 및 형식은 서비스에서 검증됩니다.
     * @return ResponseEntity<ApiResponseDto> 성공적으로 파일이 업로드된 경우 200 OK 상태와 함께 빈 응답을 반환합니다.
     */
    @Operation(summary = "파일 업로드", description = "서버에 파일을 업로드합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto> uploadFile(
            @Parameter(description = "업로드할 파일", required = true) @RequestParam MultipartFile file
    ) throws IOException {
        fileService.upload(file);
        return apiResponseManager.ok();
    }

    /**
     * 업로드한 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분이 00이고 월이 11인 데이터를 추출하는 API.
     * 추출된 데이터와 해시 값을 반환합니다.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     * @return ResponseEntity<ApiResponseDto> 추출된 데이터와 해시 값을 포함한 응답을 반환합니다.
     *         성공적으로 데이터를 추출한 경우 200 OK 상태와 함께 추출된 데이터와 해시 값을 반환합니다.
     */
    @Operation(summary = "특정 조건의 데이터 추출 및 해싱", description = "업로드된 JSON 파일에서 '누적전력량' 데이터를 추출하고 파일의 해시 값을 반환합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/extract")
    public ResponseEntity<ApiResponseDto> extractData(
            @Parameter(description = "데이터를 추출할 파일의 이름", example = "sample.json") @RequestParam String fileName
    ) throws IOException {
        return apiResponseManager.success(fileService.extractData(fileName));
    }

    /**
     * 업로드한 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분이 00이고 월이 11인 데이터를 추출하여 SampleEnergy 테이블에 담는 API.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     * @return ResponseEntity<ApiResponseDto> 테이블에 Save한 데이터를 포함한 응답을 반환합니다.
     *         성공적으로 데이터를 추출한 경우 200 OK 상태와 함께 추출된 데이터와 해시 값을 반환합니다.
     */
    @Operation(summary = "특정 조건의 데이터 추출 및 테이블 적재", description = "업로드된 JSON 파일에서 '누적전력량' 데이터를 추출하고 파일의 해시 값을 테이블에 적재합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> saveDataFromFile(
            @Parameter(description = "데이터를 추출할 파일의 이름", example = "sample.json") @RequestParam String fileName
    ) throws IOException {
        return apiResponseManager.success(fileService.saveDataFromFile(fileName));
    }
}