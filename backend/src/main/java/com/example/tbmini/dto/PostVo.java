package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVo {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String title;
    private String content;
    private String imageUrl;
    private String status;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;
}
