package com.example.tbmini.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理操作记录实体
 * 对应数据库中的moderation_actions表
 */
@Data
@TableName("moderation_actions")
public class ModerationAction {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long moderatorId;

    private String targetType;

    private Long targetId;

    private String action;

    private String reason;

    private LocalDateTime createdAt;
}