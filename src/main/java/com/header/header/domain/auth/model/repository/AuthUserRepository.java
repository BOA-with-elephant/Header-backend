package com.header.header.domain.auth.model.repository;

import com.header.header.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<User, Integer> {
    boolean existsByUserId(String userId);

    boolean existsByUserPhone(String userPhone);
}