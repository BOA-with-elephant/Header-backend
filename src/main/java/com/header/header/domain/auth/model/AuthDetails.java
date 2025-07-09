package com.header.header.domain.auth.model;

import com.header.header.domain.auth.model.dto.LoginUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthDetails implements UserDetails {
    // 사용자이름으로 조회하는 결과를 LoginUserDTO가 받아가게끔 DTO 필드 생성.
    private LoginUserDTO loginUserDTO;

    public AuthDetails() {}

    public AuthDetails(LoginUserDTO login) {
        this.loginUserDTO = login;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return loginUserDTO.getUserPwd(); // 실제 비밀번호
    }

    @Override
    public String getUsername() {
        return loginUserDTO.getUserId();
        //userId를 갖고 와서 해당하는 일치하는 userName을 출력한다.  15:04
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}