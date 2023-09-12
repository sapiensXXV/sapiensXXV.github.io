package forum.hub.domain.board.controller;


import forum.hub.domain.dto.MemberDto;
import forum.hub.global.web.session.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
public class BoardController {

    @GetMapping("/board")
    public String showBoard(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) MemberDto member
    ) {
        System.out.println("BoardController.showBoard");
        if (member == null) {
            return "redirect:/";
        }

        String name = member.getName();
        String email = member.getEmail();
        String passwordHash = member.getPasswordHash();

        //TODO 페이지네이션을 수행해서 페이지 단위로 글을 보여주어야한다. 지금은 데이터베이스에 저장되어 있는 모든것을 보여준다.
        log.info("로그인 유저 - 게시판에 접근 username=[{}], email=[{}]", name, email);

        return "board/post-list";
    }

}
