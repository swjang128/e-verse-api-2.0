package atemos.everse.api.service;

import atemos.everse.api.dto.IotStatusHistoryDto;
import org.springframework.data.domain.Pageable;

/**
 * IotHistoryService는 IoT 현황의 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 IoT 현황을 데이터베이스에 등록하고, 조건에 맞는 IoT 현황을 조회하며, 기존 IoT 현황을 수정하거나 삭제하는 기능을 정의합니다.
 */
public interface IotStatusHistoryService {
    /**
     * 조건에 맞는 IoT 현황을 조회합니다.
     *
     * @param readIotHistoryRequestDto IoT 현황 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조회된 IoT 현황 목록과 페이지 정보를 포함하는 객체입니다.
     */
    IotStatusHistoryDto.ReadIotHistoryPageResponse read(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto, Pageable pageable);
    /**
     * 특정 업체의 특정 기간 내 시간별 IoT 상태 이력 조회 API.
     *
     * @param readIotHistoryRequestDto IoT 현황 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @return 특정 업체의 특정 날짜 범위에 해당하는 IoT 상태 이력 데이터
     */
    IotStatusHistoryDto.ReadIotHistoryPageResponse readByCompanyId(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto);
    /**
     * IoT 현황을 삭제합니다.
     *
     * @param id 삭제할 IoT 현황의 ID입니다.
     */
    void delete(Long id);
}