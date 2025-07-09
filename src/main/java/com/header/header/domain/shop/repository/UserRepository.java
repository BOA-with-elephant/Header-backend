package com.header.header.domain.shop.repository;

import com.header.header.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// 유저 정보를 받아오기 위한 임시 처리.. 추후 머지 하면서 삭제될 예정
public interface UserRepository extends JpaRepository<User, Integer> {
}
