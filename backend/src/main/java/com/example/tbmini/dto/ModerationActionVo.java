package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理操作记录视图对象DTO
 * 用于返回管理员操作历史记录
 */
@Data
public class ModerationActionVo {
    private Long id;
    private Long moderatorId;
    private String moderatorNickname;
    private String targetType;
    private Long targetId;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
}