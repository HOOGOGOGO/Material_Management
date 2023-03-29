package com.rblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Material;
import com.rblue.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {



}
