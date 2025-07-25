package com.header.header.auth.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;


@Getter
@Setter
@ToString
public class LoginUserDTO {
    private Integer userCode;
    private String userId;
    private String userPwd;
    private String userName;
    private String userPhone;
    private boolean isAdmin;
}