package atemos.everse.api.batch.config;

import atemos.everse.api.batch.tasklet.SaveMeteredUsageTasklet;
import atemos.everse.api.batch.tasklet.SavePaymentTasklet;
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
 * Payment 관련 Batch Job 설정 클래스.
 * - 3개의 스케줄러 작업을 Spring Batch로 변환하여 관리합니다.
 */
@Configuration
@RequiredArgsConstructor
public class PaymentJobConfig {
    /**
     * 서비스 사용 내역을 생성하는 Job을 정의하는 메서드입니다.
     *
     * @param jobRepository Job 실행을 관리하는 Spring Batch JobRepository
     * @param saveMeteredUsageStep 서비스 사용 내역 생성 Step
     * @return 정의된 Job 객체
     */
    @Bean
    public Job saveMeteredUsageJob(JobRepository jobRepository, Step saveMeteredUsageStep) {
        return new JobBuilder("saveMeteredUsageJob", jobRepository)
                .start(saveMeteredUsageStep)
                .build();
    }

    /**
     * 서비스 사용 내역을 생성하는 Step을 정의하는 메서드입니다.
     *
     * @param jobRepository Job 실행을 관리하는 Spring Batch JobRepository
     * @param transactionManager 트랜잭션 관리자를 담당하는 PlatformTransactionManager
     * @param saveMeteredUsageTasklet 서비스 사용 내역을 생성하는 Tasklet
     * @return 정의된 Step 객체
     */
    @Bean
    public Step saveMeteredUsageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, SaveMeteredUsageTasklet saveMeteredUsageTasklet) {
        return new StepBuilder("saveMeteredUsageStep", jobRepository)
                .tasklet(saveMeteredUsageTasklet, transactionManager)
                .build();
    }

    /**
     * 결제 정보를 생성하는 Job을 정의하는 메서드입니다.
     *
     * @param jobRepository Job 실행을 관리하는 Spring Batch JobRepository
     * @param savePaymentStep 결제 정보 생성 Step
     * @return 정의된 Job 객체
     */
    @Bean
    public Job savePaymentJob(JobRepository jobRepository, Step savePaymentStep) {
        return new JobBuilder("savePaymentJob", jobRepository)
                .start(savePaymentStep)
                .build();
    }

    /**
     * 결제 정보를 생성하는 Step을 정의하는 메서드입니다.
     *
     * @param jobRepository Job 실행을 관리하는 Spring Batch JobRepository
     * @param transactionManager 트랜잭션 관리자를 담당하는 PlatformTransactionManager
     * @param savePaymentTasklet 결제 정보를 생성하는 Tasklet
     * @return 정의된 Step 객체
     */
    @Bean
    public Step savePaymentStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, SavePaymentTasklet savePaymentTasklet) {
        return new StepBuilder("savePaymentStep", jobRepository)
                .tasklet(savePaymentTasklet, transactionManager)
                .build();
    }
}