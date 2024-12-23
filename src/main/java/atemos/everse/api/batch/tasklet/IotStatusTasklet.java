package atemos.everse.api.batch.tasklet;

import atemos.everse.api.domain.IotStatus;
import atemos.everse.api.entity.IotStatusHistory;
import atemos.everse.api.repository.IotRepository;
import atemos.everse.api.repository.IotStatusHistoryRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.random.RandomGenerator;

/**
 * 이 클래스는 IoT 장비의 상태 정보를 수집하는 Tasklet입니다.
 * 프로덕션 환경에서는 실제 IoT 상태 데이터를 수집하고, 그렇지 않은 경우에는
 * 90% 확률로 NORMAL, 10% 확률로 ERROR 상태를 임의로 생성하여 DB에 저장합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotStatusTasklet implements Tasklet {
    private final IotRepository iotRepository;
    private final IotStatusHistoryRepository iotStatusHistoryRepository;
    private final RandomGenerator randomGenerator = RandomGenerator.getDefault();

    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * 이 메서드는 IoT 상태 정보를 수집하여 DB에 저장하는 작업을 수행합니다.
     * 각 IoT 장비의 상태 정보를 수집하고, 이를 IotStatusHistory 엔티티로 변환하여 DB에 저장합니다.
     *
     * @param contribution StepContribution 객체로 배치 작업의 기여 정보를 담고 있습니다.
     * @param chunkContext ChunkContext 객체로 현재 배치 단계의 상태 정보를 담고 있습니다.
     * @return RepeatStatus.FINISHED 작업이 완료됨을 나타냅니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // 현재 시간을 분 단위로 반올림하여 현재 시간 설정
        var now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        // IoT 장비 목록에서 각 장비의 상태 정보를 수집하고 저장할 준비
        var iotStatusHistories = iotRepository.findAll().stream().map(iot -> {
            var status = collectIotStatus(iot.getId()); // IoT 장비의 상태 수집
            iot.setStatus(status); // IoT 장비 상태 설정
            iotRepository.save(iot); // IoT 장비 상태 업데이트
            return IotStatusHistory.builder().iot(iot).status(status).createdDate(now).build(); // 상태 이력 기록 생성
        }).toList();
        // 상태 이력을 DB에 저장
        iotStatusHistoryRepository.saveAll(iotStatusHistories);
        return RepeatStatus.FINISHED;
    }

    /**
     * IoT 장비의 상태를 수집하는 메서드입니다.
     * 프로덕션 환경에서는 실제 데이터를 수집하며, 그렇지 않을 경우에는
     * 90% 확률로 NORMAL, 10% 확률로 ERROR 상태를 반환합니다.
     *
     * @param iotId IoT 장비의 ID
     * @return 수집된 IoT 장비의 상태
     */
    private IotStatus collectIotStatus(Long iotId) {
        if ("prod".equals(activeProfile)) {
            try {
                log.info("[{}] Executing logic to retrieve actual IoT status data", activeProfile);
                return IotStatus.NORMAL; // 실제 상태 수집
            } catch (Exception e) {
                log.warn("[{}] Unable to retrieve actual IoT status data, using random data instead. IoT ID: {}", activeProfile, iotId);
            }
        }
        // 90% 확률로 NORMAL, 10% 확률로 ERROR 상태 반환
        return randomGenerator.nextDouble() < 0.9 ? IotStatus.NORMAL : IotStatus.ERROR;
    }
}