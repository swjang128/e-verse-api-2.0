package atemos.everse.api.service;

import atemos.everse.api.domain.CompanyType;
import atemos.everse.api.dto.OptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OptionServiceImpl는 OptionService 인터페이스를 구현한 클래스입니다.
 * 이 서비스 구현체는 서비스의 전반적인 설정 상태를 제어하는 기능과 관련된 작업을 처리하는 메서드를 정의합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {
    /**
     * 서비스 전반적인 설정 상태를 조회합니다.
     *
     * @return 서비스 전반적인 설정 상태
     */
    @Override
    public OptionDto.OptionResponse read() {
        // 2FA가 필요한 국가 리스트 (고정 값: "Korea")
        String login2FactorCountry = "Korea";
        // 2FA 발송 매체 (고정 값: "Email")
        String login2FactorMethod = "Email";
        // 2FA 인증 시간 제한 (고정 값: 3분)
        Long login2FactorTimeoutMinutes = 3L;
        // 사용자 등록 후 Email 인증 사용 여부 (고정 값: "N")
        String signupEmailVerificationRequired = "N";
        // 과거 데이터 검색 최장 범위 (고정 값: 6개월)
        Long maxSearchPeriodMonths = 6L;
        // 선택 가능한 업체 유형 (고정 값: FEMS, BEMS)
        List<CompanyType> companyUsageType = List.of(CompanyType.FEMS, CompanyType.BEMS);
        // OptionResponse DTO 생성 후 반환
        return OptionDto.OptionResponse.builder()
                .login2FactorCountry(login2FactorCountry)
                .login2FactorMethod(login2FactorMethod)
                .login2FactorTimeoutMinutes(login2FactorTimeoutMinutes)
                .signupEmailVerificationRequired(signupEmailVerificationRequired)
                .maxSearchPeriodMonths(maxSearchPeriodMonths)
                .companyUsageType(companyUsageType)
                .build();
    }
}