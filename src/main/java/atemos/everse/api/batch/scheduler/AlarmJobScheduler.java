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
 * AlarmJobScheduler는 매 시간마다 알람을 생성하는 AlarmJob을 실행하는 스케줄러 클래스입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job alarmJob;

    /**
     * 매 시간마다 AlarmJob을 실행하는 메서드입니다.
     * cron 표현식을 사용하여 매 시간 정각 25초에 실행되도록 설정되었습니다.
     */
    //@Scheduled(cron = "25 * * * * *") // 테스트용
    @Scheduled(cron = "25 0 * * * *")
    public void runJob() throws Exception {
        log.info("**** [START] Generating anomaly detection alarms for energy usage by company.");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(alarmJob, jobParameters);
        log.info("**** [END] Generating anomaly detection alarms for energy usage by company.");
    }
}