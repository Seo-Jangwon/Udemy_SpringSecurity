package com.eazybytes.config;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//커스텀 AuthenticationProvider
@Component
public class EazyBankUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        // 1. 저장 시스템에서 유저 세부정보 로딩, 2. 비밀번호 비교
        // 모든 authentication 로직을 정확하게 정의해야 함.
        // 이후 인증 성공 여부가 담긴 authentication 객체를 생성


        /*
        * 여기선 UserDetailsService나 UserDetailsManager나 그것의 구현 클래스를 사용 안함.
        * 유저 상세정보 조회 로직도 여기 있기 때문
        * */

        // 1. 전달된 인증 정보에서 사용자 이름을 추출함.
        String username = authentication.getName();

        // 2. 전달된 인증 정보에서 비밀번호를 추출함.
        String pwd = authentication.getCredentials().toString();

        // 3. 저장 시스템(예: 데이터베이스)에서 사용자 이름(이메일)으로 사용자를 검색함.
        List<Customer> customer = customerRepository.findByEmail(username);

        // 4. 만약 사용자가 존재한다면,
        if (customer.size() > 0) {
            // 5. 저장된 비밀번호와 입력된 비밀번호를 비교하여 일치하는지 확인함.
            if (passwordEncoder.matches(pwd, customer.get(0).getPwd())) {
                // 6. 사용자에게 부여된 권한 목록을 생성함.
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(customer.get(0).getRole()));

                // 7. 인증에 성공한 사용자의 인증 객체를 생성하여 반환함.
                return new UsernamePasswordAuthenticationToken(username, pwd, authorities);
            } else {
                // 8. 비밀번호가 일치하지 않는 경우, 예외를 발생시킴.
                throw new BadCredentialsException("Invalid password!");
            }
        } else {
            // 9. 사용자가 존재하지 않는 경우, 예외를 발생시킴.
            throw new BadCredentialsException("No user registered with this details!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(
            authentication));// AbstractUserDetailsAuthenticationProvider의 support에서 복사
    }
}
