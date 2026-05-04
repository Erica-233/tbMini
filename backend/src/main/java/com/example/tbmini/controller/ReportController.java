package com.example.tbmini.controller;

import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.ReportRequest;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 举报控制器
 * 处理用户对帖子或评论的举报功能
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    /**
     * 提交举报
     * @param req 举报请求信息（包含被举报内容类型、ID和举报原因）
     * @param principal 当前登录用户
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> report(@Valid @RequestBody ReportRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        reportService.report(req, principal.getUserId());
        return Result.ok();
    }
}