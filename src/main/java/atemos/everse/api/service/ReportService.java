package atemos.everse.api.service;

import atemos.everse.api.dto.IotStatusHistoryDto;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ReportService는 업체의 에너지 사용량과 요금 등의 데이터를 엑셀로 다운로드하는 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface ReportService {
    /**
     * 특정 기간 내 업체의 에너지 사용량 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 우선 월별 - 일별 - 시간별로 나열하고 최하단에 전체 데이터를 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량 등을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportEnergyUsage(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response);
    /**
     * 특정 기간 내 업체의 에너지 요금 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 우선 월별 - 일별 - 시간별로 나열하고 최하단에 전체 데이터를 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportEnergyBill(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response);
    /**
     * 특정 기간 내 업체의 에너지 사용량과 요금 데이터를 엑셀 데이터로 제공합니다.
     * EnergyServiceImpl.readEnergy 메서드에서 가져온 데이터를
     * 시간별 데이터 < 일별 종합 < 월별 종합 < 전체 종합 형식으로 보여줍니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량과 요금을 엑셀 파일로 제공할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다. null인 경우 startDate와 동일하게 설정하여 특정일 조회로 처리합니다.
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportEnergyUsageAndBill(Long companyId, LocalDate startDate, LocalDate endDate, HttpServletResponse response);
    /**
     * 특정 기간 내 업체의 IoT 상태 이력을 엑셀 파일로 제공합니다. (조회 결과는 시간별로 집계됩니다)
     *
     * @param readIotHistoryRequestDto IoT 상태 이력을 조회하기 위한 요청 DTO입니다.
     * @param response  HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportIotStatusHistory(IotStatusHistoryDto.ReadIotHistoryRequest readIotHistoryRequestDto, HttpServletResponse response);
    /**
     * 기간 내 이상 탐지 관련 알람 내역 엑셀 다운로드
     * 기간 내 이상 탐지 관련 알람 내역 엑셀 다운로드 할 수 있습니다.
     *
     * @param companyId 업체 ID
     * @param startDate 알람 생성일시 검색 시작일
     * @param endDate 알람 생성일시 검색 종료일
     * @param response HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportAnomalyAlarms(Long companyId, LocalDateTime startDate, LocalDateTime endDate, HttpServletResponse response);
}