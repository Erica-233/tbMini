package com.example.tbmini.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tbmini.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("SELECT * FROM comments WHERE post_id = #{postId} AND parent_id IS NULL ORDER BY created_at DESC")
    List<Comment> selectRootComments(@Param("postId") Long postId);

    @Select("SELECT * FROM comments WHERE parent_id = #{parentId} ORDER BY created_at ASC")
    List<Comment> selectReplies(@Param("parentId") Long parentId);
}
