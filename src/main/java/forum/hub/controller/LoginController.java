package forum.hub.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm() {

        return "members/login-view";
    }

    @PostMapping("login")
    public String login(String email, String password) {
        log.info("email = [{}], password = [{}]", email, password);
        return "redirect:/";
    }
}
