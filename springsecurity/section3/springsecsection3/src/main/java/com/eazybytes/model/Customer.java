package com.eazybytes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Customer {

    @Id
/**
 * JPA 엔티티의 기본 키 생성을 정의
 *
 * @GeneratedValue: 기본 키 값 생성을 정의
 *  - strategy = GenerationType.AUTO: JPA가 기본 키 생성 전략을 자동으로 선택하게 함
 *  - generator = "native": @GenericGenerator에서 정의한 "native" 생성기를 사용함
 *
 * @GenericGenerator: Hibernate에서 제공하는 고유 생성기를 정의
 *  - name = "native": 생성기의 이름을 "native"로 지정
 *  - strategy = "native": 데이터베이스의 네이티브 ID 생성 전략을 사용. 데이터베이스가 자체적으로 제공하는 기본 ID 생성 방식
 */
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private int id;
    private String email;
    private String pwd;
    private String role;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
