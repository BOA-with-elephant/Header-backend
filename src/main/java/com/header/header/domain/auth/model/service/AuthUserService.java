package com.header.header.domain.auth.model.service;

import com.header.header.domain.auth.model.dto.AuthUserDTO;
import com.header.header.domain.auth.model.dto.LoginUserDTO;
import com.header.header.domain.auth.model.dto.SignupDTO;
import com.header.header.domain.auth.model.repository.AuthUserRepository;
import com.header.header.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
// final 붙은 필드만 골라서 생성자를 자동 생성해줌 -> 의존성을 안정적으로 주입하고 코드를 간결하게 유지해줌
public class AuthUserService {
    private final AuthUserRepository authUserRepository;
    private final ModelMapper modelMapper;

    /*save : registerNewUser
    -> SignupDTO 사용
    @param signupDTO 생성할 user 정보가 담긴 DTO
    @return 생성된 signupDTO(user관련 DTO)
    @throws IllegalArgumentException 이미 존재하는 아이디나 전화번호일 때 */
    @Transactional
    public SignupDTO registerNewUser(SignupDTO signupDTO){
        //중복확인 1 : userId
        if (authUserRepository.existsByUserId(signupDTO.getUserId())) {
            throw new IllegalArgumentException(
                    String.format("이미 존재하는 아이디입니다.", signupDTO.getUserId()));
        }
        //중복확인 2 : userPhone
        if (authUserRepository.existsByUserPhone(signupDTO.getUserPhone())) {
            throw new IllegalArgumentException(
                    String.format("이미 존재하는 전화번호입니다.", signupDTO.getUserPhone()));
        }

        User newUser = authUserRepository.save(modelMapper.map(signupDTO, User.class));

        // 생성된 userCode를 DTO에 다시 설정
        signupDTO.setUserCode(newUser.getUserCode());
        return signupDTO;
    }

    /*Read specific : Login
    * ID갖고 회원정보 조회하는 method 생성. 반환은 UserDTO로
    *
    * @param userCode
    * @return
    * @throws */
    /*public LoginUserDTO findByUserId(String userId) {
        LoginUserDTO login = authUserRepository.findById(userId);
        if(!Objects.isNull(login)){
            return login;
        }else {
            return modelMapper.map(login, LoginUserDTO.class);
        }
    }*/
    /*Spring-data-jpa: findById (Repository에서 제공해주는 메소드) 이용하는 방법*/
    public LoginUserDTO findUserByUserId(int userCode) {
        User foundUser = authUserRepository.findById(userCode)
                .orElseThrow(() -> new IllegalAccessError("해당하는 회원이 없습니다. 회원가입 후 로그인 해주십시오."));
        return modelMapper.map(foundUser, LoginUserDTO.class);
    }

    /*Update : Modify user information
    *
    * @param authUserDTO
    * @throws IllegalArgumentException */
    /*수정(엔티티 객체의 필드 값 변경) -> 일단 수정할 menuCode를 findById로 받아온다. */
    @Transactional
    public void modifyUser(AuthUserDTO authUserDTO) {
        // 1. 기존 유저 엔티티 조회 (예시로 userCode 또는 userId 기준으로 조회)
        User user = authUserRepository.findById(authUserDTO.getUserCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. 이전 값과 동일한지 확인
        if (user.getUserPwd().equals(authUserDTO.getUserPwd())) {
            throw new IllegalArgumentException("이전 비밀번호와 동일합니다.");
        }

        if (user.getUserPhone().equals(authUserDTO.getUserPhone())) {
            throw new IllegalArgumentException("이전 전화번호와 동일합니다.");
        }

        if (user.getUserName().equals(authUserDTO.getUserName())) {
            throw new IllegalArgumentException("이전 이름과 동일합니다.");
        }

        // 3. 전화번호 중복 확인
        if (authUserRepository.existsByUserPhone(authUserDTO.getUserPhone())) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }

        // 4. 도메인 메서드를 통한 정보 수정
        user.modifyUserPassword(authUserDTO.getUserPwd());
        user.modifyUserPhone(authUserDTO.getUserPhone());
        user.modifyUserName(authUserDTO.getUserName());
    }


    /*DELETE
     -> deleteById() 말고
     실제론 Update가 사용되어야 함
     isLeave = true 형태로
     @param autuUserDTO
     */
    @Transactional
    public void deleteUser(AuthUserDTO authUserDTO) {
        User user = authUserRepository.findById(authUserDTO.getUserCode())
                .orElseThrow(() -> new NoSuchElementException("이미 탈퇴한 회원입니다"));
        user.modifyUserLeave(true);
    }
}