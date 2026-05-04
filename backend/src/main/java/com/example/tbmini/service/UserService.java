package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.dto.UpdateProfileRequest;
import com.example.tbmini.dto.UserProfileResponse;
import com.example.tbmini.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public UserProfileResponse getProfileByUsername(String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toProfile(user);
    }

    public UserProfileResponse getProfileById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toProfile(user);
    }

    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (!user.getNickname().equals(req.getNickname())) {
            User exist = userMapper.selectOne(new QueryWrapper<User>().eq("nickname", req.getNickname()));
            if (exist != null && !exist.getId().equals(userId)) {
                throw new BizException("昵称已被占用");
            }
        }
        user.setNickname(req.getNickname());
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        userMapper.updateById(user);
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                null,
                user.getCreatedAt()
        );
    }
}
