package forum.hub.dto;

import lombok.Builder;

@Builder
public class MemberDto {

    public String name;
    public String email;
    public String passwordHash;

}
