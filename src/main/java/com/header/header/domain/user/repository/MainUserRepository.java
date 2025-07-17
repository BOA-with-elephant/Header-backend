package com.header.header.domain.user.repository;

import com.header.header.domain.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MainUserRepository extends JpaRepository<User, Integer> {

    /* 예약자명과 예약자 번호를 통해 userCode 가져오기 - 주혜 */
    @Query( value = "SELECT u FROM User u WHERE u.userName LIKE CONCAT('%', :userName, '%') AND u.userPhone = :userPhone")
    User findByUserNameAndUserPhone(@Param("userName") String userName, @Param("userPhone") String userPhone);

    @Query("SELECT u.userPhone " +
            "FROM User u " +
            "WHERE u.userCode = :userCode")
    String findPhoneByUserCode(Integer userCode);

    /* 고객 아이디를 통해 고객 정보 불러오기 - 정아 */
    User findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByUserPhone(String userPhone);

    boolean existsByUserPhoneAndUserCodeNot(String userPhone, int userCode);
}
