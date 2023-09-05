package forum.hub.controller.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInFormDto {

    private String username;
    private String email;
    private String password;

}
