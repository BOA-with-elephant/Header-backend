package com.header.header.domain.user.service;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.header.header.auth.common.ApiResponse.*;
import static com.header.header.auth.common.ApiResponse.SUCCESS_MODIFY_USER;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MainUserRepository userRepository;
    private final ModelMapper modelMapper;

    /***
     * dummy user을 추가한다.
     * dummy user = 이름과 전화번호만 있는 유령 유저 정보
     *
     * @param userName 유저 이름
     * @param userPhone 유저 핸드폰 번호
     * */
    @Transactional
    public User createUserByNameAndPhone(String userName, String userPhone){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        userDTO.setUserPhone(userPhone);
        return userRepository.save(modelMapper.map(userDTO, User.class));
    }

    /***
     * 이름과 핸드폰 번호로 userCode를 조회한다.
     *
     * @param userName 유저 이름
     * @param userPhone 유저 핸드폰 번호
     * */
    public Integer findUserByNameAndPhone(String userName, String userPhone){
        User find =  userRepository.findByUserNameAndUserPhone(userName, userPhone);

        return find != null ? find.getUserCode() : null;
    }

    public String getPhoneByUserCode(Integer userCode){
        return userRepository.findPhoneByUserCode(userCode);
    }

    /** save : registerNewUser
    -> SignupDTO 사용
    @param signupDTO 생성할 user 정보가 담긴 DTO
    @return 생성된 signupDTO(user관련 DTO)
     이미 존재하는 아이디나 전화번호일 때 */ /* @throws ApiResponse 이 부분 없는데 써서 오류나서 지웠어요 - 예람*/
    @Transactional
    public String registerNewUser(SignupDTO signupDTO) {
        //중복확인 1 : userId
        if (userRepository.existsByUserId(signupDTO.getUserId())) {
            return DUPLICATE_ID.getMessage();
        }

        //중복확인 2 : userPhone
        if (userRepository.existsByUserPhone(signupDTO.getUserPhone())) {
            return DUPLICATE_PHONE.getMessage();
        }
        try {
            // DTO → Entity로 변환 후 저장
            User userEntity = modelMapper.map(signupDTO, User.class);
            User savedUser = userRepository.save(userEntity);

            // 저장된 userCode를 다시 DTO에 설정 (if needed for further operations, otherwise not strictly necessary here)
            signupDTO.setUserCode(savedUser.getUserCode());

            return SUCCESS_REGISTER_USER.getMessage();
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace(); // In a real app, use a logger (e.g., logger.error("Error during user registration", e);)
            // Return a generic error message
            return UNKNOWN_ERROR.getMessage(); // Or a more specific error message if you can identify the type of exception
        }
    }

    /** Read specific : Login
     * ID갖고 회원정보 조회하는 method 생성. 반환은 UserDTO로
     *
     * @param userCode
     * @return modelMapper
     * @throws IllegalAccessError */
    /**Spring-data-jpa: findById (Repository에서 제공해주는 메소드) 이용하는 방법*/
    public LoginUserDTO findByUserId(String userId) {
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw new UsernameNotFoundException("해당하는 회원이 없습니다. 회원 가입 후 로그인 해주십시오.");
        }

        // Entity → DTO 매핑
        LoginUserDTO login = modelMapper.map(foundUser, LoginUserDTO.class);

        return login;
    }

    /**
     * Update : Modify user information
     *
     * @param userDTO
     * @throws IllegalArgumentException */
    @Transactional
    public String modifyUser(UserDTO userDTO){
        // 1. 기존 유저 엔티티 조회 (예시로 userCode 또는 userId 기준으로 조회)
        User user = userRepository.findById(userDTO.getUserCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. 이전에 사용한 값과 동일한지 확인
        if (userDTO.getUserPwd() != null && user.getUserPwd().equals(userDTO.getUserPwd())) {
            return userDTO.getUserPwd() + SAME_PASSWORD.getMessage();
        }

        if (userDTO.getUserPhone() != null && user.getUserPhone().equals(userDTO.getUserPhone())) {
            return userDTO.getUserPhone() + SAME_PHONE.getMessage();
        }

        if (userDTO.getUserName() != null && user.getUserName().equals(userDTO.getUserName())) {
            return userDTO.getUserName() + SAME_NAME.getMessage();
        }

        // 3. DB 전체와 비교, 전화번호 중복 확인 (자기 자신 제외)
        if (userRepository.existsByUserPhoneAndUserCodeNot(userDTO.getUserPhone(), userDTO.getUserCode())) {
            return DUPLICATE_PHONE.getMessage();
        }

        // 4. 도메인 메서드를 통한 정보 수정
        if (userDTO.getUserPwd() != null) user.modifyUserPassword(userDTO.getUserPwd());
        if (userDTO.getUserPhone() != null) user.modifyUserPhone(userDTO.getUserPhone());
        if (userDTO.getUserName() != null) user.modifyUserName(userDTO.getUserName());

        return SUCCESS_MODIFY_USER.getMessage();
    }


    /** DELETE
     -> deleteById() 말고
     실제론 Update가 사용되어야 함
     isLeave = true 형태로
     @param userDTO
     */
    @Transactional
    public void deleteUser(UserDTO userDTO) {
        User user = userRepository.findByUserId(userDTO.getUserId());
        if (user.isLeave()) {
            throw new NoSuchElementException("이미 탈퇴한 회원입니다");
        }
        user.modifyUserLeave(true);
    }
}
