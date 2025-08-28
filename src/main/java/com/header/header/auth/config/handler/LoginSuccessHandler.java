package com.header.header.auth.config.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.config.JwtTokenProvider;
import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 로그인 성공 시 JWT 토큰을 생성하여 응답에 포함시키는 핸들러.
 * 사용자 정보를 응답 본문에 포함하며, 특정 사용자 상태에 따른 메시지를 제공한다.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 관리를 위한 프로바이더
    private final ObjectMapper objectMapper; // JSON 직렬화를 위한 ObjectMapper
    private final ShopService shopService;

    /**
     * LoginSuccessHandler의 생성자입니다.
     * @param jwtTokenProvider JWT 토큰 프로바이더
     * @param objectMapper JSON 객체 매퍼
     * @param shopService 샵 관련 서비스 클래스
     */
    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper, ShopService shopService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.shopService = shopService;
    }

    /**
     * 인증 성공 시 호출되는 메서드입니다.
     * JWT 토큰을 생성하고, 이를 응답 본문에 JSON 형태로 포함하여 클라이언트에 전송합니다.
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 객체
     * @throws IOException 입출력 예외
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 응답 본문에 담을 데이터를 위한 Map 준비
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "로그인 성공입니다."); // 기본 성공 메시지 설정

        // 1. 인증된 사용자 정보 가져오기 (핵심!)
        LoginUserDTO loginUserDTO = null;
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        loginUserDTO = authDetails.getLoginUserDTO();

        // 2. JWT 토큰 생성 및 사용자 정보 포함 로직
        if (loginUserDTO != null) {
            List<ShopDTO> shopDTOs = shopService.findAllShopsByAdminCode(loginUserDTO.getUserCode());

            // shopDTOs 리스트에서 shopCode만 추출하여 새로운 리스트를 만듭니다.
            List<Integer> shopCodes = new ArrayList<>();
            if (shopDTOs != null && !shopDTOs.isEmpty()) {
                for (ShopDTO shopDTO : shopDTOs) {
                    shopCodes.add(shopDTO.getShopCode());
                }
            }

            // TokenDTO 생성 시 List<ShopDTO>를 전달
            TokenDTO tokenDto = jwtTokenProvider.generateTokenDTO(loginUserDTO, shopDTOs);

            String accessToken = tokenDto.getAccessToken();

            // 응답 본문에 토큰 및 사용자 정보 추가
            responseBody.put("token", accessToken);
            responseBody.put("userInfo", loginUserDTO);

            // 여러 개의 shopCode를 응답 본문에 추가
            responseBody.put("shopCodes", shopCodes);

            response.setStatus(HttpServletResponse.SC_OK);

        } else {
            responseBody.put("message", "사용자 정보를 가져오는데 실패했습니다.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // 3. 공통 응답 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 4. 응답 본문에 JSON 데이터 작성
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
