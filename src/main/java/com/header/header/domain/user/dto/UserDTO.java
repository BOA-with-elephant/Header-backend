package com.header.header.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UserDTO {

    private Integer userCode;
    private String userId;
    private String userPwd;
    private int isAdmin;
    private String userName;
    private String userPhone;
    private LocalDate birthday;
    private int isLeave;

    public UserDTO(){}

    public UserDTO(int userCode, String userId, String userPwd, int isAdmin, String userName, String userPhone, LocalDate birthday, int isLeave) {
        this.userCode = userCode;
        this.userId = userId;
        this.userPwd = userPwd;
        this.isAdmin = isAdmin;
        this.userName = userName;
        this.userPhone = userPhone;
        this.birthday = birthday;
        this.isLeave = isLeave;
    }
}
