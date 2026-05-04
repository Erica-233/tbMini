package com.example.tbmini.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tbmini.domain.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    @Select("SELECT COUNT(*) FROM reports WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 'OPEN'")
    int countOpenByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
