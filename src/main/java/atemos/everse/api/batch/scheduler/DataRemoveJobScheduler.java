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
 * DataRemoveJobScheduler는 DataRemoveJob을 스케줄링하여 주기적으로 실행하는 클래스입니다.
 * - 매일 자정에 배치 작업을 실행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataRemoveJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job dataRemoveJob;

    /**
     * 매일 자정에 DataRemoveJob을 실행합니다.
     * - JobParameters는 실행 시각을 포함하여 전달됩니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runJob() throws Exception {
        log.info("**** [START] Removing old data.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dataRemoveJob, jobParameters);
        log.info("**** [END] Removing old data.");
    }
}