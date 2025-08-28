package com.header.header.auth.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@ToString
public class SignupDTO {
    private Integer userCode;
    private String userName;
    private String userPhone; //필수 입력 값
    private String userEmail; // 이메일 인증 시 필요
    private String userId;//필수 입력 값
    private String userPwd;
    private LocalDate birthday;
    private String verifyCode;
}