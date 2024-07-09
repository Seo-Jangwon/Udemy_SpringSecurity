package com.eazybytes.config;

import com.eazybytes.model.Authority;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EazyBankUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pwd = authentication.getCredentials().toString();
        List<Customer> customer = customerRepository.findByEmail(username);
        if (!customer.isEmpty()) {
            if (passwordEncoder.matches(pwd, customer.get(0).getPwd())) {
//                List<GrantedAuthority> authorities = new ArrayList<>();
//                authorities.add(new SimpleGrantedAuthority(customer.getFirst().getRole()));// SimpleGrantedAuthority는 특정 endUser에게 권한, 역할을 부여하는 final class
                return new UsernamePasswordAuthenticationToken(username, pwd, getGrantedAuthorities(customer.getFirst().getAuthorities()));
            } else {
                throw new BadCredentialsException("Invalid password!");
            }
        } else {
            throw new BadCredentialsException("No user registered with this details!");
        }
    }

    /**
     * 주어진 Authority 객체들의 집합을 받아서,
     * 그에 상응하는 GrantedAuthority 객체들의 리스트를 반환하는 메서드.
     *
     * @param authorities 변환할 Authority 객체들의 집합 (Set)
     * @return GrantedAuthority 객체들의 리스트 (List)
     */
    private List<GrantedAuthority> getGrantedAuthorities(Set<Authority> authorities) {
        // GrantedAuthority 객체들을 담을 리스트를 새로 생성.
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 주어진 Authority 집합을 반복하면서 각각의 Authority 객체를 처리.
        for (Authority authority : authorities) {
            // 각 Authority 객체의 이름을 가져와서 SimpleGrantedAuthority 객체를 생성하고 리스트에 추가.
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
        }

        // 완성된 GrantedAuthority 객체들의 리스트를 반환.
        return grantedAuthorities;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
