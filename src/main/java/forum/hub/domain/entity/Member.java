package forum.hub.domain.entity;

import forum.hub.domain.entity.identifier.UserRole;
import forum.hub.domain.dto.MemberDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    private String name;
    private String email;
    private String passwordHash;

    //생성자
    @Builder
    public Member(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static MemberDto getMemberDto(String name, String email, String passwordHash) {
        return MemberDto.builder()
            .name(name)
            .email(email)
            .passwordHash(passwordHash)
            .build();
    }
}
