package com.example.tbmini.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.BoardDetailResponse;
import com.example.tbmini.dto.BoardVo;
import com.example.tbmini.dto.PostListItemVo;
import com.example.tbmini.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 看板控制器
 * 提供看板（板块）的浏览功能，包括看板列表、看板详情和看板下的帖子列表
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    /**
     * 获取所有活跃看板列表
     * @return 看板列表
     */
    @GetMapping
    public Result<List<BoardVo>> list() {
        return Result.ok(boardService.listActiveBoards());
    }

    /**
     * 获取看板详情
     * @param slug 看板的URL友好名称（slug）
     * @return 看板详细信息
     */
    @GetMapping("/{slug}")
    public Result<BoardDetailResponse> get(@PathVariable String slug) {
        return Result.ok(boardService.getBoardBySlug(slug));
    }

    /**
     * 获取看板下的帖子列表
     * @param slug 看板的URL友好名称
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认20条
     * @return 分页后的帖子列表
     */
    @GetMapping("/{slug}/posts")
    public Result<Page<PostListItemVo>> posts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(boardService.listBoardPosts(slug, page, pageSize));
    }
}