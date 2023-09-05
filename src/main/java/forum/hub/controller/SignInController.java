package forum.hub.controller;

import forum.hub.controller.dto.SignInFormDto;
import forum.hub.service.MemberService;
import forum.hub.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignInController {

    private final LoginService loginService;
    private final MemberService memberService;

    @GetMapping("/add")
    public String addForm() {
        return "members/sign-in";
    }

    @PostMapping("/add")
    public String save(SignInFormDto signInDto) {

        log.info("username={}, email={}, password={}", signInDto.getUsername(), signInDto.getEmail(), signInDto.getPassword());
        String hashPassword = loginService.hashPassword(signInDto.getPassword());
        log.info("hash password={}", hashPassword);

        return "redirect:/";
    }

}
