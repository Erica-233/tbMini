package com.example.tbmini.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报视图对象DTO
 * 用于返回举报的详细信息
 */
@Data
public class ReportVo {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String targetType;
    private Long targetId;
    private String reasonCode;
    private String reasonText;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}