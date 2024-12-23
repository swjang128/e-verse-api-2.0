package atemos.everse.api.service;

import atemos.everse.api.dto.IotDto;
import org.springframework.data.domain.Pageable;

/**
 * IotService는 IoT 장치의 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 * 이 인터페이스는 IoT 장치의 데이터베이스에 대한 CRUD 작업을 정의합니다.
 */
public interface IotService {
    /**
     * IoT 장치를 등록합니다.
     *
     * @param createIotDto IoT 장치를 생성하기 위한 정보가 담긴 데이터 전송 객체입니다.
     * @return 등록된 IoT 장치의 정보가 담긴 응답 객체입니다.
     */
    IotDto.ReadIotResponse create(IotDto.CreateIot createIotDto);
    /**
     * 조건에 맞는 IoT 장치를 조회합니다.
     *
     * @param readIotRequestDto IoT 장치 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조회된 IoT 장치 목록과 페이지 정보를 포함하는 맵 객체입니다.
     */
    IotDto.ReadIotPageResponse read(IotDto.ReadIotRequest readIotRequestDto, Pageable pageable);
    /**
     * 기존의 IoT 장치를 수정합니다.
     *
     * @param iotId 수정할 IoT 장치의 ID입니다.
     * @param updateIotDto IoT 장치 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 수정된 IoT 장치의 정보가 담긴 응답 객체입니다.
     */
    IotDto.ReadIotResponse update(Long iotId, IotDto.UpdateIot updateIotDto);
    /**
     * IoT 장치를 삭제합니다.
     *
     * @param iotId 삭제할 IoT 장치의 ID입니다.
     */
    void delete(Long iotId);
}