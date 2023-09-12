package forum.hub.domain.member.service;

import forum.hub.domain.entity.Member;
import forum.hub.domain.member.repository.MemberRepository;
import forum.hub.domain.member.exception.MemberNotFoundException;
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
        Optional<Member> findMember = memberRepository.findByPasswordHash(passwordHash);
        return findMember.orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }
}
