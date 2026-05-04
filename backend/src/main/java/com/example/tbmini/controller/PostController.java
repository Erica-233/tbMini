package com.example.tbmini.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.PostCreateRequest;
import com.example.tbmini.dto.PostDetailResponse;
import com.example.tbmini.dto.PostVo;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.PostService;
import com.example.tbmini.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帖子控制器
 * 处理帖子的完整生命周期，包括浏览、创建、更新、删除和点赞
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UploadService uploadService;

    /**
     * 获取帖子列表
     * @param boardId 看板ID（可选，筛选特定看板的帖子）
     * @param page 页码，默认第1页
     * @param size 每页数量，默认10条
     * @param principal 当前登录用户（可选，影响返回内容）
     * @return 分页后的帖子列表
     */
    @GetMapping
    public Result<Page<PostVo>> list(
            @RequestParam(required = false) Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.listPosts(boardId, page, size));
    }

    /**
     * 获取帖子详情
     * @param id 帖子ID
     * @param principal 当前登录用户（可选，影响返回内容）
     * @return 帖子详细信息
     */
    @GetMapping("/{id}")
    public Result<PostDetailResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        boolean admin = isAdmin(principal);
        return Result.ok(postService.getPostDetail(id, admin));
    }

    /**
     * 创建帖子
     * @param req 帖子创建请求（包含标题、内容、看板ID等）
     * @param images 帖子图片（可选，最多9张）
     * @param principal 当前登录用户
     * @return 创建的帖子详细信息
     */
    @PostMapping(consumes = "multipart/form-data")
    public Result<PostDetailResponse> create(
            @Valid @RequestPart("data") PostCreateRequest req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<String> imageUrls = uploadService.saveImages(images);
        return Result.ok(postService.createPost(req, imageUrls, principal.getUserId()));
    }

    /**
     * 更新帖子
     * @param id 帖子ID
     * @param req 帖子更新请求
     * @param images 新增的图片（可选）
     * @param principal 当前登录用户
     * @return 更新后的帖子详细信息
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Result<PostDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("data") PostCreateRequest req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<String> imageUrls = uploadService.saveImages(images);
        return Result.ok(postService.updatePost(id, req, imageUrls, principal.getUserId(), isAdmin(principal)));
    }

    /**
     * 删除帖子
     * @param id 帖子ID
     * @param principal 当前登录用户（普通用户只能删除自己的帖子，管理员可删除任意帖子）
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        postService.removePost(id, principal.getUserId(), isAdmin(principal));
        return Result.ok();
    }

    /**
     * 点赞帖子
     * @param id 帖子ID
     * @param principal 当前登录用户
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        postService.likePost(id, principal.getUserId());
        return Result.ok();
    }

    /**
     * 取消点赞帖子
     * @param id 帖子ID
     * @param principal 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        postService.unlikePost(id, principal.getUserId());
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