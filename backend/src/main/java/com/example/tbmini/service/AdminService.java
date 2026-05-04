package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.Comment;
import com.example.tbmini.domain.entity.ModerationAction;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Report;
import com.example.tbmini.dto.ModerationActionVo;
import com.example.tbmini.dto.ModerationRequest;
import com.example.tbmini.dto.PostVo;
import com.example.tbmini.mapper.CommentMapper;
import com.example.tbmini.mapper.ModerationActionMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.ReportMapper;
import com.example.tbmini.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final ModerationActionMapper moderationActionMapper;
    private final UserMapper userMapper;

    public Page<PostVo> pendingReviewPosts(int page, int size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", "PENDING_REVIEW").orderByDesc("created_at");
        Page<Post> p = postMapper.selectPage(new Page<>(page, size), qw);
        List<PostVo> list = p.getRecords().stream().map(this::toPostVo).collect(Collectors.toList());
        Page<PostVo> res = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        res.setRecords(list);
        return res;
    }

    @Transactional
    public void approve(ModerationRequest req, Long moderatorId) {
        if ("POST".equals(req.getTargetType())) {
            Post post = postMapper.selectById(req.getTargetId());
            if (post == null) throw new BizException(404, "帖子不存在");
            post.setStatus("PUBLISHED");
            postMapper.updateById(post);
        } else if ("COMMENT".equals(req.getTargetType())) {
            Comment comment = commentMapper.selectById(req.getTargetId());
            if (comment == null) throw new BizException(404, "评论不存在");
            comment.setStatus("PUBLISHED");
            commentMapper.updateById(comment);
        } else {
            throw new BizException("非法targetType");
        }
        resolveReports(req.getTargetType(), req.getTargetId());
        logAction(moderatorId, req.getTargetType(), req.getTargetId(), "APPROVE", req.getReason());
    }

    @Transactional
    public void remove(ModerationRequest req, Long moderatorId) {
        if ("POST".equals(req.getTargetType())) {
            Post post = postMapper.selectById(req.getTargetId());
            if (post == null) throw new BizException(404, "帖子不存在");
            post.setStatus("REMOVED");
            postMapper.updateById(post);
        } else if ("COMMENT".equals(req.getTargetType())) {
            Comment comment = commentMapper.selectById(req.getTargetId());
            if (comment == null) throw new BizException(404, "评论不存在");
            comment.setStatus("REMOVED");
            commentMapper.updateById(comment);
        } else {
            throw new BizException("非法targetType");
        }
        resolveReports(req.getTargetType(), req.getTargetId());
        logAction(moderatorId, req.getTargetType(), req.getTargetId(), "REMOVE", req.getReason());
    }

    private void resolveReports(String targetType, Long targetId) {
        QueryWrapper<Report> qw = new QueryWrapper<>();
        qw.eq("target_type", targetType).eq("target_id", targetId).eq("is_open", 1);
        List<Report> reports = reportMapper.selectList(qw);
        for (Report r : reports) {
            r.setStatus("RESOLVED");
            r.setIsOpen(false);
            r.setResolvedAt(LocalDateTime.now());
            reportMapper.updateById(r);
        }
    }

    private void logAction(Long moderatorId, String targetType, Long targetId, String action, String reason) {
        ModerationAction ma = new ModerationAction();
        ma.setModeratorId(moderatorId);
        ma.setTargetType(targetType);
        ma.setTargetId(targetId);
        ma.setAction(action);
        ma.setReason(reason);
        moderationActionMapper.insert(ma);
    }

    public List<ModerationActionVo> listActions() {
        return moderationActionMapper.selectList(new QueryWrapper<ModerationAction>().orderByDesc("created_at"))
                .stream().map(this::toActionVo).collect(Collectors.toList());
    }

    private PostVo toPostVo(Post post) {
        PostVo vo = new PostVo();
        vo.setId(post.getId());
        vo.setBoardId(post.getBoardId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImageUrl(post.getImageUrl());
        vo.setStatus(post.getStatus());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());
        var user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUserId(user.getId());
            vo.setUserNickname(user.getNickname());
        }
        return vo;
    }

    private ModerationActionVo toActionVo(ModerationAction ma) {
        ModerationActionVo vo = new ModerationActionVo();
        vo.setId(ma.getId());
        vo.setModeratorId(ma.getModeratorId());
        vo.setTargetType(ma.getTargetType());
        vo.setTargetId(ma.getTargetId());
        vo.setAction(ma.getAction());
        vo.setReason(ma.getReason());
        vo.setCreatedAt(ma.getCreatedAt());
        if (ma.getModeratorId() != null) {
            var user = userMapper.selectById(ma.getModeratorId());
            if (user != null) vo.setModeratorNickname(user.getNickname());
        }
        return vo;
    }
}
