package atemos.everse.api.batch.config;

import atemos.everse.api.batch.tasklet.AIForecastEnergyTasklet;
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
 * AIForecastEnergyJobConfig는 AI 예측 에너지 작업의 Job 및 Step 구성을 담당하는 설정 클래스입니다.
 * 이 클래스는 Spring Batch 작업 구성을 위한 Job과 Step을 정의합니다.
 */
@Configuration
@RequiredArgsConstructor
public class AIForecastEnergyJobConfig {
    /**
     * AI 예측 에너지 작업을 정의하는 Bean입니다.
     * 이 Job은 aiForecastEnergyStep을 시작으로 실행됩니다.
     *
     * @param jobRepository Spring Batch에서 Job 실행을 관리하는 JobRepository
     * @param aiForecastEnergyStep AI 예측 에너지 Step
     * @return 정의된 Job 객체
     */
    @Bean
    public Job aiForecastEnergyJob(JobRepository jobRepository, Step aiForecastEnergyStep) {
        return new JobBuilder("aiForecastEnergyJob", jobRepository)
                .start(aiForecastEnergyStep)
                .build();
    }

    /**
     * AI 예측 에너지 작업의 Step을 정의하는 Bean입니다.
     * 이 Step은 AIForecastEnergyTasklet을 실행하며, 트랜잭션 관리자를 사용하여 트랜잭션 처리를 보장합니다.
     *
     * @param jobRepository Spring Batch에서 Step 실행을 관리하는 JobRepository
     * @param transactionManager 트랜잭션을 관리하는 PlatformTransactionManager
     * @param aiForecastEnergyTasklet AI 예측 에너지 Tasklet
     * @return 정의된 Step 객체
     */
    @Bean
    public Step aiForecastEnergyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AIForecastEnergyTasklet aiForecastEnergyTasklet) {
        return new StepBuilder("aiForecastEnergyStep", jobRepository)
                .tasklet(aiForecastEnergyTasklet, transactionManager)
                .build();
    }
}