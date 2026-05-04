package com.example.tbmini.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.BoardCreateRequest;
import com.example.tbmini.dto.BoardVo;
import com.example.tbmini.dto.ModerationActionVo;
import com.example.tbmini.dto.ModerationRequest;
import com.example.tbmini.dto.PostVo;
import com.example.tbmini.dto.ReportVo;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.AdminService;
import com.example.tbmini.service.BoardService;
import com.example.tbmini.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 * 提供后台管理功能，包括帖子审核、举报管理和操作记录查询
 * 所有接口需要管理员权限访问
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;
    private final BoardService boardService;

    /**
     * 获取待审核帖子列表
     * @param status 帖子状态，默认PENDING_REVIEW（待审核）
     * @param page 页码，默认第1页
     * @param size 每页数量，默认10条
     * @return 分页后的待审核帖子列表
     */
    @GetMapping("/mod-queue/posts")
    public Result<Page<PostVo>> pendingPosts(
            @RequestParam(defaultValue = "PENDING_REVIEW") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.pendingReviewPosts(page, size));
    }

    /**
     * 审批通过帖子
     * @param id 帖子ID
     * @param reason 审批理由（可选）
     * @param principal 当前登录的管理员信息
     * @return 操作结果
     */
    @PostMapping("/posts/{id}/approve")
    public Result<Void> approvePost(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        ModerationRequest req = new ModerationRequest();
        req.setTargetType("POST");
        req.setTargetId(id);
        req.setReason(reason);
        adminService.approve(req, principal.getUserId());
        return Result.ok();
    }

    /**
     * 删除/移除帖子
     * @param id 帖子ID
     * @param reason 删除理由（可选）
     * @param principal 当前登录的管理员信息
     * @return 操作结果
     */
    @PostMapping("/posts/{id}/remove")
    public Result<Void> removePost(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        ModerationRequest req = new ModerationRequest();
        req.setTargetType("POST");
        req.setTargetId(id);
        req.setReason(reason);
        adminService.remove(req, principal.getUserId());
        return Result.ok();
    }

    /**
     * 获取举报列表
     * @param status 举报状态，默认OPEN（待处理）
     * @return 举报列表
     */
    @GetMapping("/reports")
    public Result<List<ReportVo>> reports(@RequestParam(defaultValue = "OPEN") String status) {
        return Result.ok(reportService.listOpenReports());
    }

    /**
     * 获取管理操作记录
     * @return 所有管理操作历史记录
     */
    @GetMapping("/actions")
    public Result<List<ModerationActionVo>> actions() {
        return Result.ok(adminService.listActions());
    }

    @PostMapping("/boards")
    public Result<BoardVo> createBoard(@Valid @RequestBody BoardCreateRequest req) {
        return Result.ok(boardService.createBoard(req));
    }
}