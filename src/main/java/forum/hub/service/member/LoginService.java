package forum.hub.service.member;

import forum.hub.dto.LoginFormDto;
import forum.hub.dto.MemberDto;
import forum.hub.entity.Member;
import forum.hub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;

    /**
     * @param email 이메일
     * @param password 패스워드
     * @return 회원이라면 Member 객체 반환, 회원이 아니라면 null 반환
     */
    public MemberDto login(String email, String password) {
        return memberRepository.findByEmail(email)
            .filter(m -> m.getPasswordHash().equals(memberUtil.hashPassword(password)))
            .map(m -> Member.getMemberDto(m.getName(), m.getEmail(), m.getPasswordHash()))
            .orElse(null);
    }

}
