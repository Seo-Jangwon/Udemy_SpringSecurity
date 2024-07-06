package com.eazybytes.config;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EazyBankUserDetails implements UserDetailsService {

    // CustomerRepository를 자동으로 주입받아 사용
    @Autowired
    private CustomerRepository customerRepository;

    // 주어진 사용자 이름(이메일)을 기반으로 사용자 세부 정보를 로드하는 메소드
    /*
     * 1. 사용자가 사용자 이름(이메일)과 비밀번호를 입력하여 로그인 시도
     *
     * 2. Spring Security가 loadUserByUsername 호출,
     * EazyBankUserDetails 클래스의 loadUserByUsername 메소드를 호출하여 사용자 세부 정보를 로드.
     * 이 메소드는 데이터베이스에서 사용자 정보를 가져와 UserDetails 객체를 반환.
     *
     *3. UserDetails 객체에는 사용자의 비밀번호가 포함.
     * Spring Security는 사용자가 입력한 비밀번호를 PasswordEncoder를 사용하여
     *
     * (
    ProjectSecurityConfig의
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    * ) 인코딩하고, 데이터베이스에서 가져온 인코딩된 비밀번호와 비교.
     * 비밀번호 비교는 DaoAuthenticationProvider의 additionalAuthenticationChecks() 함수에서 일어남.
     *  loadUserByUsername 메소드는 사용자 이름과 비밀번호, 권한 정보를 제공하는 역할.
     *
     *
     * */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userName, password;  // 사용자 이름과 비밀번호를 저장할 변수
        List<GrantedAuthority> authorities;  // 사용자의 권한을 저장할 리스트

        // CustomerRepository를 사용하여 이메일로 고객을 찾음
        List<Customer> customer = customerRepository.findByEmail(username);

        // 만약 고객 리스트가 비어있다면, UsernameNotFoundException을 던짐
        if (customer.size() == 0) {
            // 예외를 던져 사용자 세부 정보를 찾을 수 없음을 알림
            throw new UsernameNotFoundException(
                "User details not found for the user : " + username);
        } else {
            // 고객 리스트에서 첫 번째 고객의 이메일과 비밀번호를 가져옴
            userName = customer.get(0).getEmail();  // 첫 번째 고객의 이메일을 userName 변수에 저장
            password = customer.get(0).getPwd();  // 첫 번째 고객의 비밀번호를 password 변수에 저장

            // 고객의 역할(role)을 권한(authority) 리스트에 추가함
            authorities = new ArrayList<>();  // 권한 리스트 초기화
            authorities.add(new SimpleGrantedAuthority(
                customer.get(0).getRole()));  // 첫 번째 고객의 역할(role)을 권한 리스트에 추가
        }

        // UserDetails 객체를 생성하여 반환함
        // UserDetails 인터페이스를 구현한 User 객체를 생성하고, 사용자 이름, 비밀번호, 권한 리스트를 전달
        return new User(userName, password, authorities);
    }
}
