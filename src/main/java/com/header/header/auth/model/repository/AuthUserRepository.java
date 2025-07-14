package com.header.header.auth.model.repository;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<User, Integer> {
    boolean existsByUserId(String userId);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserPhoneAndUserCodeNot(String userPhone, int userCode);

    /* 고객 아이디를 통해 고객 정보 불러오기 - 정아 */
    LoginUserDTO findByUserId(String userId);
}