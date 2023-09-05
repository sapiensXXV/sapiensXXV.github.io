package forum.hub.entity;

import forum.hub.entity.identifier.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    private int age;
    private String name;
    private String email;
    private String passwordHash;
}
