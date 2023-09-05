package forum.hub.service.member;

import forum.hub.entity.Member;
import forum.hub.repository.MemberRepository;
import forum.hub.service.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //클래스레벨: 읽기전용
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public Member findByPasswordHash(String passwordHash) {
        Optional<Member> byPasswordHash = memberRepository.findByPasswordHash(passwordHash);
        return byPasswordHash.orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }
}
