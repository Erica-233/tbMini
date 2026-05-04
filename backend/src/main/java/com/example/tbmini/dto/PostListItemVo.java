package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表项视图对象DTO
 * 用于返回帖子列表中的简要信息
 */
@Data
public class PostListItemVo {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String title;
    private String summary;
    private String imageUrl;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
}