package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.dto.WarehouseDto;
import com.rblue.entity.Inform;
import com.rblue.entity.Manager;
import com.rblue.entity.Material;
import com.rblue.entity.Warehouse;
import com.rblue.service.InformService;
import com.rblue.service.ManagerService;
import com.rblue.service.MaterialService;
import com.rblue.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.DataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/storage")
@Slf4j
@RestController
public class WarehouseController {
    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private InformService informService;
    /**
     * 查询所有仓库
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectAll(int page, int pageSize, String name) {
       //查询所有仓库信息
        Page<WarehouseDto> dtoPage=warehouseService.selectAll(page,pageSize,name);
        return R.success(dtoPage);
    }

    /**
     * 获取当前需要修改的人的信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Warehouse> selectById(@PathVariable Long id) {
        LambdaQueryWrapper<Warehouse> lqw=new LambdaQueryWrapper<>();
        //添加条件
        lqw.eq(Warehouse::getIsDeleted,0).eq(Warehouse::getId,id);//根据id查询仓库信息
        Warehouse warehouse=warehouseService.getOne(lqw);
        return R.success(warehouse);
    }

    /**
     * 查询仓库名是否重复
     * @param name
     * @return
     */
    @GetMapping("/name/{name}")
    public R<String> isExistName(@PathVariable String name){
        if(name!=null){
            LambdaQueryWrapper<Warehouse> lqw=new LambdaQueryWrapper<>();
            lqw.eq(Warehouse::getName,name).eq(Warehouse::getIsDeleted,0);
            List<Warehouse> list = warehouseService.list(lqw);
            if(list.size()==0) {
                return R.success("仓库名允许使用");
            }
        }
        return R.error("当前仓库名已存在,请更换");
    }
    /**
     * 添加仓库
     *
     * @param warehouse
     * @return
     */
    @PostMapping
    public R<String> addWarehouse(@RequestBody Warehouse warehouse) {
        //添加数据到数据库

            if(warehouse!=null) {
                warehouseService.save(warehouse); //没有同名仓库可以添加新仓库
                return R.success("添加仓库成功");
            }

        return R.error("信息错误！");
    }

    /**
     * 修改仓库
     *
     * @param warehouse
     * @return
     */
    @PutMapping
    public R<String> editWarehouse(@RequestBody Warehouse warehouse) {
        //修改仓库信息
        if (warehouse != null)
            warehouseService.updateById(warehouse);
        return R.success("修改仓库成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        //接收仓库列表
        List<Warehouse> listW=new ArrayList<>();
        //遍历ids单个修改is——deleted为1
        for (Long id:ids){
            LambdaQueryWrapper<Material> lqw=new LambdaQueryWrapper<>();
            lqw.eq(Material::getWarehouseId,id).eq(Material::getIsDeleted,0);
            List<Material> list = materialService.list(lqw);
            if(list.size()>0){//当前仓库下还有材料，不允许删除
                Warehouse warehouse = warehouseService.getById(id);
                return R.error(warehouse.getName()+"还有材料，不允许删除");
            }
                Warehouse warehouse=new Warehouse();
                warehouse.setId(id);
                warehouse.setIsDeleted(1);
            listW.add(warehouse);
        }
        warehouseService.updateBatchById(listW); //将记录的isdeleted设为1
        return R.success("删除成功");
    }

    /**
     * 获取所有的仓库列表
     * @return
     */
    @GetMapping("/list")
    public R<List<Warehouse>> WarehouseList(){
        //添加条件
        LambdaQueryWrapper<Warehouse> lqw=new LambdaQueryWrapper<>();
        lqw.eq(Warehouse::getIsDeleted,0).eq(Warehouse::getStatus,1);
        //查询所有仓库
        List<Warehouse> lists=warehouseService.list(lqw);
        return R.success(lists);
    }

    /**
     * 将需要入库的材料入库
     * @param informDto
     * @return
     */
    @PutMapping("/addStorage")
    public R<String> inWarehouse(@RequestBody InformDto informDto) {
        String result = informService.inWarehouse(informDto);//材料入库，获取返回信息
        if (!result.equals("success")) {
            R.error(result); //当前返回值并不是成功，去前端回显错误信息
        }
        return R.success("入库成功");
    }

}
