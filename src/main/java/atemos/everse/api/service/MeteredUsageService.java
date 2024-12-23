package atemos.everse.api.service;

import atemos.everse.api.dto.MeteredUsageDto;
import org.springframework.data.domain.Pageable;

/**
 * MeteredUsageService는 서비스 사용 정보 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 서비스 사용 정보와 관련된 주요 CRUD 작업을 처리하는 메소드를 정의합니다.
 */
public interface MeteredUsageService {
    /**
     * 조건에 맞는 서비스 사용 정보 정보를 조회합니다.
     *
     * @param readMeteredUsageRequestDto 서비스 사용 정보 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 서비스 사용 정보 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    MeteredUsageDto.ReadMeteredUsagePageResponse read(MeteredUsageDto.ReadMeteredUsageRequest readMeteredUsageRequestDto, Pageable pageable);
    /**
     * 특정 서비스 사용 정보 정보를 수정합니다.
     *
     * @param meteredUsageId 수정할 서비스 사용 정보의 ID입니다.
     * @param updateMeteredUsageDto 서비스 사용 정보 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정한 서비스 사용 요금 정보
     */
    MeteredUsageDto.ReadMeteredUsageResponse update(Long meteredUsageId, MeteredUsageDto.UpdateMeteredUsage updateMeteredUsageDto);
    /**
     * 특정 ID에 해당하는 서비스 사용 정보를 삭제합니다.
     *
     * @param meteredUsageId 삭제할 서비스 사용 정보의 ID입니다.
     */
    void delete(Long meteredUsageId);
}