package com.eazybytes.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.eazybytes.filter.CsrfCookieFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class ProjectSecurityConfig {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        //CsrfTokenRequestHandler를 구현하기 위해
        //csrf 토큰이 요청 속성으로써 활성화 될 수 있도록 도와주고 헤더로든 변수로든 토큰값을 전달하기 위해 사용
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // 전에 브라우저를 통해 api에 접속하려 할 때 백엔드 rest api들과 직접적으로 브라우저를 통해 접속하고
            // 거기에 spring security로 만들어진 로그인에 자격증명을 입력하고 jsessionid가 생성됨.
            // 같은 jsessionid로 모든 후속 요청들을 인증 정보 입력 없이 접근 가능.
            // 이제부터는 분리된 ui app을 사용 -> 로그인해서 rest api에 접근하려면
            // spring security에 내가 만든 sessionManagement 정책에 따라 jsessionid를 생성해주어야 함.
            // -> 첫 로그인이 완료되면 항상 jsessionid를 만들어달라고 하는 것.
            // 동일한 jsessionid가 ui app에 보내지고 ui app은 첫 로그인 후에 만들어지는 JSESSIONID를 사용해 후속 요청을 처리할 수 있게 됨.
            // 아래 두 줄이 없다면 매번 보안된 api 접근할 때마다 로그인 해야함.
            .securityContext((context) -> context
                // 내가 securityContextHolder 내부에 있는 인증 정보들을 저장하는 역할을 하지 않겠다.
                // 프레임워크들이 대신 수행하게 해라. -> 보안 컨텍스트가 자동으로 저장되도록 설정
                .requireExplicitSave(false))
            // sessionManagement 설정에서 항상 새로운 세션을 생성하도록 설정
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

            //cors
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();

                // CORS 설정 부분
                // 허용할 출처(origin)를 설정. 여기서는 "http://localhost:4200"만 허용.
                config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

                // 허용할 HTTP 메서드를 설정. 여기서는 모든 메서드를 허용.
                config.setAllowedMethods(Collections.singletonList("*"));

                // 자격증명(credentials)을 허용할지 여부를 설정. 여기서는 자격증명을 허용.
                config.setAllowCredentials(true);

                // 허용할 헤더를 설정. 여기서는 모든 헤더를 허용.
                config.setAllowedHeaders(Collections.singletonList("*"));

                // Pre-flight 요청의 캐시 시간을 설정. 여기서는 3600초(1시간)로 설정.
                config.setMaxAge(3600L);

                return config;
            }))
            // CSRF 보호 기능을 비활성화.------상당히 위험하다
            //.csrf(csrf -> csrf.disable())

            //공공 api중 get api는 csrf 기능 자동으로 비활성화. contact랑 register는 post라 추가해줌
            //.csrf(csrf -> csrf.ignoringRequestMatchers( "/contact", "/register"))

            .csrf((csrf) -> csrf.csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/contact", "/register")
                // CookieCsrfTokenRepository는 csrf 토큰을 쿠키로 유지하는 역할
                // withHttpOnlyFalse는 (react/ angular 등등)앱 안의 js 코드가 쿠키를 읽을 수 있게 해줌.
                // 생성된 쿠키가 HttpOnly 플래그를 갖지 않도록 설정함으로써, 클라이언트 측의 JavaScript 코드가 해당 쿠키를 읽을 수 있게 한다는 의미
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))

            // addFilter에 우리가 만든 CsrfCookieFilter 메서드를 전달. BasicAuthenticationFilter는 http base authentication을 사용할떄 유용
            // addFilterAfter는 BasicAuthenticationFilter를 실행한 후 CsrfCookieFilter를 실행하라는 것.
            // BasicAuthenticationFilter 이후에 로그인 동작이 완료될 수 있음. 로그인이 되어야 csrf 토큰이 생성됨. 동일한 csrf 토큰을 CsrfCookieFilter를 통해 응답에 넣고싶음.
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

            // 요청에 대한 권한을 설정.
            .authorizeHttpRequests(auth -> auth
                // 인증이 필요한 엔드포인트를 설정.
                .requestMatchers("/myAccount", "/myBalance", "/myLoans", "/myCards", "/user")
                .authenticated()
                // 모든 사용자가 접근할 수 있는 엔드포인트를 설정.
                .requestMatchers("/notices", "/contact", "/register").permitAll())

            // 기본 폼 로그인 설정을 사용.
            .formLogin(withDefaults())

            // 기본 HTTP 기본 인증 설정을 사용.
            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
