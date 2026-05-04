package com.example.tbmini.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审核操作请求DTO
 * 用于接收管理员审核（通过/删除）内容的请求参数
 */
@Data
public class ModerationRequest {
    @NotBlank
    private String targetType;

    @NotNull
    private Long targetId;

    private String reason;
}