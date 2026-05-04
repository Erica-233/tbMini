package com.example.tbmini.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String password;

    private String username;

    private String nickname;

    private String role;

    private String avatar;

    private Boolean isBanned;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
