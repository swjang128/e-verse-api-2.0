package atemos.everse.api.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * IoT Scheduler Batch Job을 주기적으로 실행하기 위한 스케줄러 클래스.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job energyUsageJob;
    private final Job iotStatusJob;

    /**
     * 매 시 정각에 에너지 사용량 수집 배치 작업을 실행합니다.
     */
    //@Scheduled(cron = "0 * * * * *")  // 테스트용
    @Scheduled(cron = "0 0 * * * *")
    public void runEnergyUsageJob() throws Exception {
        log.info("**** [START] Collecting energy usage for each IoT device.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(energyUsageJob, jobParameters);
        log.info("**** [END] Collecting energy usage for each IoT device.");
    }

    /**
     * 매 시 0분 20초에 IoT 상태 수집 배치 작업을 실행합니다.
     */
    //@Scheduled(cron = "20 * * * * *") // 테스트용
    @Scheduled(cron = "20 0 * * * *")
    public void runIotStatusJob() throws Exception {
        log.info("**** [START] Checking and updating IoT status.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(iotStatusJob, jobParameters);
        log.info("**** [END] Checking and updating IoT status.");
    }
}
