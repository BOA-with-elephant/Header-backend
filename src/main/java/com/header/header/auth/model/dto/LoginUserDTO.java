package com.header.header.auth.model.dto;

import com.header.header.auth.common.UserRole;
import com.header.header.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class LoginUserDTO {
    private int userCode;
    private String userId;
    private String userPwd;
    private String userName;
    private UserRole userRole;

    public List<String> getRole() {
        if(this.userRole.getRole().length() > 0){
            return Arrays.asList(this.userRole.getRole().split(","));
        }
        return new ArrayList<>();
    }
}