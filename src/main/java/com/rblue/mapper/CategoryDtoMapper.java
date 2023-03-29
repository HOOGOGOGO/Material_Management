package com.rblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rblue.dto.CategoryDto;
import com.rblue.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryDtoMapper extends BaseMapper<CategoryDto> {

}
