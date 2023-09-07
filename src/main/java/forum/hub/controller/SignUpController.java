package forum.hub.controller;

import forum.hub.controller.dto.SignUpFormDto;
import forum.hub.service.member.MemberService;
import forum.hub.service.member.MemberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final MemberUtil memberUtil;
    private final MemberService memberService;

    @GetMapping("/add")
    public String addForm() {
        return "members/sign-in";
    }

    @PostMapping("/add")
    public String save(SignUpFormDto signUpDto) {

        log.info("username={}, email={}, password={}", signUpDto.getUsername(), signUpDto.getEmail(), signUpDto.getPassword());
        String hashPassword = memberUtil.hashPassword(signUpDto.getPassword());
        log.info("hash password={}", hashPassword);

        return "redirect:/";
    }

}
