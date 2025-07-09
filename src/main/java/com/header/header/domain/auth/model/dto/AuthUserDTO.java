package com.header.header.domain.auth.model.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
//Lombok어노테이션(@Getter, @Setter, @ToString)으로 코드의 중복과 수작업을 줄였다. 12:22
public class AuthUserDTO {
    /*login, register, response 용 DTO.
    * gpt 조언에 따라 DB테이블 중복 관리 문제가 생길 수 있으니
    * User 엔티티는 그대로 두고, 인증 로직은 user/entity/User.java를 받아온
    * DTO나 Wrapper로 간접 접근하는 방식을 채택했다.  250707 12:00 */
    //DevelopingLog 참조 12:18
    private int userCode;
    private String userId;
    private String userPwd;
    private boolean isAdmin;
    private String userName;
    private String userPhone;
    private String birthday;
    private boolean isLeave;
}
