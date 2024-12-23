package atemos.everse.api.service;

import atemos.everse.api.domain.CompanyType;
import atemos.everse.api.dto.EnergyDto;
import atemos.everse.api.entity.AIForecastEnergy;
import atemos.everse.api.entity.Company;
import atemos.everse.api.entity.Energy;
import atemos.everse.api.entity.EnergyRate;
import atemos.everse.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 업체의 에너지 사용량과 요금 등의 데이터를 조회하는 기능을 제공하는 서비스 구현 클래스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnergyServiceImpl implements EnergyService {
    private final CompanyRepository companyRepository;
    private final EnergyRepository energyRepository;
    private final EnergyRateRepository energyRateRepository;
    private final AIForecastEnergyRepository aiForecastEnergyRepository;
    private final IotRepository iotRepository;

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
    @Override
    public EnergyDto.SummaryResponse readEnergy(Long companyId, LocalDate startDate, LocalDate endDate) {
        // 종료일이 null이면 시작일로 설정
        endDate = (endDate == null) ? startDate : endDate;
        // 회사 정보와 타임존 조회
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company with ID " + companyId + " not found."));
        var zoneId = company.getCountry().getZoneId();
        // 에너지 요금 정보 조회
        var energyRate = energyRateRepository.findByCountry(company.getCountry())
                .orElseThrow(() -> new EntityNotFoundException("No energy rate found for country: " + company.getCountry().getName()));
        // IoT 장비 목록과 기간 내 에너지 사용량/예측 사용량 조회
        var iotList = iotRepository.findByCompanyId(companyId);
        var energyList = energyRepository.findByIotInAndReferenceTimeBetween(iotList,
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        var aiForecastList = aiForecastEnergyRepository.findByCompanyIdAndForecastTimeBetween(
                companyId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        // 데이터 그룹화 및 시간별 처리
        var hourlyData = processHourlyData(energyList, aiForecastList, zoneId, energyRate, company);
        var dailyData = processDailyData(hourlyData);
        var monthlyData = processMonthlyData(dailyData);
        // SummaryResponse 생성 및 반환
        return processSummaryData(hourlyData, monthlyData);
    }

    /**
     * 업체의 실시간 및 전월 에너지 사용량과 요금을 조회하는 메서드입니다.
     *
     * @param companyId 업체 ID
     * @return 실시간 및 전월 에너지 사용량과 요금을 담은 응답 객체
     */
    @Override
    public EnergyDto.RealTimeAndLastMonthResponse getRealTimeAndLastMonthEnergy(Long companyId) {
        // 회사 정보 조회 및 타임존 설정
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        var country = Optional.ofNullable(company.getCountry())
                .orElseThrow(() -> new EntityNotFoundException("Company does not have an associated country."));
        // 현재 UTC 시간과 현지 시간 계산
        var today = LocalDateTime.ofInstant(Instant.now(), country.getZoneId()).toLocalDate();
        var dayOfPreviousMonth = today.minusMonths(1);
        // 실시간 데이터와 전월의 같은 날짜 데이터를 조회 후 리턴
        return new EnergyDto.RealTimeAndLastMonthResponse(
                readEnergy(companyId, today, today),
                readEnergy(companyId, dayOfPreviousMonth, dayOfPreviousMonth));
    }

    /**
     * 이번 달 및 저번 달의 에너지 사용량과 요금을 조회하는 메서드입니다.
     *
     * @param companyId 업체 ID
     * @return 이번 달 및 저번 달의 에너지 사용량과 요금을 담은 응답 객체
     */
    @Override
    public EnergyDto.ThisAndLastMonthResponse getThisAndLastMonthEnergy(Long companyId) {
        // 회사 정보 조회 및 타임존 설정
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        var country = Optional.ofNullable(company.getCountry())
                .orElseThrow(() -> new EntityNotFoundException("Company does not have an associated country."));
        // 현재 현지 날짜 계산
        var today = LocalDateTime.ofInstant(Instant.now(), country.getZoneId()).toLocalDate();
        // 이번 달의 첫 번째 날과 마지막 날 계산
        var firstDayOfThisMonth = today.withDayOfMonth(1);
        var lastDayOfThisMonth = firstDayOfThisMonth.withDayOfMonth(firstDayOfThisMonth.lengthOfMonth());
        // 저번 달의 첫 번째 날과 마지막 날 계산
        var firstDayOfLastMonth = firstDayOfThisMonth.minusMonths(1);
        var lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        // 이번 달과 저번 달의 에너지 사용량 및 요금을 조회하여 리턴
        return new EnergyDto.ThisAndLastMonthResponse(
                readEnergy(companyId, firstDayOfThisMonth, lastDayOfThisMonth),
                readEnergy(companyId, firstDayOfLastMonth, lastDayOfLastMonth)
        );
    }

    /**
     * 주어진 에너지 사용량 및 AI 예측 데이터를 시간별로 그룹화하여 HourlyResponse 객체 리스트를 생성합니다.
     * 각 시간대별 실제 사용량, 예측 사용량, 요금 등을 계산하여 반환합니다.
     *
     * @param energyList       실제 에너지 사용량 리스트입니다. 각 IoT 장비에서 수집된 에너지 데이터를 포함합니다.
     * @param aiForecastList   AI가 예측한 에너지 사용량 리스트입니다. 회사별 AI 예측 데이터를 포함합니다.
     * @param zoneId           해당 회사의 시간대 정보입니다. 에너지 사용량이 기록된 시간을 올바르게 처리하기 위해 사용됩니다.
     * @param energyRate       회사가 속한 국가의 에너지 요금 정보를 나타냅니다. 요금을 계산할 때 사용됩니다.
     * @param company          에너지 사용량과 요금이 계산될 회사의 정보입니다.
     * @return                 각 시간대별 에너지 사용량과 요금을 포함한 HourlyResponse 리스트를 반환합니다.
     */
    private List<EnergyDto.HourlyResponse> processHourlyData(List<Energy> energyList,
                                                             List<AIForecastEnergy> aiForecastList,
                                                             ZoneId zoneId,
                                                             EnergyRate energyRate,
                                                             Company company
    ) {
        // 실제 에너지 사용량을 시간대별로 그룹화 (Records 사용)
        var energyMap = energyList.stream()
                .collect(Collectors.groupingBy(e -> e.getReferenceTime().atZone(zoneId).truncatedTo(ChronoUnit.HOURS).toInstant()));
        // AI 예측 에너지도 시간대별로 그룹화
        var aiForecastMap = aiForecastList.stream()
                .collect(Collectors.groupingBy(a -> a.getForecastTime().atZone(zoneId).truncatedTo(ChronoUnit.HOURS).toInstant()));
        return energyMap.entrySet().stream()
                .map(entry -> {
                    var hour = entry.getKey();
                    var hourlyEnergyList = entry.getValue();
                    var hourlyForecastList = aiForecastMap.getOrDefault(hour, List.of());
                    // 실제 사용량과 예측 사용량을 합산
                    var totalUsage = hourlyEnergyList.stream()
                            .map(Energy::getFacilityUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    var totalForecastUsage = hourlyForecastList.stream()
                            .map(AIForecastEnergy::getForecastUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 요금 계산
                    var hourOfDay = hour.atZone(zoneId).getHour();
                    var rate = getRate(company, energyRate, hourOfDay).setScale(4, RoundingMode.HALF_UP);
                    var totalBill = totalUsage.multiply(rate).setScale(4, RoundingMode.HALF_UP);
                    var totalForecastBill = totalForecastUsage.multiply(rate).setScale(4, RoundingMode.HALF_UP);
                    // 편차율과 예측 정확도 계산
                    var deviationRate = calculateDeviationRate(totalUsage, totalForecastUsage);
                    var forecastAccuracy = calculateForecastAccuracy(totalUsage, totalForecastUsage);
                    // HourlyResponse 생성
                    return new EnergyDto.HourlyResponse(
                            hour.atZone(zoneId).toLocalDateTime(),
                            totalUsage,
                            totalForecastUsage,
                            totalUsage.subtract(totalForecastUsage),
                            totalBill,
                            totalForecastBill,
                            totalBill.subtract(totalForecastBill),
                            deviationRate,
                            forecastAccuracy
                    );
                })
                .sorted(Comparator.comparing(EnergyDto.HourlyResponse::getReferenceTime))  // 시간 오름차순 정렬
                .toList();
    }

    /**
     * 시간별 데이터를 일별로 그룹화하여 DailyResponse 객체 리스트를 생성합니다.
     * 각 날짜별로 사용량, 예측 사용량, 요금 등을 합산하고, 편차율 및 예측 정확도를 계산하여 반환합니다.
     *
     * @param hourlyData 시간별 사용량 및 요금 데이터 리스트입니다. HourlyResponse 객체들이 포함됩니다.
     * @return 일별 사용량과 요금 데이터를 담은 DailyResponse 리스트를 반환합니다.
     */
    private List<EnergyDto.DailyResponse> processDailyData(List<EnergyDto.HourlyResponse> hourlyData) {
        return hourlyData.stream()
                .collect(Collectors.groupingBy(h -> h.getReferenceTime().toLocalDate()))  // 시간별 데이터를 일별로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    var date = entry.getKey();
                    var hourlyList = entry.getValue();
                    // 일별 사용량과 예측 사용량을 합산
                    var totalUsage = hourlyList.stream()
                            .map(EnergyDto.HourlyResponse::getUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    var totalForecastUsage = hourlyList.stream()
                            .map(EnergyDto.HourlyResponse::getForecastUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 일별 요금과 예측 요금을 합산
                    var totalBill = hourlyList.stream()
                            .map(EnergyDto.HourlyResponse::getBill)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    var totalForecastBill = hourlyList.stream()
                            .map(EnergyDto.HourlyResponse::getForecastBill)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 편차율 및 예측 정확도 계산
                    var deviationRate = calculateDeviationRate(totalUsage, totalForecastUsage);
                    var forecastAccuracy = calculateForecastAccuracy(totalUsage, totalForecastUsage);
                    // DailyResponse 생성
                    return new EnergyDto.DailyResponse(
                            date,
                            totalUsage,
                            totalForecastUsage,
                            totalUsage.subtract(totalForecastUsage),
                            totalBill,
                            totalForecastBill,
                            totalBill.subtract(totalForecastBill),
                            deviationRate,
                            forecastAccuracy,
                            hourlyList  // 시간별 데이터 리스트
                    );
                })
                .sorted(Comparator.comparing(EnergyDto.DailyResponse::getReferenceDate))  // 날짜별로 오름차순 정렬
                .toList();  // 리스트로 반환
    }

    /**
     * 일별 데이터를 월별로 그룹화하여 MonthlyResponse 객체 리스트를 생성합니다.
     * 각 월별로 사용량, 예측 사용량, 요금 등을 합산하고, 편차율 및 예측 정확도를 계산하여 반환합니다.
     *
     * @param dailyData 일별 사용량 및 요금 데이터를 담은 DailyResponse 리스트입니다.
     * @return 월별 사용량과 요금 데이터를 담은 MonthlyResponse 리스트를 반환합니다.
     */
    private List<EnergyDto.MonthlyResponse> processMonthlyData(List<EnergyDto.DailyResponse> dailyData) {
        return dailyData.stream()
                .collect(Collectors.groupingBy(d -> d.getReferenceDate().withDayOfMonth(1)))  // 일별 데이터를 월별로 그룹화
                .entrySet().stream()
                .map(entry -> {
                    var month = entry.getKey();
                    var dailyList = entry.getValue();
                    // 월별 사용량과 예측 사용량을 합산
                    var totalUsage = dailyList.stream()
                            .map(EnergyDto.DailyResponse::getDailyUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    var totalForecastUsage = dailyList.stream()
                            .map(EnergyDto.DailyResponse::getDailyForecastUsage)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 월별 요금과 예측 요금을 합산
                    var totalBill = dailyList.stream()
                            .map(EnergyDto.DailyResponse::getDailyBill)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    var totalForecastBill = dailyList.stream()
                            .map(EnergyDto.DailyResponse::getDailyForecastBill)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 편차율 및 예측 정확도 계산
                    var deviationRate = calculateDeviationRate(totalUsage, totalForecastUsage);
                    var forecastAccuracy = calculateForecastAccuracy(totalUsage, totalForecastUsage);
                    // MonthlyResponse 생성
                    return new EnergyDto.MonthlyResponse(
                            month,
                            totalUsage,
                            totalForecastUsage,
                            totalUsage.subtract(totalForecastUsage),
                            totalBill,
                            totalForecastBill,
                            totalBill.subtract(totalForecastBill),
                            deviationRate,
                            forecastAccuracy,
                            dailyList.stream().collect(Collectors.toMap(
                                    d -> d.getReferenceDate().toString(), // 날짜별 키로 맵핑
                                    d -> d,
                                    (oldValue, newValue) -> oldValue,
                                    LinkedHashMap::new
                            ))
                    );
                })
                .sorted(Comparator.comparing(EnergyDto.MonthlyResponse::getReferenceMonth))  // 월별로 오름차순 정렬
                .toList();  // 리스트로 반환
    }

    /**
     * 시간별, 일별, 월별 데이터를 모두 합산하여 SummaryResponse 객체를 생성합니다.
     * 전체 사용량, 예측 사용량, 요금 등을 합산하고, 편차율 및 예측 정확도를 계산하여 반환합니다.
     *
     * @param hourlyData 시간별 사용량 및 요금 데이터를 담은 HourlyResponse 리스트입니다.
     * @param monthlyData 월별 사용량 및 요금 데이터를 담은 MonthlyResponse 리스트입니다.
     * @return 전체 사용량과 요금 데이터를 담은 SummaryResponse 객체를 반환합니다.
     */
    private EnergyDto.SummaryResponse processSummaryData(List<EnergyDto.HourlyResponse> hourlyData,
                                                         List<EnergyDto.MonthlyResponse> monthlyData) {
        // 전체 사용량 및 요금 계산
        var totalUsage = hourlyData.stream()
                .map(EnergyDto.HourlyResponse::getUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalForecastUsage = hourlyData.stream()
                .map(EnergyDto.HourlyResponse::getForecastUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalBill = hourlyData.stream()
                .map(EnergyDto.HourlyResponse::getBill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalForecastBill = hourlyData.stream()
                .map(EnergyDto.HourlyResponse::getForecastBill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var deviationRate = calculateDeviationRate(totalUsage, totalForecastUsage);
        var forecastAccuracy = calculateForecastAccuracy(totalUsage, totalForecastUsage);
        // SummaryResponse 생성
        return EnergyDto.SummaryResponse.builder()
                .monthlyResponse(monthlyData.stream()
                        .collect(Collectors.toMap(
                                m -> m.getReferenceMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")), // 월별 키로 맵핑
                                Function.identity(), // m -> m 대체 가능
                                (existing, replacement) -> existing, // 중복 처리
                                LinkedHashMap::new // 삽입 순서 보장
                        ))
                )
                .summaryUsage(totalUsage)
                .summaryForecastUsage(totalForecastUsage)
                .summaryUsageForecastDifference(totalUsage.subtract(totalForecastUsage))
                .summaryBill(totalBill)
                .summaryForecastBill(totalForecastBill)
                .summaryBillForecastDifference(totalBill.subtract(totalForecastBill))
                .summaryDeviationRate(deviationRate)
                .summaryForecastAccuracy(forecastAccuracy)
                .build();
    }

    /**
     * 특정 시간대의 요금을 반환합니다.
     *
     * @param company 업체 엔티티입니다.
     * @param energyRate 해당 국가의 에너지 요금 정보입니다.
     * @param hour 조회할 시간대 (0~23)
     * @return 해당 시간대에 적용되는 요금을 반환합니다.
     */
    @Override
    public BigDecimal getRate(Company company, EnergyRate energyRate, int hour) {
        // 회사 타입에 따른 기본 요금을 설정
        BigDecimal baseRate = company.getType() == CompanyType.FEMS ? energyRate.getIndustrialRate() : energyRate.getCommercialRate();
        // 시간대에 따른 요금 증감율을 적용
        if (energyRate.getPeakHours().contains(hour)) {
            return baseRate.multiply(energyRate.getPeakMultiplier()).setScale(4, RoundingMode.HALF_UP);
        } else if (energyRate.getMidPeakHours().contains(hour)) {
            return baseRate.multiply(energyRate.getMidPeakMultiplier()).setScale(4, RoundingMode.HALF_UP);
        } else {
            return baseRate.multiply(energyRate.getOffPeakMultiplier()).setScale(4, RoundingMode.HALF_UP);
        }
    }

    /**
     * 편차율을 계산합니다.
     * @param usage 실제 사용량 값입니다.
     * @param forecastUsage 예측 사용량 값입니다.
     * @return 편차율을 반환합니다.
     */
    @Override
    public BigDecimal calculateDeviationRate(BigDecimal usage, BigDecimal forecastUsage) {
        var maxValue = usage.max(forecastUsage);
        if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usage.subtract(forecastUsage)
                .abs()
                .divide(maxValue, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 예측 정확도를 계산합니다.
     * @param usage 실제 사용량 값입니다.
     * @param forecastUsage 예측 사용량 값입니다.
     * @return 예측 정확도를 반환합니다.
     */
    @Override
    public BigDecimal calculateForecastAccuracy(BigDecimal usage, BigDecimal forecastUsage) {
        var maxValue = usage.max(forecastUsage);
        if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ONE
                .subtract(usage.subtract(forecastUsage)
                        .abs()
                        .divide(maxValue, 10, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }
}