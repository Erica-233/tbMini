package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailResponse {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String title;
    private String bodyMd;
    private String status;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images;
}
