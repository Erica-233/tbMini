package com.example.tbmini.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建评论请求DTO
 * 用于接收用户创建评论的请求参数
 */
@Data
public class CommentRequest {
    @NotNull
    private Long postId;

    private Long parentId;

    @NotBlank
    private String bodyMd;
}