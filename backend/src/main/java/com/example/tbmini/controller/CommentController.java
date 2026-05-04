package com.example.tbmini.controller;

import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.CommentRequest;
import com.example.tbmini.dto.CommentVo;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * 处理帖子的评论相关操作，包括查看、创建和删除评论
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * 获取帖子的评论列表
     * @param postId 帖子ID
     * @param principal 当前登录用户信息（可选，管理员可看到所有评论）
     * @return 评论列表
     */
    @GetMapping("/posts/{postId}/comments")
    public Result<List<CommentVo>> list(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean admin = isAdmin(principal);
        return Result.ok(commentService.listComments(postId, admin));
    }

    /**
     * 创建评论
     * @param req 评论请求信息
     * @param principal 当前登录用户
     * @return 创建的评论信息
     */
    @PostMapping("/comments")
    public Result<CommentVo> create(@Valid @RequestBody CommentRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(commentService.createComment(req, principal.getUserId()));
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @param principal 当前登录用户（普通用户只能删除自己的评论，管理员可删除任意评论）
     * @return 操作结果
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> remove(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        commentService.removeComment(id, principal.getUserId(), isAdmin(principal));
        return Result.ok();
    }

    /**
     * 判断当前用户是否为管理员
     * @param principal 用户主体
     * @return 是否为管理员
     */
    private boolean isAdmin(UserPrincipal principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}