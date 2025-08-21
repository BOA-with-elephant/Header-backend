package com.header.header.domain.user.service;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Read specific : findId
     * ID갖고 회원정보 조회하는 method 생성. 반환은 UserDTO로
     *
     * @param userCode
     * @return modelMapper
     * @throws IllegalAccessError */
    /**Spring-data-jpa: findById (Repository에서 제공해주는 메소드) 이용하는 방법*/
    public LoginUserDTO findByUserId(String userId) {
        //이 메소드는 단순히 사용자 ID를 기반으로 데이터베이스에서 사용자 정보를 조회하는 역할만 한다. Id 찾기에서 사용할 것
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw new UsernameNotFoundException("해당하는 회원이 없습니다. 회원 가입을 먼저 진행해주십시오.");
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
        // 1. 기존 유저 엔티티 조회
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

    /**
     * RESET PWD
     *
     * @return
     */
    @Transactional
    public UserDTO resetPwd(UserDTO userDTO) {
        // 1. 필수 입력값 누락 여부 체크
        if (userDTO.getUserId() == null || userDTO.getUserPhone() == null || userDTO.getUserName() == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }

        // 2. DB에서 유저 정보 조회
        User user = userRepository.findByUserId(userDTO.getUserId());

        // 3. 유저가 존재하지 않거나 정보가 일치하지 않으면 예외 발생
        if (user == null || !user.getUserName().equals(userDTO.getUserName()) || !user.getUserPhone().equals(userDTO.getUserPhone())) {
            throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
        }

        // 4. 본인 확인 성공 시 다음 단계 로직 호출 (예: 이메일 전송)
        // 이 부분에 ⭐이메일 전송 로직이 추가되어야 함.
        // sendEmailForPasswordReset(user.getUserEmail()); user에는 email이 없기 때문에 이거 그대로 쓰면 안 되고 redis에 저장한 이메일 써야함
        return userDTO;
    }
}
