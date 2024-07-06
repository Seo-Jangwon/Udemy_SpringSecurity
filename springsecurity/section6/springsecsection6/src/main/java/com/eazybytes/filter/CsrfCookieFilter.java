package com.eazybytes.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    // 접속하는 ui 앱에 csrf 토큰을 쿠키로 전달하기 위해
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // 요청 객체에서 CsrfToken을 가져옴. CsrfToken 클래스의 이름을 속성으로 사용하여 요청에서 이 속성을 검색.
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // csrfToken이 null이 아니고, 헤더 이름이 null이 아닌 경우
        if (null != csrfToken.getHeaderName()) {
            // 응답 객체에 CSRF 토큰을 헤더로 설정.
            // 클라이언트 측 애플리케이션이 CSRF 토큰을 사용할 수 있도록 하기 위함.
            // 응답이 헤더 안에 있는 토큰 값.
            // 여기서 헤더는 보내고 쿠키는 보내지 않았다. -> security는 csrf 토큰 값을 채우면 브라우저나 ui 앱에 보내는 응답까지 신경써줌.
            response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }

        // 필터 체인의 다음 필터로 요청과 응답을 전달.
        // 현재 필터가 요청과 응답을 처리한 후에도 남은 필터들이 계속해서 처리할 수 있도록 함.
        filterChain.doFilter(request, response);
    }
}