package com.example.tbmini.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tbmini.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
