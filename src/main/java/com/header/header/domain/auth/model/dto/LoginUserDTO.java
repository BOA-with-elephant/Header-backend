package com.header.header.domain.auth.model.dto;

import com.header.header.domain.auth.common.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginUserDTO {
    private int userCode;
    private String userId;
    private String userPwd;
    private String userName;
    private boolean isAdmin;
}
