package atemos.everse.api.batch.tasklet;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.entity.Energy;
import atemos.everse.api.entity.Iot;
import atemos.everse.api.repository.EnergyRepository;
import atemos.everse.api.repository.IotRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.random.RandomGenerator;

/**
 * 이 클래스는 IoT 장비의 에너지 사용량을 수집하는 Tasklet입니다.
 * 각 IoT 장비의 에너지 사용량을 수집하여 DB에 저장하는 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnergyUsageTasklet implements Tasklet {
    private final IotRepository iotRepository;
    private final EnergyRepository energyRepository;
    private final RandomGenerator randomGenerator = RandomGenerator.getDefault();

    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * 이 메서드는 IoT 장비의 에너지 사용량을 수집하여 DB에 저장하는 작업을 수행합니다.
     * 각 IoT 장비의 에너지 사용량을 수집한 후, Energy 엔티티로 변환하여 저장합니다.
     *
     * @param contribution StepContribution 객체로 배치 작업의 기여 정보를 담고 있습니다.
     * @param chunkContext ChunkContext 객체로 현재 배치 단계의 상태 정보를 담고 있습니다.
     * @return RepeatStatus.FINISHED 작업이 완료됨을 나타냅니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        var now = Instant.now().truncatedTo(ChronoUnit.MINUTES); // 현재 시간을 분 단위로 반올림
        // 각 IoT 장비의 에너지 사용량 수집
        var energyList = iotRepository.findAll().stream().map(iot -> {
            ZoneId zoneId = iot.getCompany().getCountry().getZoneId(); // 회사의 타임존을 가져옴
            LocalDateTime referenceTime = LocalDateTime.ofInstant(now, zoneId).minusHours(1); // IoT 장비의 타임존에 맞춰 referenceTime 설정
            return Energy.builder().iot(iot).facilityUsage(collectEnergyUsage(iot)).referenceTime(referenceTime).build(); // 에너지 데이터 생성
        }).toList();
        // 수집한 에너지를 DB에 저장
        energyRepository.saveAll(energyList);
        return RepeatStatus.FINISHED;
    }

    /**
     * IoT 장비의 상태가 ERROR인 경우 에너지 사용량은 0으로 설정하며,
     * 그렇지 않은 경우에는 랜덤 값을 생성하여 에너지 사용량을 반환합니다.
     *
     * @param iot IoT 장비 객체
     * @return 수집한 에너지 사용량
     */
    private BigDecimal collectEnergyUsage(Iot iot) {
        // 장비 상태가 ERROR인 경우 에너지 사용량 0 반환
        if (iot.getStatus() == IotStatus.ERROR) {
            return BigDecimal.ZERO;
        }
        // 활성화된 Spring 프로파일이 "prod"인 경우 실제 데이터 수집 로직 실행
        if ("prod".equals(activeProfile)) {
            try {
                log.info("[{}] Executing logic to retrieve actual energy usage", activeProfile);
                return BigDecimal.ZERO; // 실제 데이터 수집 로직이 구현되지 않았음
            } catch (Exception e) {
                log.warn("[{}] Unable to retrieve actual energy usage, using random data instead. IoT ID: {}", activeProfile, iot.getId());
            }
        }
        // IoT 장비가 속한 업체가 보유한 Normal 상태인 IoT 장비 개수를 구하기
        long normalIotCount = iotRepository.countByCompanyAndStatus(iot.getCompany(), IotStatus.NORMAL);
        // 935 / IoT 장비가 속한 업체가 보유한 Normal 상태인 IoT 장비 개수(정상 장비 수가 0인 경우를 대비해 NORMAL 장비 수가 0개면 1개로 처리)
        BigDecimal energyUsage = BigDecimal.valueOf(935).divide(BigDecimal.valueOf(normalIotCount > 0 ? normalIotCount : 1), 4, RoundingMode.HALF_UP);
        // 랜덤 에너지 사용량 생성(935 / IoT 장비가 속한 업체가 보유한 Normal 상태인 IoT 장비 개수로 생성)(소수점 넷째 자리에서 반올림)
        BigDecimal fluctuation = energyUsage.multiply(BigDecimal.valueOf(0.2)); // ±20% 변동량
        BigDecimal randomFluctuation = fluctuation.multiply(BigDecimal.valueOf(randomGenerator.nextDouble() * 2 - 1)); // -20% ~ +20% 랜덤 값 생성
        return energyUsage.add(randomFluctuation).setScale(4, RoundingMode.HALF_UP);
    }
}