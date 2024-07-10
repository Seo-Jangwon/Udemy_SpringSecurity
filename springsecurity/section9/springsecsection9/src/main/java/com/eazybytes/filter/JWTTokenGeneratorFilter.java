package com.eazybytes.filter;


import com.eazybytes.constants.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 이 필터가 호출될 때 엔드유저의 인증이 성공적일 것임.
        // SecurityContextHolder.getContext().getAuthentication()을 이용하여 현재 인증된 유저의 정보를 가져옴.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 null이 아닐 경우 JWT 토큰을 생성.
        if (null != authentication) {
            // JWT 키를 생성.
            SecretKey key = Keys.hmacShaKeyFor(
                SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));

            // JWT 토큰을 생성.
            String jwt = Jwts.builder()
                .issuer("Eazy Bank") // 이 토큰을 발행하는 개인 혹은 조직을 설정
                .subject("JWT Token") // 토큰의 주제를 설정.
                .claim("username", authentication.getName()) // 로그인된 유저의 이름을 JWT에 추가.
                .claim("authorities",
                    populateAuthorities(authentication.getAuthorities())) // 로그인된 유저의 권한을 JWT에 추가.
                // 비밀번호는 절대로 JWT에 포함하지 않음.
                .issuedAt(new Date()) // 토큰 발행 시간을 설정.
                .expiration(new Date((new Date()).getTime() + 30000)) // 토큰 만료 시간을 설정.
                .signWith(key) // 토큰에 서명.
                .compact(); // 토큰을 문자열로 직렬화.

            // 생성된 JWT 토큰을 응답 헤더에 설정.
            response.setHeader(SecurityConstants.JWT_HEADER, jwt);
        }

        // 다음 필터를 호출.
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 이 조건에 따라 필터를 실행하지 않음.
        // JWT 필터는 로그인 중에만 실행되어야 함.
        // 이 메서드가 true를 반환하면 해당 요청에 대해 필터가 적용되지 않으며, false를 반환하면 필터가 적용
        // 로그인 작업 중 호출할 동작이 "/user"이기 때문에 이 경로에서만 필터가 실행되도록 함.
        return !request.getServletPath().equals("/user");
    }

    private String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        // 유저의 권한을 콤마로 구분된 문자열로 변환합니다.
        Set<String> authoritiesSet = new HashSet<>();
        for (GrantedAuthority authority : collection) {
            authoritiesSet.add(authority.getAuthority());
        }
        return String.join(",", authoritiesSet);
    }

}