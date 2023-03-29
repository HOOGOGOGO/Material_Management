package com.rblue.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rblue.entity.Material;
import com.rblue.entity.Warehouse;

public interface MaterialService extends IService<Material> {

    //查询未入库材料信息
    public Page NoInWarehouse(int page,int pageSize,String name);

}
