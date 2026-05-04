package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.dto.BoardCreateRequest;
import com.example.tbmini.dto.BoardDetailResponse;
import com.example.tbmini.dto.BoardVo;
import com.example.tbmini.dto.PostListItemVo;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 看板服务
 * 处理看板（板块）的查询和帖子列表获取
 */
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    /**
     * 获取所有活跃看板列表
     * @return 看板列表
     */
    public List<BoardVo> listActiveBoards() {
        QueryWrapper<Board> qw = new QueryWrapper<>();
        qw.eq("is_active", 1).orderByAsc("sort_order", "id");
        return boardMapper.selectList(qw).stream().map(this::toBoardVo).collect(Collectors.toList());
    }

    public BoardVo createBoard(BoardCreateRequest req) {
        QueryWrapper<Board> qw = new QueryWrapper<>();
        qw.eq("slug", req.getSlug());
        if (boardMapper.selectOne(qw) != null) {
            throw new BizException(400, "slug已存在，请换一个");
        }
        Board board = new Board();
        board.setName(req.getName());
        board.setSlug(req.getSlug());
        board.setDescription(req.getDescription());
        board.setIsActive(true);
        board.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        boardMapper.insert(board);
        return toBoardVo(board);
    }

    /**
     * 根据slug获取看板详情
     * @param slug 看板的URL友好名称
     * @return 看板详细信息
     */
    public BoardDetailResponse getBoardBySlug(String slug) {
        QueryWrapper<Board> qw = new QueryWrapper<>();
        qw.eq("slug", slug);
        Board board = boardMapper.selectOne(qw);
        if (board == null || Boolean.FALSE.equals(board.getIsActive())) {
            throw new BizException(404, "板块不存在");
        }
        return toBoardDetail(board);
    }

    /**
     * 获取看板下的帖子列表
     * @param slug 看板的URL友好名称
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页后的帖子列表
     */
    public Page<PostListItemVo> listBoardPosts(String slug, int page, int pageSize) {
        Board board = boardMapper.selectOne(new QueryWrapper<Board>().eq("slug", slug));
        if (board == null || Boolean.FALSE.equals(board.getIsActive())) {
            throw new BizException(404, "板块不存在");
        }

        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("board_id", board.getId());
        qw.eq("status", "PUBLISHED");
        qw.orderByDesc("created_at");
        Page<Post> postPage = postMapper.selectPage(new Page<>(page, pageSize), qw);

        List<PostListItemVo> records = postPage.getRecords().stream().map(this::toPostListItemVo).collect(Collectors.toList());
        Page<PostListItemVo> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 将Board实体转换为BoardVo
     * @param b 看板实体
     * @return 看板VO对象
     */
    private BoardVo toBoardVo(Board b) {
        BoardVo vo = new BoardVo();
        vo.setId(b.getId());
        vo.setName(b.getName());
        vo.setSlug(b.getSlug());
        vo.setDescription(b.getDescription());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }

    /**
     * 将Board实体转换为BoardDetailResponse
     * @param b 看板实体
     * @return 看板详细响应对象
     */
    private BoardDetailResponse toBoardDetail(Board b) {
        BoardDetailResponse vo = new BoardDetailResponse();
        vo.setId(b.getId());
        vo.setName(b.getName());
        vo.setSlug(b.getSlug());
        vo.setDescription(b.getDescription());
        vo.setIsActive(b.getIsActive());
        vo.setCreatedAt(b.getCreatedAt());
        return vo;
    }

    /**
     * 将Post实体转换为PostListItemVo
     * @param post 帖子实体
     * @return 帖子列表项VO
     */
    private PostListItemVo toPostListItemVo(Post post) {
        PostListItemVo vo = new PostListItemVo();
        vo.setId(post.getId());
        vo.setBoardId(post.getBoardId());
        vo.setTitle(post.getTitle());
        vo.setSummary(extractSummary(post.getContent()));
        vo.setImageUrl(post.getImageUrl());
        vo.setLikeCount(post.getLikeCount());
        vo.setCreatedAt(post.getCreatedAt());
        return vo;
    }

    /**
     * 提取内容摘要
     * @param content 原始内容（可能包含HTML标签）
     * @return 纯文本摘要
     */
    private String extractSummary(String content) {
        if (content == null) return "";
        String plain = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return plain.length() > 200 ? plain.substring(0, 200) + "..." : plain;
    }
}