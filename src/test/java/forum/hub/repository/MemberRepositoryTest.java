package forum.hub.repository;

import forum.hub.domain.entity.Member;
import forum.hub.domain.member.repository.MemberRepository;
import forum.hub.global.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    PasswordUtil passwordUtil;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void testData() {

        String hashPassword1 = passwordUtil.hashPassword("hash1");
        String hashPassword2 = passwordUtil.hashPassword("hash2");
        String hashPassword3 = passwordUtil.hashPassword("hash3");

        Member member1 = new Member("member1", "aaa@gmail.com", hashPassword1);
        Member member2 = new Member("member2", "bbb@gmail.com", hashPassword2);
        Member member3 = new Member("member3", "ccc@gmail.com", hashPassword3);

    }

    @Test
    public void findByHashPasswordTest() throws Exception {
        final String PASSWORD = "member1_password";

        String hashPassword = passwordUtil.hashPassword(PASSWORD);
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