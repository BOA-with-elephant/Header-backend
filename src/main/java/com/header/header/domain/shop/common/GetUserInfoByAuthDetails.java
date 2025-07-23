package com.header.header.domain.shop.common;

import com.header.header.auth.model.AuthDetails;
import com.header.header.domain.user.projection.UserCode;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetUserInfoByAuthDetails {

    private final MainUserRepository mainUserRepository;

    /*유저 이름으로 코드를 가져오는 로직의 메소드, 샵 도메인과 유저 예약 도메인에서 재사용*/
    public Integer getUserCodeByAuthDetails(AuthDetails authDetails) {

        Optional<UserCode> userCodeProjection = mainUserRepository.findUserCodeByUserId(authDetails.getUsername());

        return userCodeProjection.get().getUserCode();
    }
}
