package forum.hub.domain.login.service;

import forum.hub.domain.dto.MemberDto;
import forum.hub.domain.entity.Member;
import forum.hub.domain.member.repository.MemberRepository;
import forum.hub.global.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordUtil passwordUtil;

    /**
     * @param email 이메일
     * @param password 패스워드
     * @return 회원이라면 Member 객체 반환, 회원이 아니라면 null 반환
     */
    public MemberDto login(String email, String password) {
        return memberRepository.findByEmail(email)
            .filter(m -> m.getPasswordHash().equals(passwordUtil.hashPassword(password)))
            .map(m -> Member.getMemberDto(m.getName(), m.getEmail(), m.getPasswordHash()))
            .orElse(null);
    }

}
