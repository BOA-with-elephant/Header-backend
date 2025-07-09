package com.header.header.domain.shop.repository;

import com.header.header.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 임시 처리, 주혜님 거랑 합칠 때 삭제 예상
public interface UserRepository extends JpaRepository<User, Integer> {
}
