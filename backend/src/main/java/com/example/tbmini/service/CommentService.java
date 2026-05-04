package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.Comment;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.dto.CommentRequest;
import com.example.tbmini.dto.CommentVo;
import com.example.tbmini.mapper.CommentMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Transactional
    public CommentVo createComment(CommentRequest req, Long userId) {
        Post post = postMapper.selectById(req.getPostId());
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(404, "帖子不存在");
        }
        if (req.getParentId() != null) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent == null || parent.getParentId() != null) {
                throw new BizException("只能回复根评论");
            }
            if (!parent.getPostId().equals(req.getPostId())) {
                throw new BizException("评论与帖子不匹配");
            }
        }
        Comment comment = new Comment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId());
        comment.setContent(req.getBodyMd());
        comment.setBodyMd(req.getBodyMd());
        comment.setStatus("PUBLISHED");
        commentMapper.insert(comment);
        return toVo(comment, false);
    }

    public List<CommentVo> listComments(Long postId, boolean admin) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(404, "帖子不存在");
        }
        List<Comment> roots = commentMapper.selectRootComments(postId);
        if (!admin) {
            roots = roots.stream().filter(c -> !"REMOVED".equals(c.getStatus())).collect(Collectors.toList());
        }
        List<CommentVo> rootVos = roots.stream().map(c -> toVo(c, admin)).collect(Collectors.toList());
        for (CommentVo root : rootVos) {
            List<Comment> replies = commentMapper.selectReplies(root.getId());
            if (!admin) {
                replies = replies.stream().filter(c -> !"REMOVED".equals(c.getStatus())).collect(Collectors.toList());
            }
            root.setChildren(replies.stream().map(c -> toVo(c, admin)).collect(Collectors.toList()));
        }
        return rootVos;
    }

    @Transactional
    public void removeComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BizException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BizException(403, "无权限删除该评论");
        }
        comment.setStatus("REMOVED");
        comment.setContent(null);
        comment.setBodyMd(null);
        commentMapper.updateById(comment);
    }

    private CommentVo toVo(Comment comment, boolean admin) {
        CommentVo vo = new CommentVo();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setParentId(comment.getParentId());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        boolean removed = "REMOVED".equals(comment.getStatus());
        vo.setRemoved(removed);
        if (removed && !admin) {
            vo.setBodyMd(null);
        } else {
            vo.setBodyMd(comment.getBodyMd());
        }
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUserId(user.getId());
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }
}
