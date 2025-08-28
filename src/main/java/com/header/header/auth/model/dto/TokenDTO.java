package com.header.header.auth.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TokenDTO {

    private String grantType;			// 토큰 타입
    private String userId; 			// 인증받은 회원 이름
    private List<Integer> shopCode;       // 회원이 갖고 있는 샵의 샵 코드  #샵을 여러개 갖고있는 경우는 어떡하지?
    private String accessToken; 		// 액세스 토큰
    private Long accessTokenExpiresIn;	// Long 형의 만료 시간

    public TokenDTO(String grantType, String userId, List<Integer> shopCode, String accessToken, Long accessTokenExpiresIn) {
        this.grantType = grantType;
        this.userId = userId;
        this.shopCode = shopCode;
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public TokenDTO(String jwtToken) {
    }
}
