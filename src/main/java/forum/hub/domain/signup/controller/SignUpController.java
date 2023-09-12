package forum.hub.domain.signup.controller;

import forum.hub.domain.signup.dto.SignUpFormDto;
import forum.hub.domain.entity.Member;
import forum.hub.domain.member.service.MemberService;
import forum.hub.global.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignUpController {

    private final PasswordUtil passwordUtil;
    private final MemberService memberService;

    @GetMapping("/add")
    public String addForm() {
        return "members/sign-in";
    }

    @PostMapping("/add")
    public String save(SignUpFormDto dto) {
        String hashPassword = passwordUtil.hashPassword(dto.getPassword());
        memberService.save(new Member(dto.getUsername(), dto.getEmail(), passwordUtil.hashPassword(dto.getPassword())));

        return "redirect:/";
    }

}
