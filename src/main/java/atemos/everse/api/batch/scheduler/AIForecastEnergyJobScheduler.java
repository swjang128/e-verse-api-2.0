package atemos.everse.api.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIForecastEnergyJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job aiForecastEnergyJob;

    //@Scheduled(cron = "30 * * * * *")  // 테스트용
    @Scheduled(cron = "30 30 0 * * *")  // 매일 0시 30분 30초에 실행
    public void runJob() throws Exception {
        log.info("**** [START] Generating AI forecast data for last month, this month, and next month.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(aiForecastEnergyJob, jobParameters);
        log.info("**** [END] Generating AI forecast data for last month, this month, and next month.");
    }
}