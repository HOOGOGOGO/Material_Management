package com.rblue.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rblue.dto.InformDto;
import com.rblue.entity.Category;
import com.rblue.entity.Warehouse;

public interface WarehouseService extends IService<Warehouse> {

    //分页查询所有仓库信息
    public Page selectAll(int page, int pageSize, String name);

}
