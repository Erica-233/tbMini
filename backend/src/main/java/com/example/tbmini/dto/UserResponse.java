package com.example.tbmini.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户信息响应DTO
 * 用于返回用户的基本信息
 */
@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
}