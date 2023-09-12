package forum.hub.domain.entity;

import forum.hub.domain.entity.identifier.Topic;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends DateEntity{

    @Id @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    private Long userId;

    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "post")
    List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    List<Attachment> attachments = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private Topic topic;

    /**
     * TODO: 연관과녜 편의 메서드 추가.
     */
}
