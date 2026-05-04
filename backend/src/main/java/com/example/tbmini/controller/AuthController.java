package com.example.tbmini.controller;

import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.LoginRequest;
import com.example.tbmini.dto.LoginResponse;
import com.example.tbmini.dto.RegisterRequest;
import com.example.tbmini.dto.UserResponse;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户注册、登录和获取当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 用户注册
     * @param req 注册请求信息（包含用户名、密码等）
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.ok();
    }

    /**
     * 用户登录
     * @param req 登录请求信息（包含用户名、密码）
     * @return 登录响应（包含JWT令牌和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    /**
     * 获取当前登录用户信息
     * @param principal 当前认证的用户主体
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(authService.getCurrentUser(principal.getUserId()));
    }
}