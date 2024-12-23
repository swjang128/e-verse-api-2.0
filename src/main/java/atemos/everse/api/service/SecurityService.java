package atemos.everse.api.service;

import atemos.everse.api.config.EncryptUtil;
import atemos.everse.api.entity.Member;
import atemos.everse.api.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

/**
 * SecurityService는 현재 로그인된 사용자의 정보와 특정 사용자 ID를 비교하여
 * 요청한 사용자가 자기 자신인지 확인하는 기능을 제공합니다.
 */
@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final MemberRepository memberRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 현재 로그인한 사용자가 요청한 사용자 ID와 동일한지 확인.
     *
     * @param id 확인할 사용자 ID
     * @return 요청한 사용자 ID와 현재 사용자의 ID가 동일한지 여부
     */
    @Transactional(readOnly = true)
    public boolean isSelf(Long id) {
        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 현재 사용자의 이메일 가져오기
        String currentUserEmail = authentication.getName();
        // 이메일을 암호화하여 데이터베이스에서 사용자 정보 조회
        Member currentMember = memberRepository.findByEmail(encryptUtil.encrypt(currentUserEmail))
                .orElseThrow(() -> new NotFoundException("No such member."));
        // 현재 사용자의 ID가 요청된 ID와 동일한지 여부를 리턴
        return currentMember.getId().equals(id);
    }
}