package com.rblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.dto.InformDto;
import com.rblue.dto.MaterialDto;
import com.rblue.dto.OutformDto;
import com.rblue.dto.WarehouseDto;
import com.rblue.entity.*;
import com.rblue.mapper.InformMapper;
import com.rblue.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InformServiceImpl extends ServiceImpl<InformMapper, Inform> implements InformService {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private InformMaterialService informMaterialService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WarehouseService warehouseService;

    //保存添加的材料入库单
    public void saveInform(InformDto indto){

        //保存订单
        this.save(indto);

        //获取当前入库单号
        Long id=indto.getId();

        List<MaterialDto> materials=indto.getMaterials();

        //获取其中的每一个材料id并存储到数据库中
       List<InformMaterial> lists= materials.stream().map(item->{
            //创建In对象
            InformMaterial in=new InformMaterial();

            //获取材料id
            Long materialId=item.getId();
            //设置材料单号
            in.setMaterialId(materialId);
            //插入唯一的入库单号
            in.setInformId(id);
            return in;
        }).collect(Collectors.toList());
       //保存到入库材料表中内
        informMaterialService.saveBatch(lists);


    }

    //更新添加的材料入库单
    public void updateInform(InformDto indto){
        //创建对象
        Inform inform=new Inform();
        //获取入库单id
        Long id=indto.getId();
        //复制
        BeanUtils.copyProperties(indto,inform);
        //更新订单
        this.updateById(inform);
        //根据入库单id去删除入库材料
        //添加条件
        LambdaQueryWrapper<InformMaterial> lqw=new LambdaQueryWrapper<>();
        lqw.eq(InformMaterial::getInformId,id);
        informMaterialService.remove(lqw);
        //获取当前更新后的材料信息
        List<MaterialDto> materials=indto.getMaterials();

        //获取其中的每一个材料id并存储到数据库中
        List<InformMaterial> lists= materials.stream().map(item->{
            //创建In对象
            InformMaterial in=new InformMaterial();

            //获取材料id
            Long materialId=item.getId();
            //设置材料单号
            in.setMaterialId(materialId);
            //插入唯一的入库单号
            in.setInformId(id);
            return in;
        }).collect(Collectors.toList());
        //保存到入库材料表中内
        informMaterialService.saveBatch(lists);


    }

    //根据id查找
    public InformDto selectById(Long id){
        //根据id查询出库单
        Inform inform= this.getById(id);
        //根据创建人查询信息
        Manager manager=new Manager();
        manager=managerService.getById(inform.getCreateUser());
        //根据出库单号获取所有的出库单材料信息
        LambdaQueryWrapper<InformMaterial> lqw=new LambdaQueryWrapper<>();
        //添加入库单id条件
        lqw.eq(InformMaterial::getInformId,id);
        //查询得到所有的出库材料记录
        List<InformMaterial> informMaterials= informMaterialService.list(lqw);
        //遍历得到材料信息
        List<MaterialDto> materialDtoList=informMaterials.stream().map(item->{
            //获取材料 Id、入库量、和领料次数
            Long materialId=item.getMaterialId();
            //根据id查询材料信息
            Material material= materialService.getById(materialId);
            MaterialDto materialDto=new MaterialDto();
            BeanUtils.copyProperties(material,materialDto);
            //获取仓库id查询仓库信息
           Warehouse warehouse=warehouseService.getById( material.getWarehouseId());
           materialDto.setWarehouse(warehouse);
            return materialDto;
        }).collect(Collectors.toList());

        InformDto informDto=new InformDto();

        //复制出库信息
        BeanUtils.copyProperties(inform,informDto);
        //设置材料信息
        informDto.setMaterials(materialDtoList);
        //设置填写人
        informDto.setUserName(manager.getName());
        return informDto;
    }

    //将入库单中的材料入库
    public String inWarehouse(InformDto informDto){
        //获取当前入库单号
        Long id=informDto.getId();
        //复制到新的入库对象
        Inform inform=new Inform();
        BeanUtils.copyProperties(informDto,inform); //复制值到不同的对象
        //遍历所有的材料，获取材料id
        List<Material> materialList=new ArrayList<>();
        //遍历所有的材料
        materialList=informDto.getMaterials().stream().map(item->{ //材料集合
            Material material=new Material();
            //复制
            BeanUtils.copyProperties(item,material);
            //设置为入库状态
            material.setStatus(1);
            return material;
        }).collect(Collectors.toList());
        //判断是否写入了入库信息
        for (Material material:materialList){
            if(material.getWarehouseId()==null) return "当前有材料未填写入库信息";
        }

        //将所有材料全部更新
        materialService.updateBatchById(materialList);
        //将入库单更新为入库状态
        inform.setStorageStatus(1);
        this.updateById(inform);
        return "success";
    }
}
