package com.header.header.domain.user.dto;

import com.header.header.auth.common.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@Getter
@Setter
@ToString
public class UserDTO {

    private int userCode;
    private String userId;
    private String userPwd;
    //private int isAdmin;
    private String userName;
    private String userPhone;
    private Date birthday;
    private int isLeave;
    private UserRole role;


    public UserDTO(){}

    public UserDTO(int userCode, String userId, String userPwd, String userName, String userPhone, Date birthday, int isLeave, UserRole role) {
        this.userCode = userCode;
        this.userId = userId;
        this.userPwd = userPwd;
        this.userName = userName;
        this.userPhone = userPhone;
        this.birthday = birthday;
        this.isLeave = isLeave;
        this.role = role;
    }
}
