package com.header.header.auth.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SignupDTO {
    private Integer userCode;
    private String userName; //필수 입력 값
    private String userPhone; //필수 입력 값, 전화번호 인증 필요 13:27
    private String userId;
    private String userPwd;
    private String birthday;
    /*고민! 여기서 회원가입할 사람이 알아서 골라 받는 거 아니고
    shopLocation 등록하거나 등록상태(shopStatus) '영업 중'인 경우에는
    자동으로 ADMIN 되게끔 만들어야 함.
    shopLocation이 삭제되어 shopCode를 하나도 갖고 있지 않게 되거나
    '폐업'한 경우에는 ADMIN 권한 삭제되도록 만들어야 함 13:47
    17:00 해결 완료. DevelopingLog.md 250707 12번 참고 바람*/
}