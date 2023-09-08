package forum.hub.repository;

import forum.hub.entity.Member;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    public Optional<Member> findByPasswordHash(String passwordHash);

    public Optional<Member> findByEmail(String email);

    public Optional<Member> findByName(String name);

}
