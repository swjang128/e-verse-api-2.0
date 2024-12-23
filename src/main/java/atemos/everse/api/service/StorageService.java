package atemos.everse.api.service;

import atemos.everse.api.dto.StorageDto;

import java.util.List;

/**
 * StorageService는 데이터베이스 사용량 조회 기능을 제공하는 서비스 인터페이스입니다.
 * 이 인터페이스는 데이터베이스 사용량 기능과 관련된 작업을 처리하는 메소드를 정의합니다.
 */
public interface StorageService {
    /**
     * 조건에 맞는 데이터베이스 사용량 정보를 조회합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @return 조건에 맞는 데이터베이스 사용량 정보 목록과 관련된 추가 정보를 포함하는 맵 객체입니다.
     */
    StorageDto.StorageResponse getDataUsageByCompanyId(Long companyId);
}