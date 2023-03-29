package com.rblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Category;
import com.rblue.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaterialDtoMapper extends BaseMapper<MaterialDto> {
    //多表查询的分类查询
    List<MaterialDto> selectAllByNameAndCategoryName(@Param("begin") int begin, @Param("size") int size, @Param("materialDto") MaterialDto materialDto);
    //多表查询的分类查询
    List<MaterialDto> selectAllByNameAndCategoryNameAndWarehouse(@Param("begin") int begin, @Param("size") int size, @Param("materialDto") MaterialDto materialDto);

//根据条件查询总记录数
    int selectTotalByCondition(@Param("materialDto") MaterialDto materialDto);//因为没有使用Param注解，当下次使用时直接用对应的属性名就可以

    int selectTotalByConditionAndWarehouse(@Param("materialDto") MaterialDto materialDto);//因为没有使用Param注解，当下次使用时直接用对应的属性名就可以
}
