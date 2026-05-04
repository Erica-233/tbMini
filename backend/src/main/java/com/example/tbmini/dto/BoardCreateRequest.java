package com.example.tbmini.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardCreateRequest {
    @NotBlank(message = "板块名称不能为空")
    @Size(max = 100, message = "板块名称最长100字符")
    private String name;

    @NotBlank(message = "slug不能为空")
    @Size(max = 100, message = "slug最长100字符")
    private String slug;

    @Size(max = 500, message = "描述最长500字符")
    private String description;

    private Integer sortOrder;
}
