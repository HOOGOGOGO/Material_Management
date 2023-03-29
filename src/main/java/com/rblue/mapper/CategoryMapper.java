package com.rblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rblue.entity.Category;
import com.rblue.entity.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}
