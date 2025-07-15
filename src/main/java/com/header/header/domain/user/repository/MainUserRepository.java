package com.header.header.domain.user.repository;

import com.header.header.domain.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MainUserRepository extends JpaRepository<User, Integer> {

    /* 예약자명과 예약자 번호를 통해 userCode 가져오기 - 주혜 */
    @Query( value = "SELECT u FROM User u WHERE u.userName LIKE CONCAT('%', :userName, '%') AND u.userPhone = :userPhone")
    User findByUserNameAndUserPhone(@Param("userName") String userName, @Param("userPhone") String userPhone);

    @Query("SELECT u.userPhone " +
            "FROM User u " +
            "WHERE u.userCode == :userCode ")
    String findPhoneByUserCode(Integer userCode);
}
