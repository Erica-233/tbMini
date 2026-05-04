package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.BizException;
import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.PostImage;
import com.example.tbmini.domain.entity.PostLike;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.dto.PostCreateRequest;
import com.example.tbmini.dto.PostDetailResponse;
import com.example.tbmini.dto.PostVo;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.PostImageMapper;
import com.example.tbmini.mapper.PostLikeMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.UserMapper;
import com.example.tbmini.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final BoardMapper boardMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostImageMapper postImageMapper;

    public Page<PostVo> listPosts(Long boardId, int page, int size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        if (boardId != null) {
            qw.eq("board_id", boardId);
        }
        qw.eq("status", "PUBLISHED");
        qw.orderByDesc("created_at");
        Page<Post> p = postMapper.selectPage(new Page<>(page, size), qw);
        return convertPage(p);
    }

    public PostDetailResponse getPostDetail(Long id, boolean admin) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!admin && !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(404, "帖子不存在");
        }
        return toDetailVo(post);
    }

    public PostVo getPost(Long id, boolean admin) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!admin && !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(404, "帖子不存在");
        }
        return toVo(post);
    }

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest req, List<String> imageUrls, Long userId) {
        Board board = boardMapper.selectOne(new QueryWrapper<Board>().eq("slug", req.getBoardSlug()));
        if (board == null || Boolean.FALSE.equals(board.getIsActive())) {
            throw new BizException("板块不存在");
        }
        Post post = new Post();
        post.setBoardId(board.getId());
        post.setUserId(userId);
        post.setTitle(req.getTitle());
        post.setBodyMd(req.getBodyMd());
        post.setContent(req.getBodyMd());
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            post.setImageUrl(imageUrls.get(0));
        }
        postMapper.insert(post);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int sort = 0;
            for (String url : imageUrls) {
                PostImage pi = new PostImage();
                pi.setPostId(post.getId());
                pi.setImageUrl(url);
                pi.setSortOrder(sort++);
                postImageMapper.insert(pi);
            }
        }

        return toDetailVo(post);
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, PostCreateRequest req, List<String> imageUrls, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BizException(403, "无权限编辑该帖子");
        }

        if (req.getBoardSlug() != null && !req.getBoardSlug().isBlank()) {
            Board board = boardMapper.selectOne(new QueryWrapper<Board>().eq("slug", req.getBoardSlug()));
            if (board == null || Boolean.FALSE.equals(board.getIsActive())) {
                throw new BizException("板块不存在");
            }
            post.setBoardId(board.getId());
        }
        if (req.getTitle() != null) {
            post.setTitle(req.getTitle());
        }
        if (req.getBodyMd() != null) {
            post.setBodyMd(req.getBodyMd());
            post.setContent(req.getBodyMd());
        }
        postMapper.updateById(post);

        if (imageUrls != null) {
            postImageMapper.delete(new QueryWrapper<PostImage>().eq("post_id", postId));
            int sort = 0;
            for (String url : imageUrls) {
                PostImage pi = new PostImage();
                pi.setPostId(postId);
                pi.setImageUrl(url);
                pi.setSortOrder(sort++);
                postImageMapper.insert(pi);
            }
            if (!imageUrls.isEmpty()) {
                post.setImageUrl(imageUrls.get(0));
                postMapper.updateById(post);
            }
        }

        return toDetailVo(post);
    }

    @Transactional
    public void removePost(Long postId, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BizException(403, "无权限删除该帖子");
        }
        post.setStatus("REMOVED");
        postMapper.updateById(post);
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equals(post.getStatus())) {
            throw new BizException(404, "帖子不存在");
        }
        QueryWrapper<PostLike> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).eq("user_id", userId);
        PostLike exist = postLikeMapper.selectOne(qw);
        if (exist != null) {
            return;
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        postLikeMapper.insert(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.updateById(post);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        QueryWrapper<PostLike> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).eq("user_id", userId);
        PostLike exist = postLikeMapper.selectOne(qw);
        if (exist == null) {
            return;
        }
        postLikeMapper.delete(qw);
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
        }
    }

    private Page<PostVo> convertPage(Page<Post> page) {
        List<PostVo> list = page.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        Page<PostVo> res = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        res.setRecords(list);
        return res;
    }

    private PostVo toVo(Post post) {
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
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUserId(user.getId());
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        Board board = boardMapper.selectById(post.getBoardId());
        if (board != null) {
            vo.setBoardName(board.getName());
        }
        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal != null) {
            QueryWrapper<PostLike> qw = new QueryWrapper<>();
            qw.eq("post_id", post.getId()).eq("user_id", principal.getUserId());
            vo.setLiked(postLikeMapper.selectOne(qw) != null);
        } else {
            vo.setLiked(false);
        }
        return vo;
    }

    private PostDetailResponse toDetailVo(Post post) {
        PostDetailResponse vo = new PostDetailResponse();
        vo.setId(post.getId());
        vo.setBoardId(post.getBoardId());
        vo.setTitle(post.getTitle());
        vo.setBodyMd(post.getBodyMd());
        vo.setStatus(post.getStatus());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUserId(user.getId());
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        Board board = boardMapper.selectById(post.getBoardId());
        if (board != null) {
            vo.setBoardName(board.getName());
        }
        List<PostImage> images = postImageMapper.selectList(
                new QueryWrapper<PostImage>().eq("post_id", post.getId()).orderByAsc("sort_order"));
        vo.setImages(images.stream().map(PostImage::getImageUrl).toList());

        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal != null) {
            QueryWrapper<PostLike> qw = new QueryWrapper<>();
            qw.eq("post_id", post.getId()).eq("user_id", principal.getUserId());
            vo.setLiked(postLikeMapper.selectOne(qw) != null);
        } else {
            vo.setLiked(false);
        }
        return vo;
    }

    public Page<PostVo> listUserPosts(String username, int page, int size) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("user_id", user.getId());
        qw.eq("status", "PUBLISHED");
        qw.orderByDesc("created_at");
        Page<Post> p = postMapper.selectPage(new Page<>(page, size), qw);
        return convertPage(p);
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
            return up;
        }
        return null;
    }
}
