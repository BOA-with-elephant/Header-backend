package com.header.header.auth.model.service;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.AuthUserDTO;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.auth.model.repository.AuthUserRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import com.header.header.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

import static com.header.header.auth.common.ApiResponse.*;

@Service
@RequiredArgsConstructor
// final 붙은 필드만 골라서 생성자를 자동 생성해줌 -> 의존성을 안정적으로 주입하고 코드를 간결하게 유지해줌
public class AuthUserService implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        LoginUserDTO login = userService.findByUserId(userId);

        if(Objects.isNull(login)){
            throw new UsernameNotFoundException("해당하는 회원이 없습니다");
        }

        return new AuthDetails(login);
    }
}