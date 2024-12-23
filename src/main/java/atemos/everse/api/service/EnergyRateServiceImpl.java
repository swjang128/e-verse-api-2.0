package atemos.everse.api.service;

import atemos.everse.api.domain.EnergyRatePeakType;
import atemos.everse.api.dto.EnergyRateDto;
import atemos.everse.api.entity.Country;
import atemos.everse.api.entity.EnergyRate;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.CountryRepository;
import atemos.everse.api.repository.EnergyRateRepository;
import atemos.everse.api.specification.EnergyRateSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * EnergyRateServiceImpl 클래스는 에너지 요금의 생성, 조회, 수정, 삭제 등의 기능을 제공하는 서비스 클래스입니다.
 * 이 클래스는 관리자 권한을 가진 사용자가 에너지 요금을 관리할 수 있도록 다양한 메서드를 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class EnergyRateServiceImpl implements EnergyRateService {
    private final EnergyRateRepository energyRateRepository;
    private final CountryRepository countryRepository;
    private final CompanyRepository companyRepository;

    /**
     * 에너지 요금을 생성합니다.
     *
     * @param createEnergyRateDto 에너지 요금 생성 정보를 담은 DTO입니다.
     * @return 생성된 에너지 요금 정보가 담긴 응답 객체입니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EnergyRateDto.ReadEnergyRateResponse create(EnergyRateDto.CreateEnergyRate createEnergyRateDto) {
        // 해당 국가가 존재하는지 확인
        Country country = countryRepository.findById(createEnergyRateDto.getCountryId())
                .orElseThrow(() -> new EntityNotFoundException("No such country."));
        // 에너지 요금 엔티티 생성 및 저장
        EnergyRate energyRate = EnergyRate.builder()
                .country(country)
                .industrialRate(createEnergyRateDto.getIndustrialRate())
                .commercialRate(createEnergyRateDto.getCommercialRate())
                .peakMultiplier(createEnergyRateDto.getPeakMultiplier())
                .midPeakMultiplier(createEnergyRateDto.getMidPeakMultiplier())
                .offPeakMultiplier(createEnergyRateDto.getOffPeakMultiplier())
                .peakHours(createEnergyRateDto.getPeakHours())
                .midPeakHours(createEnergyRateDto.getMidPeakHours())
                .offPeakHours(createEnergyRateDto.getOffPeakHours())
                .build();
        // 엔티티 저장 및 DTO로 변환하여 반환
        return new EnergyRateDto.ReadEnergyRateResponse(energyRateRepository.save(energyRate));
    }

    /**
     * 조건에 맞는 에너지 요금을 조회합니다.
     *
     * @param readEnergyRateRequestDto 에너지 요금 조회 요청을 위한 DTO입니다.
     * @param pageable 페이지 정보입니다.
     * @return 조건에 맞는 에너지 요금 목록과 페이지 정보를 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public EnergyRateDto.ReadEnergyRatePageResponse read(EnergyRateDto.ReadEnergyRateRequest readEnergyRateRequestDto, Pageable pageable) {
        // 조건에 맞는 에너지 요금 조회
        var energyRatePage = energyRateRepository.findAll(EnergyRateSpecification.findWith(readEnergyRateRequestDto), pageable);
        // 결과를 DTO로 변환하여 반환
        var energyRateList = energyRatePage.getContent().stream()
                .map(EnergyRateDto.ReadEnergyRateResponse::new)
                .toList();
        return new EnergyRateDto.ReadEnergyRatePageResponse(
                energyRateList,
                energyRatePage.getTotalElements(),
                energyRatePage.getTotalPages()
        );
    }

    /**
     * 특정 업체 또는 모든 업체의 시간별 에너지 사용 요금을 조회합니다.
     *
     * @param companyId 조회할 업체의 ID (선택적)
     * @return 시간별 에너지 사용 요금을 담은 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public EnergyRateDto.HourlyRatesResponse readHourlyRates(Long companyId) {
        // 특정 업체의 ID가 주어진 경우
        if (companyId != null) {
            // 주어진 업체 ID로 업체를 조회. 업체가 존재하지 않으면 예외 발생
            var company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new EntityNotFoundException("No such company."));
            // 조회된 업체의 국가에 해당하는 에너지 요금 정보를 조회. 없으면 예외 발생
            var energyRate = energyRateRepository.findByCountry(company.getCountry())
                    .orElseThrow(() -> new EntityNotFoundException("No energy rate found for this country."));
            // 시간대별 요금을 계산
            var hourlyRates = calculateHourlyRates(energyRate);
            // 특정 업체의 시간대별 요금을 응답 리스트에 추가
            var response = EnergyRateDto.HourlyRate.builder()
                    .companyId(companyId)
                    .hourlyRates(hourlyRates)
                    .build();
            // 응답 객체를 리스트 형식으로 반환
            return EnergyRateDto.HourlyRatesResponse.builder()
                    .companyHourlyRates(List.of(response))
                    .build();
        } else {
            // 업체 ID가 주어지지 않은 경우 모든 업체의 에너지 요금을 조회
            var allCompanies = companyRepository.findAll();
            // 모든 업체의 시간대별 요금을 저장할 리스트를 초기화
            var companyHourlyRatesList = new ArrayList<EnergyRateDto.HourlyRate>();
            // 각 업체를 순회하면서 시간대별 요금을 계산
            for (var company : allCompanies) {
                // 각 업체의 국가에 해당하는 에너지 요금을 조회. 없으면 예외 발생
                var energyRate = energyRateRepository.findByCountry(company.getCountry())
                        .orElseThrow(() -> new EntityNotFoundException("No energy rate found for this country."));
                // 시간대별 요금을 계산
                var hourlyRates = calculateHourlyRates(energyRate);
                // 각 업체의 ID와 시간대별 요금을 리스트에 추가
                var companyHourlyRate = EnergyRateDto.HourlyRate.builder()
                        .companyId(company.getId())
                        .hourlyRates(hourlyRates)
                        .build();
                companyHourlyRatesList.add(companyHourlyRate);
            }
            // 모든 업체의 시간대별 요금을 포함한 응답 객체를 반환
            return EnergyRateDto.HourlyRatesResponse.builder()
                    .companyHourlyRates(companyHourlyRatesList)
                    .build();
        }
    }

    /**
     * 주어진 에너지 요금 정보를 기반으로 시간대별 요금을 계산합니다.
     *
     * @param energyRate 에너지 요금 정보를 담은 엔티티
     * @return 시간대별 요금을 담은 맵 (시간대 -> 요금)
     */
    private Map<Integer, EnergyRateDto.HourlyRateDetail> calculateHourlyRates(EnergyRate energyRate) {
        Map<Integer, EnergyRateDto.HourlyRateDetail> hourlyRates = new HashMap<>();
        // 0시부터 23시까지 순회하며 해당 시간대의 요금을 계산합니다.
        for (int hour = 0; hour < 24; hour++) {
            BigDecimal rate;
            EnergyRatePeakType status;
            // 해당 시간대가 피크 시간대인지 확인
            if (energyRate.getPeakHours().contains(hour)) {
                rate = energyRate.getCommercialRate().multiply(energyRate.getPeakMultiplier());
                status = EnergyRatePeakType.PEAK;
            }
            // 해당 시간대가 경피크 시간대인지 확인
            else if (energyRate.getMidPeakHours().contains(hour)) {
                rate = energyRate.getCommercialRate().multiply(energyRate.getMidPeakMultiplier());
                status = EnergyRatePeakType.MID_PEAK;
            }
            // 해당 시간대가 비피크 시간대인지 확인
            else if (energyRate.getOffPeakHours().contains(hour)) {
                rate = energyRate.getCommercialRate().multiply(energyRate.getOffPeakMultiplier());
                status = EnergyRatePeakType.OFF_PEAK;
            }
            // 해당 시간대가 정의되지 않은 경우 기본 상업용 요금을 적용
            else {
                rate = energyRate.getCommercialRate();
                status = EnergyRatePeakType.UNKNOWN;
            }
            // 시간대별 요금과 상태를 맵에 추가
            hourlyRates.put(hour, EnergyRateDto.HourlyRateDetail.builder()
                    .rate(rate)
                    .status(status)
                    .build());
        }
        return hourlyRates;
    }

    /**
     * 기존의 에너지 요금을 수정합니다.
     *
     * @param energyRateId 수정할 에너지 요금의 ID입니다.
     * @param updateEnergyRateDto 에너지 요금 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 수정된 에너지 요금의 정보가 담긴 응답 객체입니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EnergyRateDto.ReadEnergyRateResponse update(Long energyRateId, EnergyRateDto.UpdateEnergyRate updateEnergyRateDto) {
        // 에너지 요금이 존재하는지 확인
        EnergyRate energyRate = energyRateRepository.findById(energyRateId)
                .orElseThrow(() -> new EntityNotFoundException("No such energy rate."));
        // 각 필드를 Optional로 감싸고 값이 있을 때만 설정
        Optional.ofNullable(updateEnergyRateDto.getIndustrialRate()).ifPresent(energyRate::setIndustrialRate);
        Optional.ofNullable(updateEnergyRateDto.getCommercialRate()).ifPresent(energyRate::setCommercialRate);
        Optional.ofNullable(updateEnergyRateDto.getPeakMultiplier()).ifPresent(energyRate::setPeakMultiplier);
        Optional.ofNullable(updateEnergyRateDto.getMidPeakMultiplier()).ifPresent(energyRate::setMidPeakMultiplier);
        Optional.ofNullable(updateEnergyRateDto.getOffPeakMultiplier()).ifPresent(energyRate::setOffPeakMultiplier);
        Optional.ofNullable(updateEnergyRateDto.getPeakHours()).ifPresent(energyRate::setPeakHours);
        Optional.ofNullable(updateEnergyRateDto.getMidPeakHours()).ifPresent(energyRate::setMidPeakHours);
        Optional.ofNullable(updateEnergyRateDto.getOffPeakHours()).ifPresent(energyRate::setOffPeakHours);
        // 엔티티 저장 및 DTO로 변환하여 반환
        return new EnergyRateDto.ReadEnergyRateResponse(energyRateRepository.save(energyRate));
    }

    /**
     * 에너지 요금을 삭제합니다.
     *
     * @param energyRateId 삭제할 에너지 요금의 ID입니다.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long energyRateId) {
        // 에너지 요금이 존재하는지 확인
        EnergyRate energyRate = energyRateRepository.findById(energyRateId)
                .orElseThrow(() -> new EntityNotFoundException("No such energy rate."));
        // 에너지 요금 정보 삭제
        energyRateRepository.delete(energyRate);
    }
}