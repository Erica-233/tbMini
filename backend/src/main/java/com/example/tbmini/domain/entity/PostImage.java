package com.example.tbmini.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子图片实体
 * 对应数据库中的post_images表
 */
@Data
@TableName("post_images")
public class PostImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String imageUrl;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}