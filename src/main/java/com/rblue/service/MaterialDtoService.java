package com.rblue.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Category;

import java.util.List;

public interface MaterialDtoService extends IService<MaterialDto> {

    //多表查询的分类查询
    Page<MaterialDto> selectAllByNameAndCategoryName(int page, int pageSize, MaterialDto materialDto);

    //    查询记录总数
    int selectTotal();

}
