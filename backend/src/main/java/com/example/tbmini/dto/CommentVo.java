package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图对象DTO
 * 用于返回评论的详细信息，包含子评论
 */
@Data
public class CommentVo {
    private Long id;
    private Long postId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Long parentId;
    private String bodyMd;
    private String status;
    private Boolean removed;
    private LocalDateTime createdAt;
    private List<CommentVo> children;
}