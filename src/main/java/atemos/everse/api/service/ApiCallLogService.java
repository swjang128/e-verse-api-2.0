package atemos.everse.api.service;

import atemos.everse.api.dto.ApiCallLogDto;
import org.springframework.data.domain.Pageable;

/**
 * ApiCallLogService는 API 호출 로그와 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 API 호출 로그의 조회, 유료 API 호출 횟수 통계 조회 및 로그 삭제 기능을 제공합니다.
 */
public interface ApiCallLogService {
    /**
     * 주어진 조건에 맞는 API 호출 로그를 조회합니다.
     *
     * @param readApiCallLogRequestDto API 호출 로그 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체로, 결과 목록의 페이지 번호와 크기를 지정합니다.
     * @return 조회된 API 호출 로그 목록과 관련된 추가 정보를 포함하는 맵입니다. 이 맵에는 조회된 로그 목록과 총 페이지 수 등이 포함될 수 있습니다.
     */
    ApiCallLogDto.ReadApiCallLogPageResponse read(ApiCallLogDto.ReadApiCallLogRequest readApiCallLogRequestDto, Pageable pageable);
    /**
     * 월별 및 일별 유료 API 호출 횟수를 조회합니다.
     *
     * @param readApiCallLogRequestDto 유료 API 호출 횟수 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @return 조회된 월별 및 일별 유료 API 호출 횟수 데이터를 포함하는 맵입니다. 이 맵에는 기간별 호출 횟수 통계가 포함될 수 있습니다.
     */
    ApiCallLogDto.ReadChargeableApiCallCountResponse  readChargeableApiCallCount(ApiCallLogDto.ReadApiCallLogRequest readApiCallLogRequestDto);
    /**
     * 특정 기간까지의 API 호출 로그를 삭제합니다.
     *
     * @param deleteApiCallLogRequestDto API 호출 로그 삭제 요청을 위한 데이터 전송 객체입니다. 이 객체에는 삭제할 로그의 기간 및 기타 필요한 정보가 포함됩니다.
     */
    void delete(ApiCallLogDto.DeleteApiCallLogRequest deleteApiCallLogRequestDto);
}