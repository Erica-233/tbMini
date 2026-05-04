package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 看板视图对象DTO
 * 用于返回看板列表的简要信息
 */
@Data
public class BoardVo {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime createdAt;
}