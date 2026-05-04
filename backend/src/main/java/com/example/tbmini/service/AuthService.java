package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.Role;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.dto.LoginRequest;
import com.example.tbmini.dto.LoginResponse;
import com.example.tbmini.dto.RegisterRequest;
import com.example.tbmini.dto.UserResponse;
import com.example.tbmini.mapper.UserMapper;
import com.example.tbmini.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest req) {
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("email", req.getEmail()));
        if (exist != null) {
            throw new BizException("邮箱已注册");
        }
        User usernameExist = userMapper.selectOne(new QueryWrapper<User>().eq("username", req.getUsername()));
        if (usernameExist != null) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setRole(Role.USER.name());
        user.setIsBanned(false);
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", principal.getUsername()));
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(token,
                new LoginResponse.UserInfo(user.getId(), user.getEmail(), user.getUsername(), user.getNickname(), user.getRole(), user.getAvatar()));
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getNickname(), user.getRole(), user.getAvatar());
    }
}
