package forum.hub.domain.signup.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpFormDto {

    private String username;
    private String email;
    private String password;

}
