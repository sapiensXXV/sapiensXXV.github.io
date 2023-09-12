package forum.hub.domain.login.controller;


import forum.hub.domain.login.dto.LoginFormDto;
import forum.hub.domain.dto.MemberDto;
import forum.hub.domain.login.service.LoginService;
import forum.hub.global.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginForm() {

        return "login/login-form";
    }

    @PostMapping("login")
    public String login(
        @ModelAttribute LoginFormDto form,
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(defaultValue = "/") String redirectURL,
        Model model
    ) {
        MemberDto loginMemberDto = loginService.login(form.getEmail(), form.getPassword());
        if (loginMemberDto == null) {
            return "login/login-form";
        }

        // TODO 로그인 성공처리
        // 세션이 있으면 세션을 반환하고, 없으면 신규세션을 반환한다.
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMemberDto);

        return "redirect:" + redirectURL;
    }
}
