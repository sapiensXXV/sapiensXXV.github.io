package forum.hub.controller;

import forum.hub.controller.dto.SignInDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class SignInController {

    @GetMapping("/add")
    public String addForm() {
        return "members/sign-in";
    }

    @PostMapping("/add")
    public String save(String username, String email, String password) {
        SignInDto signInDto = SignInDto.builder()
            .username(username)
            .email(email)
            .password(password)
            .build();

        return "redirect:/";
    }

}
