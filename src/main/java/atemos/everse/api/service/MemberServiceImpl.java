package atemos.everse.api.service;

import atemos.everse.api.config.EncryptUtil;
import atemos.everse.api.config.JwtUtil;
import atemos.everse.api.domain.MemberRole;
import atemos.everse.api.dto.MemberDto;
import atemos.everse.api.entity.Member;
import atemos.everse.api.entity.Menu;
import atemos.everse.api.repository.CompanyRepository;
import atemos.everse.api.repository.MemberRepository;
import atemos.everse.api.repository.MenuRepository;
import atemos.everse.api.repository.SubscriptionRepository;
import atemos.everse.api.specification.MemberSpecification;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MemberServiceImpl 클래스는 사용자(Member) 관련된 기능을 구현하는 서비스 클래스입니다.
 * 사용자의 생성, 조회, 수정, 삭제 기능을 제공하며, 사용자 정보의 암호화 및 복호화,
 * 접근 가능한 메뉴 설정 등의 기능을 포함합니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationServiceImpl authenticationService;
    private final EmailService emailService;
    private final EncryptUtil encryptUtil;
    private final JwtUtil jwtUtil;

    /**
     * 사용자를 등록하는 메서드입니다.
     * @param createMemberDto 사용자 생성 정보
     * @return 등록된 사용자 정보 응답 객체
     */
    @Override
    @Transactional
    public MemberDto.ReadMemberResponse create(MemberDto.CreateMember createMemberDto) {
        // 업체가 존재하는지 확인
        var company = companyRepository.findById(createMemberDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("No such company."));
        // 이메일 및 전화번호 중복 확인
        checkDuplicateMember(createMemberDto.getEmail(), createMemberDto.getPhone());
        // Member 엔티티 생성 및 저장 (이름, 이메일, 전화번호 암호화)
        var member = Member.builder()
                .name(encryptUtil.encrypt(createMemberDto.getName()))
                .email(encryptUtil.encrypt(createMemberDto.getEmail()))
                .role(createMemberDto.getRole())
                .phone(encryptUtil.encrypt(createMemberDto.getPhone()))
                .company(company)
                .password(passwordEncoder.encode(createMemberDto.getPassword()))
                .build();
        // 접근 가능한 메뉴 조회
        var accessibleMenuIds = menuRepository.findAllByAccessibleRolesContains(createMemberDto.getRole()).stream()
                .map(Menu::getId)
                .collect(Collectors.toSet());
        // 엔티티 저장
        memberRepository.save(member);
        // 웰컴 메일 발송
        try {
            var subject = "[E-Verse] Welcome to the Future of Energy Management!";
            var message = String.format("""
                <html><body>
                <p>Dear %s,</p>
                <br>
                <p>We are excited to welcome you to ATEMoS's E-Verse platform, where energy management meets innovation!</p>
                <p>At E-Verse, we offer state-of-the-art tools that allow you to accurately <strong>measure</strong> your energy consumption, <strong>predict</strong> future usage trends, and optimize your energy spending.</p>
                <br>
                <p>With our advanced analytics, you can monitor your energy usage in real-time, receive accurate forecasts on your upcoming bills, and make data-driven decisions to save on energy costs.</p>
                <br>
                <p>To get started, log in to your dashboard, explore the features, and discover how you can maximize your energy efficiency with E-Verse.</p>
                <br>
                <p>Thank you for joining us on this journey toward smarter energy management!</p>
                <p>Best regards,</p>
                <p>The ATEMoS Team</p>
                </body></html>
                """, encryptUtil.decrypt(member.getName()));
            emailService.sendEmail(encryptUtil.decrypt(member.getEmail()), subject, message);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
        // 복호화된 name, email, phone 필드를 사용하여 응답 객체 생성
        var decryptedName = encryptUtil.decrypt(member.getName());
        var decryptedEmail = encryptUtil.decrypt(member.getEmail());
        var decryptedPhone = encryptUtil.decrypt(member.getPhone());
        return MemberDto.ReadMemberResponse.builder()
                .memberId(member.getId())
                .companyId(company.getId())
                .companyName(company.getName())
                .companyType(company.getType())
                .role(member.getRole())
                .name(decryptedName)
                .email(decryptedEmail)
                .phone(decryptedPhone)
                .status(member.getStatus())
                .accessibleMenuIds(accessibleMenuIds)
                .build();
    }

    /**
     * 조건에 맞는 사용자 목록을 조회하는 메서드입니다.
     * @param readMemberRequestDto 사용자 조회 조건
     * @param pageable 페이징 정보
     * @return 조건에 맞는 사용자 목록과 페이징 정보
     */
    @Override
    @Transactional(readOnly = true)
    public MemberDto.ReadMemberPageResponse read(MemberDto.ReadMemberRequest readMemberRequestDto, Pageable pageable) {
        // 현재 인증된 사용자의 정보에서 타임존 가져오기
        var zoneId = jwtUtil.getCurrentMember().getCompany().getCountry().getZoneId();
        // 조건에 맞는 사용자 목록 조회
        var memberPage = memberRepository.findAll(MemberSpecification.findWith(readMemberRequestDto), pageable);
        // 엔티티 목록을 DTO로 변환하여 리턴
        var memberList = memberPage.getContent().stream()
                .map(member -> {
                    var company = member.getCompany();
                    // 접근 가능한 메뉴 정보 가져오기
                    var accessibleMenuIds = menuRepository.findAllByAccessibleRolesContains(member.getRole()).stream()
                            .filter(menu -> {
                                if (member.getRole() == MemberRole.ADMIN) {
                                    return true; // ADMIN 권한은 모든 메뉴 접근 가능
                                }
                                var requiredSubscription = menu.getRequiredSubscription();
                                if (requiredSubscription == null) {
                                    return true; // 구독이 필요 없는 메뉴
                                }
                                // 구독이 현재 유효한지 확인
                                var count = subscriptionRepository.countValidSubscription(member.getCompany(), requiredSubscription, LocalDate.now(member.getCompany().getCountry().getZoneId()));
                                return count != null && count > 0;
                            })
                            .map(Menu::getId)
                            .collect(Collectors.toUnmodifiableSet());
                    // 복호화 및 마스킹 처리
                    var applyMasking = Boolean.TRUE.equals(readMemberRequestDto.getMasking());
                    var name = decryptAndMask(member.getName(), applyMasking, this::maskName);
                    var email = decryptAndMask(member.getEmail(), applyMasking, this::maskEmail);
                    var phone = decryptAndMask(member.getPhone(), applyMasking, this::maskPhone);
                    // MemberDTO 응답 객체로 Build(name, email, phone은 마스킹 처리)
                    return MemberDto.ReadMemberResponse.builder()
                            .memberId(member.getId())
                            .companyId(company.getId())
                            .companyName(company.getName())
                            .companyType(company.getType())
                            .role(member.getRole())
                            .status(member.getStatus())
                            .name(name)
                            .email(email)
                            .phone(phone)
                            .accessibleMenuIds(accessibleMenuIds)
                            .createdDate(member.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime())
                            .modifiedDate(member.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime())
                            .build();
                })
                .toList();
        // 응답 객체 반환
        return new MemberDto.ReadMemberPageResponse(
                memberList,
                memberPage.getTotalElements(),
                memberPage.getTotalPages());
    }

    /**
     * 암호화된 값을 복호화하고, 필요한 경우 마스킹 처리하는 메서드입니다.
     * @param encryptedValue 암호화된 값
     * @param applyMasking 마스킹 처리 여부
     * @param maskFunction 마스킹 함수
     * @return 복호화된 값 (마스킹 적용 시 마스킹된 값)
     */
    private String decryptAndMask(String encryptedValue, boolean applyMasking, Function<String, String> maskFunction) {
        var decryptedValue = encryptUtil.decrypt(encryptedValue);
        return applyMasking ? maskFunction.apply(decryptedValue) : decryptedValue;
    }

    /**
     * 사용자의 정보를 수정하는 메서드입니다.
     * @param memberId 사용자 ID
     * @param updateMemberDto 사용자 정보 수정 데이터
     * @return 수정된 사용자 정보 응답 객체
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN') or @securityService.isSelf(#memberId)")
    @Transactional
    public MemberDto.ReadMemberResponse update(Long memberId, MemberDto.UpdateMember updateMemberDto) {
        // 해당 사용자가 존재하는지 확인
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 수정하려는 사용자 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(member.getCompany().getId());
        // 이메일 중복 확인
        if (updateMemberDto.getEmail() != null && !member.getEmail().equals(encryptUtil.encrypt(updateMemberDto.getEmail()))) {
            if (memberRepository.existsByEmailAndIdNot(encryptUtil.encrypt(updateMemberDto.getEmail()), member.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
            }
        }
        // 전화번호 중복 확인
        if (updateMemberDto.getPhone() != null && !member.getPhone().equals(encryptUtil.encrypt(updateMemberDto.getPhone()))) {
            if (memberRepository.existsByPhoneAndIdNot(encryptUtil.encrypt(updateMemberDto.getPhone()), member.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already in use.");
            }
        }
        // 해당 업체가 존재하는지 확인
        var company = Optional.ofNullable(updateMemberDto.getCompanyId())
                .map(companyId -> companyRepository.findById(companyId)
                        .orElseThrow(() -> new EntityNotFoundException("No such company.")))
                .orElse(member.getCompany());
        member.setCompany(company);
        // 사용자 정보 업데이트
        Optional.ofNullable(updateMemberDto.getName()).map(encryptUtil::encrypt).ifPresent(member::setName);
        Optional.ofNullable(updateMemberDto.getEmail()).map(encryptUtil::encrypt).ifPresent(member::setEmail);
        Optional.ofNullable(updateMemberDto.getPhone()).map(encryptUtil::encrypt).ifPresent(member::setPhone);
        Optional.ofNullable(updateMemberDto.getPassword()).map(passwordEncoder::encode).ifPresent(member::setPassword);
        Optional.ofNullable(updateMemberDto.getFailedLoginAttempts()).ifPresent(member::setFailedLoginAttempts);
        Optional.ofNullable(updateMemberDto.getRole()).ifPresent(member::setRole);
        Optional.ofNullable(updateMemberDto.getStatus()).ifPresent(member::setStatus);
        // 접근 가능한 메뉴 조회
        var accessibleMenuIds = menuRepository.findAllByAccessibleRolesContains(Optional.ofNullable(updateMemberDto.getRole()).orElse(member.getRole())).stream()
                .map(Menu::getId)
                .collect(Collectors.toSet());
        // 회사가 소속된 국가의 시간대 정보를 가져오기
        var zoneId = company.getCountry().getZoneId();
        // 엔티티 저장
        memberRepository.save(member);
        // 복호화된 name, email, phone 필드를 사용하여 응답 객체 생성
        var decryptedName = encryptUtil.decrypt(member.getName());
        var decryptedEmail = encryptUtil.decrypt(member.getEmail());
        var decryptedPhone = encryptUtil.decrypt(member.getPhone());
        return MemberDto.ReadMemberResponse.builder()
                .memberId(member.getId())
                .companyId(company.getId())
                .companyName(company.getName())
                .companyType(company.getType())
                .role(member.getRole())
                .name(decryptedName)
                .email(decryptedEmail)
                .phone(decryptedPhone)
                .status(member.getStatus())
                .accessibleMenuIds(accessibleMenuIds)
                .createdDate(member.getCreatedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime())
                .modifiedDate(member.getModifiedDate().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime())
                .build();
    }

    /**
     * 사용자를 삭제하는 메서드입니다.
     * @param memberId 사용자 ID
     */
    @Override
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Transactional
    public void delete(Long memberId) {
        // 사용자 정보가 있는지 확인
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("No such member."));
        // 호출하는 사용자가 ADMIN이거나 사용자의 companyId와 삭제하려는 사용자 정보의 companyId가 일치할 때만 실행
        authenticationService.validateCompanyAccess(member.getCompany().getId());
        // 사용자 정보 삭제
        memberRepository.delete(member);
    }

    /**
     * 이메일 또는 전화번호가 중복되는지 확인하는 메서드입니다.
     * @param email 사용자 이메일
     * @param phone 사용자 전화번호
     */
    @Override
    @Transactional(readOnly = true)
    public void checkDuplicateMember(String email, String phone) {
        var encryptedEmail = encryptUtil.encrypt(email);
        var encryptedPhone = encryptUtil.encrypt(phone);
        if (memberRepository.existsByEmailOrPhone(encryptedEmail, encryptedPhone)) {
            throw new EntityExistsException("Member with this email or phone already exists");
        }
    }

    /**
     * 사용자 이메일로 사용자 정보를 로드합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티 객체
     */
    @Override
    public Member loadMemberByEmail(String email) {
        return memberRepository.findByEmail(encryptUtil.encrypt(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No such member."));
    }

    /**
     * 이름 마스킹: 이름의 첫 글자만 남기고 나머지는 별표(*) 처리
     * @param name 이름
     * @return 마스킹된 이름
     */
    private String maskName(String name) {
        return (name == null || name.length() < 2) ? name : name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 이메일 마스킹: '@' 이전의 절반을 별표(*) 처리
     * @param email 이메일
     * @return 마스킹된 이메일
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        var parts = email.split("@");
        return parts[0].charAt(0) + "*".repeat(parts[0].length() - 1) + "@" + parts[1];
    }

    /**
     * 전화번호 마스킹: 중간 4자리를 별표(*) 처리
     * @param phone 전화번호
     * @return 마스킹된 전화번호
     */
    private String maskPhone(String phone) {
        return (phone == null || phone.length() < 4) ? phone : phone.substring(0, phone.length() - 4) + "****";
    }
}