package com.example.tbmini.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 举报请求DTO
 * 用于接收用户举报内容的请求参数
 */
@Data
public class ReportRequest {
    @NotBlank
    private String targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    private String reasonCode;

    private String reasonText;
}