package atemos.everse.api.service;

import atemos.everse.api.dto.EnergyRateDto;
import org.springframework.data.domain.Pageable;

/**
 * EnergyRateServiceImpl 클래스는 에너지 요금의 생성, 조회, 수정, 삭제 등의 기능을 정의하는 인터페이스입니다.
 */
public interface EnergyRateService {
    /**
     * 에너지 요금을 생성합니다.
     *
     * @param createEnergyRateDto 에너지 요금 생성 정보를 담은 DTO입니다.
     * @return 생성된 에너지 요금 정보가 담긴 응답 객체입니다.
     */
    EnergyRateDto.ReadEnergyRateResponse create(EnergyRateDto.CreateEnergyRate createEnergyRateDto);
    /**
     * 조건에 맞는 에너지 요금을 조회합니다.
     *
     * @param readEnergyRateRequestDto 에너지 요금 조회 요청을 위한 DTO입니다.
     * @param pageable 페이지 정보입니다.
     * @return 조건에 맞는 에너지 요금 목록과 페이지 정보를 포함하는 응답 객체입니다.
     */
    EnergyRateDto.ReadEnergyRatePageResponse read(EnergyRateDto.ReadEnergyRateRequest readEnergyRateRequestDto, Pageable pageable);
    /**
     * 특정 업체의 시간별 에너지 사용 요금을 조회합니다.
     *
     * @param companyId 조회할 업체의 ID
     * @return 시간별 에너지 사용 요금을 담은 응답 객체
     */
    EnergyRateDto.HourlyRatesResponse readHourlyRates(Long companyId);
    /**
     * 기존의 에너지 요금을 수정합니다.
     *
     * @param energyRateId 수정할 에너지 요금의 ID입니다.
     * @param updateEnergyRateDto 에너지 요금 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 수정된 에너지 요금의 정보가 담긴 응답 객체입니다.
     */
    EnergyRateDto.ReadEnergyRateResponse update(Long energyRateId, EnergyRateDto.UpdateEnergyRate updateEnergyRateDto);
    /**
     * 에너지 요금을 삭제합니다.
     *
     * @param energyRateId 삭제할 에너지 요금의 ID입니다.
     */
    void delete(Long energyRateId);
}