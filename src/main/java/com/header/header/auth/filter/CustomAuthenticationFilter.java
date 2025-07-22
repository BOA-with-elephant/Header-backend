package com.header.header.auth.filter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.model.dto.LoginUserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class CustomAuthenticationFilter
        extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    /**
     * description. 지정된 url 요청 시 해당 요청을 가로채서 검증 로직을 수행하는 메소드
     * getAuthRequest에서 반환된 임시토큰을 받아서 검증 로직을 수행하는 메소드
     *
     * @param request  : HttpServletRequest
     * @param response : HttpServletResponse
     * @return Authentication
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        UsernamePasswordAuthenticationToken authRequest;

        try {
            authRequest = getAuthRequest(request);
            setDetails(request, authRequest);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * description. 사용자의 로그인 요청 시 "요청 정보를 임시 토큰에 저장"하는 (로직에 대한) 메소드
     *
     * @param request : HttpServletRequest
     * @return UsernamePasswordAuthenticationToken
     * @throws IOException
     */
    private UsernamePasswordAuthenticationToken getAuthRequest(HttpServletRequest request) throws IOException {
        //request(요청)는 json 형식으로 반환되기에 json이 갖고 있는 ObjectMapper라는 것을 통해
        // json 형식으로 변환
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        LoginUserDTO user = objectMapper.readValue(request.getInputStream(), LoginUserDTO.class);

        return new UsernamePasswordAuthenticationToken(user.getUserId(), user.getUserPwd());
    }
}