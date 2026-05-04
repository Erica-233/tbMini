package com.example.tbmini.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建帖子请求DTO
 * 用于接收用户创建帖子的请求参数
 */
@Data
public class PostCreateRequest {
    @NotBlank
    private String boardSlug;

    @NotBlank
    private String title;

    private String bodyMd;
}