package forum.hub.domain.member.repository;

import forum.hub.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    public Optional<Member> findByPasswordHash(String passwordHash);

    public Optional<Member> findByEmail(String email);

    public Optional<Member> findByName(String name);

}
