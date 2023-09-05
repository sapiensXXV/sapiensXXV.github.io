package forum.hub.repository;

import forum.hub.entity.Member;
import forum.hub.service.login.LoginService;
import forum.hub.service.login.SessionLoginService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired SessionLoginService loginService;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    public void testData() {

        String hashPassword1 = loginService.hashPassword("hash1");
        String hashPassword2 = loginService.hashPassword("hash2");
        String hashPassword3 = loginService.hashPassword("hash3");

        Member member1 = new Member("member1", "aaa@gmail.com", hashPassword1);
        Member member2 = new Member("member2", "bbb@gmail.com", hashPassword2);
        Member member3 = new Member("member3", "ccc@gmail.com", hashPassword3);

    }

    @Test
    public void findByHashPasswordTest() throws Exception {
        final String PASSWORD = "member1_password";

        String hashPassword = loginService.hashPassword(PASSWORD);
        Member savedMember = memberRepository.save(new Member("member", "asdf@gmail.com", hashPassword));
        Member findMember = memberRepository.findByPasswordHash(hashPassword).get();

        assertThat(savedMember).isEqualTo(findMember);
    }

    @Test
    public void findByEmailTest() {
        final String E_MAIL = "email_example@gmail.com";
        Member savedMember = memberRepository.save(new Member("member", E_MAIL, "hash_password"));
        Member findMember = memberRepository.findByEmail(E_MAIL).get();

        assertThat(savedMember).isEqualTo(findMember);
    }

    @Test
    public void findByUsernameTest() {
        final String NAME = "name_example";

        Member savedMember = memberRepository.save(new Member(NAME, "email_example@gmail.com", "hash_password"));
        Member findMember = memberRepository.findByName(NAME).get();

        assertThat(savedMember).isEqualTo(findMember);
    }

}