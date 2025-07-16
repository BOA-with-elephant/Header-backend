package com.header.header.domain.user.controller;

import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.user.facade.UserFacadeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class UserController {
    private final UserFacadeService userFacadeServiceService;

    public UserController(UserFacadeService userFacadeServiceService) {
        this.userFacadeServiceService = userFacadeServiceService;
    }

    @GetMapping("/session")
    public void signup(){
        //void = user/signup으로 (자동) 반환
    }

    //post 요청 받아줄 핸들러 메소드 작성
    @PostMapping("/users")
    public ModelAndView signup(ModelAndView mv, @ModelAttribute SignupDTO signupDTO){
        //@ModelAttribute SignupDTO signupDTO ->
        // 원래는 signupDTO가 유효성에 부합하는지도 확인해야함.
        //그러나 이번엔 간단하게 바로 DTO 적용하는 걸로.

        int result = userFacadeServiceService.registerUser(signupDTO);
        //수행횟수(숫자)를 반환 받기위해 int result로 변수 생성

        String msg = "";

        if(result >0){
            msg = "Successfully Signed up";
            mv.setViewName("auth/session.html");
            // 회원가입 성공 시 로그인 페이지로 이동
        }else {
            msg="Fail to sign up";
            mv.setViewName("auth/users");
            //실패 시 회원가입 form("auth/users")으로 다시 돌아오도록 설정
        }
        //signup 페이지 <header> <script> 내에 메세지 alert 뜨도록 작성
        mv.addObject("msg", msg);

        return mv;
    }
}
