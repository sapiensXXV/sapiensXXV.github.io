package forum.hub.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberDto {

    public String name;
    public String email;
    public String passwordHash;

}
