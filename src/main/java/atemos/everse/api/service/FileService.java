package atemos.everse.api.service;

import atemos.everse.api.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 파일과 관련된 서비스 인터페이스입니다.
 * 이 인터페이스는 파일 업로드, 데이터 추출, 파일 해싱 등의 기능을 제공합니다.
 */
public interface FileService {
    /**
     * 업로드할 파일을 서버에 저장합니다.
     *
     * @param file 업로드할 MultipartFile 객체.
     *             이 파일은 서버에 저장되며, 이후 데이터 추출 또는 해싱에 사용될 수 있습니다.
     */
    void upload(MultipartFile file) throws IOException;
    /**
     * 업로드된 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분과 초가 00인 데이터를 추출합니다.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     *                 해당 파일에서 고정된 조건에 맞는 데이터를 필터링합니다.
     */
    List<FileDto.ExtractFile> extractData(String fileName) throws IOException;
    /**
     * 업로드된 JSON 파일에서 "ITEM_NAME"이 "누적전력량"이고 "TIMESTAMP"의 분과 초가 00인 데이터를 추출 후 테이블(SampleEnergy)에 저장합니다.
     *
     * @param fileName 데이터를 추출할 JSON 파일의 이름.
     *                 해당 파일에서 고정된 조건에 맞는 데이터를 필터링합니다.
     */
    List<FileDto.ExtractFile> saveDataFromFile(String fileName) throws IOException;
}