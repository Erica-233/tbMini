package com.example.tbmini.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reports")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    private String targetType;

    private Long targetId;

    private String reasonCode;

    private String reasonText;

    private String status;

    private Boolean isOpen;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
