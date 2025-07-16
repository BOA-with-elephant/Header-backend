package com.header.header.domain.user.facade;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.menu.service.MenuCategoryService;
import com.header.header.domain.message.service.MessageSendBatchService;
import com.header.header.domain.message.service.MessageTemplateService;
import com.header.header.domain.message.service.ShopMessageHistoryService;
import com.header.header.domain.reservation.service.BossReservationService;
import com.header.header.domain.sales.service.SalesService;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import com.header.header.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;


import static com.header.header.auth.common.ApiResponse.SUCCESS_REGISTER_USER;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

    private final UserService userService;
    private final MainUserRepository userRepository;
    private final BossReservationService bossReservationService;
    private final SalesService salesService;
    private final MessageSendBatchService messageSendBatchService;
    private final MessageTemplateService messageTemplateService;
    private final MenuCategoryService menuCategoryService;
    private final ShopMessageHistoryService shopMessageHistoryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* 1. 더미 유저 등록 (전화번호/이름 기반)
    * 업장에서 전화번호 + 이름만으로 임시 가입한 dummy user 생성 */
    @Transactional
    public User registerDummyUser(String userName, String userPhone) {
        return userService.createUserByNameAndPhone(userName, userPhone);
    }

    /* 2. 이름/전화번호로 유저 코드 조회 */
    public Integer getUserCodeByNameAndPhone(String name, String phone) {
        return userService.findUserByNameAndPhone(name, phone);
    }

    /* 3. 회원가입 처리 (중복검사 포함) */
    @Transactional
    public int registerUser(SignupDTO signupDTO) {
        signupDTO.setUserPwd(passwordEncoder.encode(signupDTO.getUserPwd()));

        String resultMessage = userService.registerNewUser(signupDTO);

        // Convert the String message to an int for the controller
        if (resultMessage.equals(SUCCESS_REGISTER_USER.getMessage())) {
            return 1; // Or any positive integer to indicate success
        } else {
            return 0; // Or any non-positive integer to indicate failure
        }
    }

    //spring security filter를 통해 적용되는 코드.
    public int regist(SignupDTO signupDTO) {

        signupDTO.setUserPass(passwordEncoder.encode(signupDTO.getUserPass()));

        int result = 0;

        //error 발생시 중단되지 않고 실행하도록 try/catch문 넣기
        try {
            result = userMapper.regist(signupDTO);
        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    /* 4. 로그인 시 유저 정보 조회 */
    public LoginUserDTO login(String userId) {
        return userService.findByUserId(userId);
    }

    /* 5. 회원 정보 수정 */
    @Transactional
    public String updateUser(UserDTO dto) {
        return userService.modifyUser(dto);
    }

    /* 6. 회원 탈퇴 처리 (isLeave = true) */
    @Transactional
    public void withdrawUser(UserDTO dto) {
        userService.deleteUser(dto);
    }
}