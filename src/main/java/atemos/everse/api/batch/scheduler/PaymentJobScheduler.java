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
 * PaymentJobScheduler는 매일 정해진 시간에 Payment 관련 배치 작업을 실행합니다.
 * 각 Job은 별도의 스케줄에 따라 실행되며, JobParameters는 실행 시각을 포함하여 전달됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job savePaymentJob;
    private final Job saveMeteredUsageJob;

    /**
     * 매 분 30초에 MeteredUsageJob을 실행합니다.
     */
    @Scheduled(cron = "30 * * * * *")
    public void runSaveMeteredUsageJob() throws Exception {
        log.info("**** [START] Storing metered usage data for services used by companies from the service start date until yesterday in MeteredUsage entity.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(saveMeteredUsageJob, jobParameters);
        log.info("**** [END] Storing metered usage data for services used by companies from the service start date until yesterday in MeteredUsage entity.");
    }

    /**
     * 매 분 45초에 PaymentJob을 실행합니다.
     */
    @Scheduled(cron = "45 * * * * *")
    public void runSavePaymentJob() throws Exception {
        log.info("**** [START] Saving or updating Payment entity with service usage and subscription status for a specific day.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(savePaymentJob, jobParameters);
        log.info("**** [END] Saving or updating Payment entity with service usage and subscription status for a specific day.");
    }
}