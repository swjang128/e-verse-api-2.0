package atemos.everse.api.batch.tasklet;

import atemos.everse.api.domain.PaymentMethod;
import atemos.everse.api.domain.PaymentStatus;
import atemos.everse.api.entity.Payment;
import atemos.everse.api.entity.Subscription;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.MeteredUsageRepository;
import atemos.everse.api.repository.PaymentRepository;
import atemos.everse.api.repository.SubscriptionRepository;
import atemos.everse.api.service.PaymentService;
import atemos.everse.api.service.StorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * 결제 데이터를 생성하거나 업데이트하는 Spring Batch Tasklet 클래스입니다.
 * 각 업체의 현지 시간대를 기준으로 현재부터 이전 모든 날짜의 사용량 데이터를 수집하여
 * 결제 정보를 생성하거나 업데이트합니다. 만약 기존 결제 정보가 없으면 새로운 결제 정보를 생성하고,
 * 이미 존재하면 해당 정보를 업데이트합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SavePaymentTasklet implements Tasklet {
    private final CompanyRepository companyRepository;
    private final MeteredUsageRepository meteredUsageRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StorageService storageService;
    private final PaymentService paymentService;

    /**
     * 각 업체의 결제 데이터를 생성하거나 업데이트합니다.
     * @param contribution 현재 스텝의 기여도 정보를 담고 있는 객체입니다.
     * @param chunkContext 청크 처리 시의 컨텍스트 정보를 담고 있는 객체입니다.
     * @return 작업 완료 상태를 반환합니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        companyRepository.findAll().forEach(company -> {
            // 해당 업체의 가장 오래된 MeteredUsage 데이터를 조회합니다.
            meteredUsageRepository.findFirstByCompanyOrderByUsageDateAsc(company).ifPresentOrElse(
                    earliestMeteredUsage -> {
                        // 가장 오래된 사용일(usageDate)부터 현재까지의 사용량 데이터를 조회합니다.
                        var meteredUsages = meteredUsageRepository.findByCompanyAndUsageDateBetween(
                                company,
                                earliestMeteredUsage.getUsageDate(),
                                LocalDate.now(company.getCountry().getZoneId())
                        );
                        // 사용량 데이터에 대해 반복문을 실행합니다.
                        meteredUsages.forEach(meteredUsage -> {
                            var usageDate = meteredUsage.getUsageDate();
                            // 해당 사용일에 맞는 Payment 데이터를 조회합니다. 없으면 null 반환.
                            var payment = paymentRepository.findByCompanyAndUsageDate(company, usageDate).orElse(null);
                            // 구독 서비스 목록을 조회합니다.
                            var subscriptionServiceList = subscriptionRepository.findAllByCompanyIdsAndDate(
                                    List.of(company.getId()), usageDate
                            ).stream().map(Subscription::getService).toList();
                            // 스토리지 사용량 데이터를 조회합니다.
                            var storageUsage = storageService.getDataUsageByCompanyId(company.getId()).getTotalStorageUsage();
                            // 결제 금액을 계산합니다.
                            var amount = paymentService.calculateAmount(meteredUsage, subscriptionServiceList, storageUsage);
                            // Payment 정보가 없으면 새로 생성합니다.
                            if (payment == null) {
                                paymentRepository.save(
                                        Payment.builder()
                                                .company(company)
                                                .meteredUsage(meteredUsage)
                                                .subscriptionServiceList(new ArrayList<>(subscriptionServiceList)) // Immutable issue 방지
                                                .storageUsage(storageUsage)
                                                .method(PaymentMethod.CARD) // 결제 수단은 카드로 고정
                                                .amount(amount) // 계산된 금액
                                                .status(PaymentStatus.OUTSTANDING) // 결제 상태는 OUTSTANDING으로 설정
                                                .scheduledPaymentDate(usageDate.plusMonths(1).withDayOfMonth(10)) // 다음 달 10일에 결제 예정
                                                .usageDate(usageDate) // 사용일 설정
                                                .build()
                                );
                                //log.info("Created new Payment for Company ID: {}, Date: {}, Amount: {}", company.getId(), usageDate, amount);
                            } else {
                                // 기존 Payment가 있으면 결제 정보를 업데이트합니다.
                                payment.setSubscriptionServiceList(new ArrayList<>(subscriptionServiceList)); // Immutable issue 방지
                                payment.setStorageUsage(storageUsage);
                                // 결제 상태가 COMPLETE가 아닌 경우에만 amount 수정
                                if (!payment.getStatus().equals(PaymentStatus.COMPLETE)) {
                                    payment.setAmount(amount);
                                }
                                paymentRepository.save(payment);
                                //log.info("Updated Payment ID: {} for Company ID: {}, Date: {}, New Amount: {}", payment.getId(), company.getId(), usageDate, amount);
                            }
                        });
                    },
                    // MeteredUsage 데이터가 없으면 경고 로그를 출력합니다.
                    () -> log.warn("Company ID: {} has no MeteredUsage records. Skipping.", company.getId())
            );
        });
        return RepeatStatus.FINISHED;
    }
}