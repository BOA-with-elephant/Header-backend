package com.header.header.auth.model.dto;

public class TokenDTO {

    private String grantType;			// 토큰 타입
    private String userId; 			// 인증받은 회원 이름
    private String accessToken; 		// 액세스 토큰
    private Long accessTokenExpiresIn;	// Long 형의 만료 시간

    public TokenDTO(String grantType, String userId, String accessToken, Long accessTokenExpiresIn) {
        this.grantType = grantType;
        this.userId = userId;
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public TokenDTO(String jwtToken) {
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public void setAccessTokenExpiresIn(Long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    @Override
    public String toString() {
        return "TokenDTO{" +
                "grantType='" + grantType + '\'' +
                ", userId='" + userId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", accessTokenExpiresIn=" + accessTokenExpiresIn +
                '}';
    }
}
