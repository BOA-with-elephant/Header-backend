package com.header.header.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name="tbl_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userCode;
    private String userId;
    private String userPwd;
    private boolean isAdmin;
    private String userName;
    private String userPhone;
    private Date birthday;
    private boolean isLeave;

    public void modifyUserLeave(boolean isLeave){
        this.isLeave = isLeave;
    }

    public void modifyUserPassword(String newPwd) {
        this.userPwd = newPwd;
    }

    public void modifyUserPhone(String newPhone) {
        this.userPhone = newPhone;
    }

    public void modifyUserName(String newName) {
        this.userName = newName;
    }
}