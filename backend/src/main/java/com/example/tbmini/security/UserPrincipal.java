package com.example.tbmini.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 用户主体信息
 * 扩展Spring Security的User类，添加userId字段
 */
@Getter
public class UserPrincipal extends User {
    private final Long userId;

    public UserPrincipal(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
}