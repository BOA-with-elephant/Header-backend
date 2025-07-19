package com.header.header.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate birthday;
    private boolean isLeave;

    public User(String userId, String userPwd, boolean isAdmin, String userName, String userPhone, LocalDate birthday, boolean isLeave) {
        this.userId = userId;
        this.userPwd = userPwd;
        this.isAdmin = isAdmin;
        this.userName = userName;
        this.userPhone = userPhone;
        this.birthday = birthday;
        this.isLeave = isLeave;
    }

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