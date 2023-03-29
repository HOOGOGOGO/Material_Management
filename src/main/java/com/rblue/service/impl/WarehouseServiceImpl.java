package com.rblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.dto.WarehouseDto;
import com.rblue.entity.Inform;
import com.rblue.entity.Manager;
import com.rblue.entity.Material;
import com.rblue.entity.Warehouse;
import com.rblue.mapper.WarehouseMapper;
import com.rblue.service.InformService;
import com.rblue.service.ManagerService;
import com.rblue.service.MaterialService;
import com.rblue.service.WarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {
    @Autowired
    private ManagerService managerService;

    //分页查询所有仓库信息
    public Page selectAll(int page, int pageSize, String name){

        //构造页表构造器
        Page<Warehouse> warehousePage = new Page<>(page, pageSize);
        //做增强，查询多表
        Page<WarehouseDto> dtoPage = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Warehouse> lqw = new LambdaQueryWrapper<>();
        //添加查询名称条件
        lqw.like(name != null, Warehouse::getName, name);
        //添加未删除条件
        lqw.eq(Warehouse::getIsDeleted, 0);
        //查询
        this.page(warehousePage, lqw);

        //此时warehousePage已有全部数据
        //将数据复制到dtoPage
        BeanUtils.copyProperties(warehousePage,dtoPage,"records");
        //获取仓库的集合
        List<Warehouse> warehouseList = warehousePage.getRecords();

        List<WarehouseDto> lists = warehouseList.stream().map(item -> {
            //创建一个dto对象
            WarehouseDto dto = new WarehouseDto();
            //复制内容
            BeanUtils.copyProperties(item,dto);
            //获取负责人id
            Long id = item.getManagerId();
            //查询得到管理员的记录
            Manager manager = managerService.getById(id);
            dto.setManagerName(manager.getName());
            return dto;
        }).collect(Collectors.toList());//得到所有仓库信息
        dtoPage.setRecords(lists);
        return dtoPage;
    }


}
