package forum.hub.global.config;


import forum.hub.domain.entity.Member;
import forum.hub.domain.member.repository.MemberRepository;
import forum.hub.global.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

@Configuration
@RequiredArgsConstructor
public class MemberConfig {

    private final PasswordUtil passwordUtil;
    private final MemberRepository repository;

    @Bean
    public void initMembers() {
        Member member = new Member("123123", "123123", passwordUtil.hashPassword("123123"));
        repository.save(member);
    }
}
