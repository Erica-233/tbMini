package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 看板详情响应DTO
 * 用于返回看板的详细信息
 */
@Data
public class BoardDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}