package atemos.everse.api.batch.config;

import atemos.everse.api.batch.tasklet.EnergyUsageTasklet;
import atemos.everse.api.batch.tasklet.IotStatusTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * IotBatchJobConfig는 IoT 장비의 상태 정보와 에너지 사용량을 처리하는
 * Spring Batch Job 설정을 정의한 클래스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class IotJobConfig {
    /**
     * IoT 에너지 사용량을 처리하는 Job을 정의하는 메서드입니다.
     * 이 Job은 energyUsageStep을 시작으로 실행됩니다.
     *
     * @param jobRepository Spring Batch에서 Job 실행을 관리하는 JobRepository
     * @param energyUsageStep IoT 에너지 사용량 Step
     * @return 정의된 Job 객체
     */
    @Bean
    public Job energyUsageJob(JobRepository jobRepository, Step energyUsageStep) {
        return new JobBuilder("energyUsageJob", jobRepository)
                .start(energyUsageStep)
                .build();
    }

    /**
     * IoT 에너지 사용량을 처리하는 Step을 정의하는 메서드입니다.
     * 이 Step은 EnergyUsageTasklet을 실행하며, 트랜잭션 관리자를 통해 트랜잭션 처리가 보장됩니다.
     *
     * @param jobRepository Spring Batch에서 Step 실행을 관리하는 JobRepository
     * @param transactionManager 트랜잭션 관리자를 담당하는 PlatformTransactionManager
     * @param energyUsageTasklet IoT 에너지 사용량 Tasklet
     * @return 정의된 Step 객체
     */
    @Bean
    public Step energyUsageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, EnergyUsageTasklet energyUsageTasklet) {
        return new StepBuilder("energyUsageStep", jobRepository)
                .tasklet(energyUsageTasklet, transactionManager)
                .build();
    }

    /**
     * IoT 장비 상태 정보를 처리하는 Job을 정의하는 메서드입니다.
     * 이 Job은 iotStatusStep을 시작으로 실행됩니다.
     *
     * @param jobRepository Spring Batch에서 Job 실행을 관리하는 JobRepository
     * @param iotStatusStep IoT 장비 상태 Step
     * @return 정의된 Job 객체
     */
    @Bean
    public Job iotStatusJob(JobRepository jobRepository, Step iotStatusStep) {
        return new JobBuilder("iotStatusJob", jobRepository)
                .start(iotStatusStep)
                .build();
    }

    /**
     * IoT 장비 상태 정보를 처리하는 Step을 정의하는 메서드입니다.
     * 이 Step은 IotStatusTasklet을 실행하며, 트랜잭션 관리자를 통해 트랜잭션 처리가 보장됩니다.
     *
     * @param jobRepository Spring Batch에서 Step 실행을 관리하는 JobRepository
     * @param transactionManager 트랜잭션 관리자를 담당하는 PlatformTransactionManager
     * @param iotStatusTasklet IoT 장비 상태 Tasklet
     * @return 정의된 Step 객체
     */
    @Bean
    public Step iotStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, IotStatusTasklet iotStatusTasklet) {
        return new StepBuilder("iotStatusStep", jobRepository)
                .tasklet(iotStatusTasklet, transactionManager)
                .build();
    }
}