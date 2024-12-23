package atemos.everse.api.service;

import atemos.everse.api.dto.FileDto;
import atemos.everse.api.entity.SampleEnergy;
import atemos.everse.api.repository.SampleEnergyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final SampleEnergyRepository sampleEnergyRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 업로드할 파일을 서버에 저장합니다.
     *
     * @param file 업로드할 MultipartFile 객체.
     *             이 파일은 서버에 저장되며, 이후 데이터 추출 또는 해싱에 사용될 수 있습니다.
     */
    @Override
    public void upload(MultipartFile file) throws IOException {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IOException("업로드할 파일이 비어 있습니다.");
        }
        // 업로드 경로 설정 및 디렉토리 생성
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists() && !uploadPath.mkdirs()) {
            throw new IllegalStateException("업로드 경로를 생성할 수 없습니다: " + uploadDir);
        }
        // 파일 저장 경로 설정 및 파일 저장
        file.transferTo(new File(uploadPath, Objects.requireNonNull(file.getOriginalFilename())));
    }

    /**
     * 업로드된 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분과 초가 00인 데이터를 추출 후 csv 파일로 다운로드합니다.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     *                 해당 파일에서 고정된 조건에 맞는 데이터를 필터링합니다.
     */
    @Override
    public List<FileDto.ExtractFile> extractData(String fileName) throws IOException {
        // JSON 파일 경로 설정
        String filePath = uploadDir + File.separator + fileName;
        // ObjectMapper 초기화
        ObjectMapper objectMapper = new ObjectMapper();
        List<FileDto.ExtractFile> extractedData;
        // JSON 파일을 읽어서 전체 객체로 변환
        Map<String, Object> jsonData = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
        // "data" 배열을 가져옴
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonData.get("data");
        // 필터링 후 데이터 계산
        extractedData = new ArrayList<>();
        BigDecimal previousValue = null;
        for (Map<String, Object> data : dataList) {
            if ("누적전력량".equals(data.get("ITEM_NAME"))) {
                String timestampStr = (String) data.get("TIMESTAMP");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if (timestamp.getMinute() == 0 && timestamp.getMonthValue() == 11) {
                    BigDecimal currentValue = new BigDecimal(data.get("ITEM_VALUE").toString());
                    BigDecimal increaseAmount;
                    // 첫 번째 항목의 경우 증가량을 0으로 설정
                    if (previousValue == null) {
                        increaseAmount = BigDecimal.ZERO;
                    } else {
                        increaseAmount = currentValue.subtract(previousValue); // 증가량 계산
                    }
                    previousValue = currentValue; // 이전 값 업데이트
                    // 추출된 데이터 객체 생성
                    extractedData.add(FileDto.ExtractFile.builder()
                            .itemName((String) data.get("ITEM_NAME"))
                            .itemValue(currentValue)  // 원래 ITEM_VALUE
                            .increaseFromPrevious(increaseAmount)  // 증가량
                            .timestamp(timestamp)
                            .build());
                }
            }
        }
        return extractedData;
    }

    /**
     * 업로드된 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분과 초가 00인 데이터를 추출 후 테이블(SampleEnergy)에 저장합니다.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     *                 해당 파일에서 고정된 조건에 맞는 데이터를 필터링합니다.
     */
    @Override
    public List<FileDto.ExtractFile> saveDataFromFile(String fileName) throws IOException {
        // 데이터 추출
        List<FileDto.ExtractFile> extractedData = extractData(fileName);
        // SampleEnergy 엔티티로 변환 후 저장
        extractedData.forEach(data -> {
            SampleEnergy sampleEnergy = SampleEnergy.builder()
                    .facilityUsage(data.getIncreaseFromPrevious()) // 증가량을 facilityUsage에 저장
                    .referenceTime(data.getTimestamp()) // referenceTime에 타임스탬프 저장
                    .build();
            sampleEnergyRepository.save(sampleEnergy);
        });
        return extractedData;
    }
}