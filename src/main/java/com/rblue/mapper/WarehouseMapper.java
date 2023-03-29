package com.rblue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rblue.entity.Category;
import com.rblue.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {
}
