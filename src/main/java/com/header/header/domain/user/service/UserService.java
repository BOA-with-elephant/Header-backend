package com.header.header.domain.user.service;

import com.header.header.auth.common.ApiResponse;
import com.header.header.auth.exception.DuplicatedPhoneException;
import com.header.header.auth.exception.SameNameException;
import com.header.header.auth.exception.SamePhoneException;
import com.header.header.auth.exception.SamePwdException;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;

import static com.header.header.auth.common.ApiResponse.*;
import static com.header.header.auth.common.ApiResponse.SUCCESS_MODIFY_USER;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MainUserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final HttpSession httpSession;
    private final JavaMailSenderImpl mailSender;
    private int authNumber;

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
        //String을 UserDTO로 자료형 바꿔야하나? 0826 14:43
        // 1. 기존 유저 엔티티 조회
        User user = userRepository.findById(userDTO.getUserCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        // 2. 이전에 사용한 값과 동일한지 확인
        if (userDTO.getUserName() != null && user.getUserName().equals(userDTO.getUserName())) {
            log.info("[UserService] 이전과 같은 이름을 입력함");
            throw new SameNameException(userDTO.getUserName() + SAME_NAME.getMessage());
        }

        if (userDTO.getUserPhone() != null && user.getUserPhone().equals(userDTO.getUserPhone())) {
            log.info("[UserService] 이전과 같은 전화번호를 입력함");
            throw new SamePhoneException(userDTO.getUserPhone() + SAME_PHONE.getMessage());
        }

        if (userDTO.getUserPwd() != null && passwordEncoder.matches(userDTO.getUserPwd(), user.getUserPwd())) {
            log.info("[UserService] 이전과 같은 비밀번호를 입력함");
            throw new SamePwdException(userDTO.getUserPwd() + SAME_PASSWORD.getMessage());
        }

        // 3. DB 전체와 비교, 전화번호 중복 확인 (자기 자신 제외)
        if (userRepository.existsByUserPhoneAndUserCodeNot(userDTO.getUserPhone(), userDTO.getUserCode())) {
            throw new DuplicatedPhoneException(ApiResponse.DUPLICATE_PHONE.getMessage());
        }

        // 4. 도메인 메서드를 통한 정보 수정
        if (isNotEmpty(userDTO.getUserPwd())) {
            String encodedPwd = passwordEncoder.encode(userDTO.getUserPwd());
            user.modifyUserPassword(encodedPwd);
        }
        if (isNotEmpty(userDTO.getUserPhone())) user.modifyUserPhone(userDTO.getUserPhone());
        if (isNotEmpty(userDTO.getUserName())) user.modifyUserName(userDTO.getUserName());

        userRepository.save(user); //Put .save explicitly

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

    @Transactional
    public void SignupCodeGenerator() {
        Random rn = new Random();
        String randomNumber = "";
        for (int i = 0; i < 6; i++){
            randomNumber +=Integer.toString(rn.nextInt(10));
        }
        this.authNumber = Integer.parseInt(randomNumber);
    }

    public MimeMessage CreateMail(String mail){
        SignupCodeGenerator();
        MimeMessage message = mailSender.createMimeMessage();
        try
        {
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setFrom(mail);
            message.setSubject("이메일 인증"); // 이메일 제목
            String body = "";
            body += "<h3>Header CRM 가입 요청 </h3>";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + authNumber + "</h1>";
            body += "<h3>  감사합니다. </h3>";
            message.setText(body, "UTF-8", "html"); // 이메일 본문
            log.info("sent email: {}", "@90Company");
        }
        catch (MessagingException e){
            log.error("[EmailService.send()] error {}", e.getMessage());
        }

        return message;
    }
}
