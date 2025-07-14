package com.header.header.auth.common;

public enum UserRole {
    //열거타입 enum = 상수의 집합
    //관련 상수들을 집합으로 갖고 있는 것이 enum type

    // user와 사용자의 권한에 대한 값을 여기서 상수로 받아줄 것임
    USER("USER"),
    ADMIN("ADMIN");

    private String role;

    UserRole(String role) {
        // 생성자. UserRole() 앞에 public이 생략되어있음
        this.role=role;
    }

    public String getRole() {
        return role;
    } //내(?)가 갖고 있는 role 정보를 getRole로 꺼내온다

    @Override
    public String toString() {
        return "UserRole{" +
                "role='" + role + '\'' +
                '}';
    }
}
