package com.header.header.domain.user.controller;

import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.facade.UserFacadeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class UserController {
    private final UserFacadeService userFacadeServiceService;

    public UserController(UserFacadeService userFacadeServiceService) {
        this.userFacadeServiceService = userFacadeServiceService;
    }

    @GetMapping("/users")
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

    @GetMapping("/session")
    public void login() {
    }

    //로그인 실패시 핸들해줄 핸들러 메소드 작성
    @GetMapping("/session/fail")
    public ModelAndView loginFail(ModelAndView mv,
                                  @RequestParam(value = "msg"
                                          , defaultValue = "login failed") String msg) {
        //@RequestParam을 통해 어떤 실패가 일어난 건지 메시지 받는다.
        mv.addObject("msg", msg);
        mv.setViewName("/auth/fail");
        return mv;
    }

    // 회원정보 수정 시
    @GetMapping("/auth/{user_id}")
    public void modifyUser(){}

    @PutMapping("/auth/{user_id}")
    public String modifyUser(@ModelAttribute UserDTO userDTO) {
        userFacadeServiceService.updateUser(userDTO);
        return "redirect:/session";
    }

    // 회원탈퇴 시
    @GetMapping("/auth/{user_id}/leave")
    public void leaveHeader() {}

    //회원탈퇴 시 patchmapping 이용해서 isAdmin 변경
    @PatchMapping("/auth/{user_id}/leave")
    public String leaveHeader(@ModelAttribute UserDTO userDTO) {
        userFacadeServiceService.withdrawUser(userDTO);
        return "redirect:/";
    }
}
