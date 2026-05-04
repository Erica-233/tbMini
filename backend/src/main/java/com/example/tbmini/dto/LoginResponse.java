package com.example.tbmini.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应DTO
 * 用于返回登录成功后的JWT令牌和用户信息
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;

    /**
     * 用户信息子DTO
     */
    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String username;
        private String nickname;
        private String role;
        private String avatar;
    }
}