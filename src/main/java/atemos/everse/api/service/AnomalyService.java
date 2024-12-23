package atemos.everse.api.service;

import atemos.everse.api.dto.AnomalyDto;
import org.springframework.data.domain.Pageable;

/**
 * AnomalyService는 이상탐지 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 이상탐지와 관련된 주요 CRUD 작업을 처리하는 메소드를 정의합니다.
 */
public interface AnomalyService {
    /**
     * 새로운 이상탐지를 등록합니다.
     *
     * @param createAnomalyDto 이상탐지를 등록하기 위한 데이터 전송 객체입니다.
     * @return 등록된 이상탐지 정보를 담고 있는 응답 객체입니다.
     */
    AnomalyDto.ReadAnomalyResponse create(AnomalyDto.CreateAnomaly createAnomalyDto);
    /**
     * 조건에 맞는 이상탐지 정보를 조회합니다.
     *
     * @param readAnomalyRequestDto 이상탐지 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조건에 맞는 이상탐지 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    AnomalyDto.ReadAnomalyPageResponse read(AnomalyDto.ReadAnomalyRequest readAnomalyRequestDto, Pageable pageable);
    /**
     * 특정 이상탐지 정보를 수정합니다.
     *
     * @param anomalyId 수정할 이상탐지의 ID입니다.
     * @param updateAnomalyDto 이상탐지 수정 정보를 포함하는 데이터 전송 객체입니다.
     * @return 수정된 이상탐지 정보를 담고 있는 응답 객체입니다.
     */
    AnomalyDto.ReadAnomalyResponse update(Long anomalyId, AnomalyDto.UpdateAnomaly updateAnomalyDto);
    /**
     * 특정 ID에 해당하는 이상탐지를 삭제합니다.
     *
     * @param anomalyId 삭제할 이상탐지의 ID입니다.
     */
    void delete(Long anomalyId);
}