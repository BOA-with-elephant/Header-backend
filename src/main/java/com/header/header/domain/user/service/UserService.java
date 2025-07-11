package com.header.header.domain.user.service;

import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MainUserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
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

    /**
     * 이름과 핸드폰 번호로 userCode를 조회한다.
     *
     * @param userName 유저 이름
     * @param userPhone 유저 핸드폰 번호
     * */
    public Integer findUserByNameAndPhone(String userName, String userPhone){
        User find =  userRepository.findByUserNameAndUserPhone(userName, userPhone);

        return find != null ? find.getUserCode() : null;
    }
}
