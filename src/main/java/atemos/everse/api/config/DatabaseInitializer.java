package atemos.everse.api.config;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.domain.MemberStatus;
import atemos.everse.api.domain.SampleData;
import atemos.everse.api.entity.*;
import atemos.everse.api.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * 데이터베이스 초기 설정을 담당하는 클래스입니다.
 * 데이터베이스가 비어 있는 경우 관리자 계정과 샘플 데이터를 생성합니다.
 */
@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class DatabaseInitializer {
    private final MenuRepository menuRepository;
    private final CountryRepository countryRepository;
    private final EnergyRateRepository energyRateRepository;
    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final AnomalyRepository anomalyRepository;
    private final EnergyRepository energyRepository;
    private final IotRepository iotRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptUtil encryptUtil;
    private final RandomGenerator randomGenerator = RandomGenerator.getDefault();

    /**
     * 데이터베이스 초기화를 담당하는 메서드입니다.
     * 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        createSampleParentMenu();
        createSampleChildrenMenu();
        createSampleCountry();
        createSampleEnergyRate();
        createSampleCompany();
        createSampleMember();
        createSampleAnomaly();
        createSampleIot();
        createHistoricalEnergyData();
    }

    /**
     * 상위 메뉴 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     * 열거형 데이터를 동적으로 처리하여, 열거형 변경 시 자동 반영되도록 수정되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleParentMenu() {
        if (menuRepository.count() == 0) {
            log.info("**** No Parent Menu data found. Creating sample Parent Menu entries.");
            var parentMenuList = Arrays.stream(SampleData.ParentMenu.values())
                    .map(parentMenu -> Menu.builder()
                            .name(parentMenu.getName())
                            .url(parentMenu.getPath())
                            .description(parentMenu.getDescription())
                            .accessibleRoles(parentMenu.getAccessibleRoles())
                            .depth(0)
                            .available(true)
                            .requiredSubscription(parentMenu.getRequiredSubscription())
                            .build())
                    .toList();
            menuRepository.saveAll(parentMenuList);
        }
    }

    /**
     * 하위 메뉴 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     * 열거형을 통해 상위 메뉴를 동적으로 매핑하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleChildrenMenu() {
        if (!menuRepository.existsByDepth(1)) {
            log.info("**** No Children Menu data with Depth 1 found. Creating sample Children Menu entries.");
            // 상위 메뉴를 매핑하여 Map으로 변환
            var parentMenuMap = menuRepository.findAll().stream()
                    .collect(Collectors.toMap(Menu::getName, menu -> menu));
            var childrenMenuList = Arrays.stream(SampleData.ChildrenMenu.values())
                    .map(childrenMenu -> {
                        var parentMenu = parentMenuMap.get(childrenMenu.getParentMenu().getName());
                        if (parentMenu == null) {
                            throw new EntityNotFoundException("Parent menu not found: " + childrenMenu.getParentMenu().getName());
                        }
                        return Menu.builder()
                                .name(childrenMenu.getName())
                                .url(childrenMenu.getPath())
                                .description(childrenMenu.getDescription())
                                .accessibleRoles(childrenMenu.getAccessibleRoles())
                                .depth(parentMenu.getDepth() + 1)
                                .parent(parentMenu)
                                .available(true)
                                .requiredSubscription(childrenMenu.getRequiredSubscription())
                                .build();
                    })
                    .toList();
            menuRepository.saveAll(childrenMenuList);
        }
    }

    /**
     * 국가 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     * 열거형을 순회하며 동적으로 국가 데이터를 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleCountry() {
        if (countryRepository.count() == 0) {
            log.info("**** No Country data found. Creating sample Country entries.");
            var countries = Arrays.stream(SampleData.Country.values())
                    .map(countryEnum -> Country.builder()
                            .name(countryEnum.getName())
                            .languageCode(countryEnum.getLanguageCode())
                            .timeZone(countryEnum.getTimeZone())
                            .build())
                    .toList();
            countryRepository.saveAll(countries);
        }
    }

    /**
     * 샘플 에너지 요금 데이터를 생성하여 데이터베이스에 저장합니다.
     * 국가별로 동적으로 에너지 요금 데이터를 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleEnergyRate() {
        if (energyRateRepository.count() == 0) {
            log.info("**** No EnergyRate data found. Creating sample EnergyRate entries.");
            var energyRates = Arrays.stream(SampleData.EnergyRate.values())
                    .map(sampleRate -> {
                        var country = countryRepository.findByName(sampleRate.name())
                                .orElseThrow(() -> new EntityNotFoundException("Country not found: " + sampleRate.name()));
                        return createEnergyRate(country, sampleRate);
                    })
                    .toList();
            energyRateRepository.saveAll(energyRates);
        }
    }


    /**
     * 샘플 회사 데이터를 생성하여 데이터베이스에 저장합니다.
     * 국가별로 동적으로 회사 데이터를 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleCompany() {
        if (companyRepository.count() == 0) {
            log.info("**** No Company data found. Creating sample Company entries.");
            var companies = Arrays.stream(SampleData.Company.values())
                    .map(companyEnum -> {
                        var country = countryRepository.findByName(companyEnum.getCountryName())
                                .orElseThrow(() -> new EntityNotFoundException("Country not found: " + companyEnum.getCountryName()));
                        return Company.builder()
                                .name(companyEnum.getName())
                                .email(companyEnum.getEmail())
                                .tel(companyEnum.getTel())
                                .fax(companyEnum.getFax())
                                .address(companyEnum.getAddress())
                                .country(country)
                                .type(companyEnum.getType())
                                .build();
                    })
                    .toList();
            companyRepository.saveAll(companies);
        }
    }

    /**
     * 샘플 사용자 데이터를 생성하여 데이터베이스에 저장합니다.
     * 동적으로 사용자 데이터를 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleMember() {
        if (memberRepository.count() == 0) {
            log.info("**** No Member data found. Creating sample Member entries.");
            var members = Arrays.stream(SampleData.Member.values())
                    .map(memberEnum -> {
                        var company = companyRepository.findByName(memberEnum.getName())
                                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + memberEnum.getName()));
                        return Member.builder()
                                .name(encryptUtil.encrypt(memberEnum.getName()))
                                .email(encryptUtil.encrypt(memberEnum.getEmail()))
                                .phone(encryptUtil.encrypt(memberEnum.getPhone()))
                                .company(company)
                                .password(passwordEncoder.encode(memberEnum.getPassword()))
                                .role(memberEnum.getRole())
                                .status(MemberStatus.ACTIVE)
                                .build();
                    })
                    .toList();
            memberRepository.saveAll(members);
        }
    }

    /**
     * 샘플 이상 탐지 데이터를 생성하여 데이터베이스에 저장합니다.
     * 동적으로 각 회사에 대한 이상 탐지 데이터를 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleAnomaly() {
        if (anomalyRepository.count() == 0) {
            log.info("**** No Anomaly data found. Creating sample Anomaly entries.");
            var anomalies = Arrays.stream(SampleData.Anomaly.values())
                    .map(anomalyEnum -> {
                        var company = companyRepository.findByName(anomalyEnum.getCompanyName())
                                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + anomalyEnum.getCompanyName()));
                        return Anomaly.builder()
                                .company(company)
                                .lowestHourlyEnergyUsage(anomalyEnum.getLowestHourlyEnergyUsage())
                                .highestHourlyEnergyUsage(anomalyEnum.getHighestHourlyEnergyUsage())
                                .available(true)
                                .build();
                    })
                    .toList();
            anomalyRepository.saveAll(anomalies);
        }
    }

    /**
     * 샘플 IoT 데이터를 생성하여 데이터베이스에 저장합니다.
     * 각 회사별로 IoT 장치를 동적으로 생성하도록 개선되었습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleIot() {
        if (iotRepository.count() == 0) {
            log.info("**** No IoT data found. Creating sample IoT entries.");
            var iots = Arrays.stream(SampleData.Iot.values())
                    .map(iotEnum -> {
                        var company = companyRepository.findByName(iotEnum.getCompanyName())
                                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + iotEnum.getCompanyName()));
                        return Iot.builder()
                                .company(company)
                                .serialNumber(iotEnum.getSerialNumber())
                                .type(iotEnum.getType())
                                .location(iotEnum.getLocation())
                                .price(iotEnum.getPrice())
                                .status(IotStatus.NORMAL)
                                .build();
                    })
                    .toList();
            iotRepository.saveAll(iots);
        }
    }

    /**
     * 과거 에너지 데이터를 생성하는 메서드입니다.
     * 현재 시간으로부터 3개월 전까지의 에너지 데이터를 시간 단위로 생성합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createHistoricalEnergyData() {
        if (energyRepository.count() == 0) {
            var endTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);  // LocalDateTime 사용
            var startTime = endTime.minusMonths(3);  // 3개월 전으로 설정
            log.info("**** Generating historical energy usage data from {} to {}", startTime, endTime);
            // IoT 장치와 관련된 Company와 Country를 함께 로드
            var allIots = iotRepository.findAllWithCompanyAndCountry();
            log.info("**** Processing {} IoT devices.", allIots.size());
            List<Energy> batch = new ArrayList<>();
            while (startTime.isBefore(endTime)) {
                var currentBatchTime = startTime;
                allIots.forEach(iot -> {
                    Company company = iot.getCompany();
                    ZoneId companyZoneId = company.getCountry().getZoneId();
                    // 로컬 시간을 업체의 타임존에 맞춰 변환
                    LocalDateTime localReferenceTime = currentBatchTime.atZone(ZoneId.systemDefault())
                            .withZoneSameInstant(companyZoneId)
                            .toLocalDateTime();
                    var energyData = Energy.builder()
                            .iot(iot)
                            .facilityUsage(collectEnergyUsageForHistoricalData(iot))
                            .referenceTime(localReferenceTime)  // 타임존 변환된 시간
                            .build();
                    batch.add(energyData);
                });
                energyRepository.saveAll(batch);
                batch.clear();
                startTime = startTime.plus(1, ChronoUnit.HOURS);
            }
        } else {
            log.info("**** Energy table is not empty. Skipping historical data creation.");
        }
    }

    /**
     * 에너지 요금 데이터를 생성하는 보조 메서드입니다.
     */
    private EnergyRate createEnergyRate(Country country, SampleData.EnergyRate sampleRate) {
        return EnergyRate.builder()
                .country(country)
                .industrialRate(sampleRate.getIndustrialRate())
                .commercialRate(sampleRate.getCommercialRate())
                .peakMultiplier(sampleRate.getPeakMultiplier())
                .peakHours(sampleRate.getPeakHours())
                .midPeakMultiplier(sampleRate.getMidPeakMultiplier())
                .midPeakHours(sampleRate.getMidPeakHours())
                .offPeakMultiplier(sampleRate.getOffPeakMultiplier())
                .offPeakHours(sampleRate.getOffPeakHours())
                .build();
    }

    /**
     * IoT 장치가 정상일 경우 랜덤한 에너지 사용량을 반환하고,
     * 에러 상태일 경우 0을 반환하는 메서드입니다.
     */
    private BigDecimal collectEnergyUsageForHistoricalData(Iot iot) {
        if (iot.getStatus() == IotStatus.ERROR) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(randomGenerator.nextDouble() * 600).setScale(4, RoundingMode.HALF_UP);
    }
}