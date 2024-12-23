package atemos.everse.api.service;

import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.domain.SubscriptionServiceList;
import atemos.everse.api.dto.ChatbotDto;
import atemos.everse.api.entity.Company;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.EnergyRepository;
import atemos.everse.api.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * ChatbotServiceImpl은 ChatbotService 인터페이스를 구현하여
 * 챗봇의 비즈니스 로직을 처리합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
    private final EnergyRepository energyRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthenticationService authenticationService;

    /**
     * 사용자의 질문을 처리하여 응답을 생성합니다.
     *
     * @param request 챗봇 요청 DTO
     * @return 챗봇 응답 DTO
     */
    @Override
    public ChatbotDto.ChatbotResponse chatbot(ChatbotDto.ChatbotRequest request) {
        try {
            // 업체 정보 조회
            var company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("No such company."));
            var member = authenticationService.getCurrentUserInfo();
            // 현재 해당 업체가 AI_ENERGY_USAGE_FORECAST 서비스를 구독 중이지 않으면 403 Forbidden 예외 발생
            if (member.getRole() != MemberRole.ADMIN && subscriptionRepository.countValidSubscription(
                    company,
                    SubscriptionServiceList.AI_ENERGY_USAGE_FORECAST,
                    LocalDate.now(company.getCountry().getZoneId())) == 0) {
                // ADMIN이 아닌 경우 구독 필요
                throw new AccessDeniedException("Subscription to the service is required.");
            }
            // 연도와 월 파싱 및 메시지 생성
            return parseYearMonth(request.getQuestion())
                    .map(yearMonth -> {
                        var energyUsage = getEnergyUsage(company, yearMonth.year(), yearMonth.month());
                        // 에너지 사용량 데이터가 없는 경우 처리
                        var message = energyUsage == null || energyUsage.compareTo(BigDecimal.ZERO) == 0 ?
                                "%d년 %d월의 에너지 사용량 데이터가 존재하지 않습니다.".formatted(yearMonth.year(), yearMonth.month()) :
                                "네! [%s]업체의 %d년 %d월 에너지 사용량은 %.2f kWh입니다.".formatted(company.getName(), yearMonth.year(), yearMonth.month(), energyUsage);
                        return ChatbotDto.ChatbotResponse.builder()
                                .response(message)
                                .build();
                    })
                    .orElseGet(() -> ChatbotDto.ChatbotResponse.builder()
                            .response("죄송합니다. 요청하신 연도와 월을 이해하지 못했습니다.")
                            .build());
        } catch (Exception e) {
            log.error("Error processing chatbot request", e);
            return ChatbotDto.ChatbotResponse.builder()
                    .response("죄송합니다. 요청하신 연도와 월을 이해하지 못했습니다.")
                    .build();
        }
    }

    /**
     * 사용자의 질문에서 연도와 월을 추출합니다.
     * 이 메서드는 정규식을 사용하여 사용자의 질문에서 연도와 월 정보를 파싱합니다.
     * 예를 들어, "2023년 10월 에너지 사용량을 보여줘"라는 질문에서 2023과 10을 추출합니다.
     *
     * @param question 사용자의 질문 문자열
     * @return 연도와 월을 담은 Optional 객체 (파싱 실패 시 Optional.empty())
     */
    private Optional<YearMonth> parseYearMonth(String question) {
        var pattern = Pattern.compile("(\\d{4})년\\s*(\\d{1,2})월");
        var matcher = pattern.matcher(question);
        if (matcher.find()) {
            var year = Integer.parseInt(matcher.group(1));
            var month = Integer.parseInt(matcher.group(2));
            // 월이 1부터 12 사이인지 검증
            if (month < 1 || month > 12) {
                log.error("Invalid month: {} (valid range is 1-12)", month);
                return Optional.empty();
            }
            return Optional.of(new YearMonth(year, month));
        }
        return Optional.empty();
    }

    /**
     * 특정 업체의 특정 연도와 월에 대한 에너지 사용량을 조회합니다.
     * 이 메서드는 업체의 타임존을 고려하여 해당 월의 시작과 끝 시간을 계산하고,
     * 데이터베이스에서 해당 기간의 에너지 사용량을 합산하여 반환합니다.
     *
     * @param company 조회할 업체 엔티티
     * @param year    조회할 연도
     * @param month   조회할 월 (1부터 12까지)
     * @return 에너지 사용량 (BigDecimal), 데이터가 없을 경우 null 반환
     */
    private BigDecimal getEnergyUsage(Company company, int year, int month) {
        // 조회할 기간의 시작과 끝을 계산합니다.
        var start = LocalDateTime.of(year, month, 1, 0, 0);
        var end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        // 에너지 사용량을 조회 후 리턴
        return energyRepository.getTotalFacilityUsage(company, start, end);
    }

    /**
     * 연도와 월을 나타내는 불변 객체입니다.
     * 이 클래스는 연도와 월을 함께 관리하기 위한 레코드 타입으로,
     * 연산의 편의성을 제공합니다.
     */
    private record YearMonth(int year, int month) {}
}