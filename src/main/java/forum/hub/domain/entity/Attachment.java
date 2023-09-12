package forum.hub.domain.entity;


import forum.hub.domain.entity.identifier.AttachmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Attachment {

    @Id @GeneratedValue
    @Column(name = "attachment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String originFileName;

    private String storeFileName;

    @Enumerated(value = EnumType.STRING)
    private AttachmentType attachmentType;

}
