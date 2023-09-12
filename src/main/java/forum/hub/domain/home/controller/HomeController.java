package forum.hub.domain.home.controller;

import forum.hub.domain.dto.MemberDto;
import forum.hub.global.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(
        HttpServletRequest request,
        Model model
    ) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            // 로그인한 사용자가 아니라면 일반 홈으로 이동
            return "home";
        }

        MemberDto memberDto = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("member", memberDto);

        return "login-home";

    }

}
