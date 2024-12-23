package atemos.everse.api.service;

import atemos.everse.api.dto.EnergyDto;
import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.EnergyRate;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * EnergyService는 업체의 에너지 사용량과 요금 등의 데이터를 조회하고 엑셀로 제공하는 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface EnergyService {
    /**
     * 기간 내 업체가 사용한 에너지 사용량과 요금을 조회합니다.
     * 조회 결과는 시간별(HourlyResponse.referenceTime), 일별(DailyResponse.referenceDate), 월별(MonthlyResonse.referenceMonth), 전체(SummaryResponse)로 집계하며,
     * 필요시 사용량과 요금 데이터를 모두 제공하거나 선택적으로 제공할 수 있습니다.
     *
     * @param companyId 업체 ID입니다. 에너지 사용량과 요금을 조회할 업체를 식별하는 ID입니다.
     * @param startDate 기간 조회 시작일입니다.
     * @param endDate 기간 조회 종료일입니다.
     * @return 기간 내 업체가 사용한 에너지 사용량과 요금을 담은 응답 객체입니다.
     */
    EnergyDto.SummaryResponse readEnergy(Long companyId, LocalDate startDate, LocalDate endDate);
    /**
     * 업체의 실시간 및 전월 에너지 사용량과 요금을 조회하는 메서드입니다.
     *
     * @param companyId 업체 ID
     * @return 실시간 및 전월 에너지 사용량과 요금을 담은 응답 객체
     */
    EnergyDto.RealTimeAndLastMonthResponse getRealTimeAndLastMonthEnergy(Long companyId);
    /**
     * 이번 달 및 저번 달의 에너지 사용량과 요금을 조회하는 메서드입니다.
     *
     * @param companyId 업체 ID
     * @return 이번 달 및 저번 달의 에너지 사용량과 요금을 담은 응답 객체
     */
    EnergyDto.ThisAndLastMonthResponse getThisAndLastMonthEnergy(Long companyId);
    /**
     * 해당 업체의 1kw당 요금을 조회합니다.
     *
     * @param company 업체 정보입니다. 1kw당 요금을 조회할 업체를 식별하는 ID입니다.
     * @param energyRate 에너지 요금 정보
     * @param hour 요금 계산할 시간대
     * @return 해당 업체의 1kw당 요금입니다.
     */
    BigDecimal getRate(Company company, EnergyRate energyRate, int hour);
    /**
     * 오차율(Deviation EnergyRate)을 계산하는 메서드입니다.
     *
     * @param usageValue 실제 사용량
     * @param forecastUsage 예측 사용량
     * @return 오차율(Deviation EnergyRate)
     */
    BigDecimal calculateDeviationRate(BigDecimal usageValue, BigDecimal forecastUsage);
    /**
     * 예측 정확도(Forecast Accuracy)를 계산하는 메서드입니다.
     *
     * @param usageValue 실제 사용량
     * @param forecastUsage 예측 사용량
     * @return 예측 정확도(Forecast Accuracy)
     */
    BigDecimal calculateForecastAccuracy(BigDecimal usageValue, BigDecimal forecastUsage);
}
