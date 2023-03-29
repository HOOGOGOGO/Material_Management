package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.R;
import com.rblue.dto.CategoryDto;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Category;
import com.rblue.entity.Material;
import com.rblue.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/material")
@Slf4j
@RestController
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private CategoryService categoryService;

   @Autowired
    private MaterialDtoService dtoService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 获取当前需要修改的人的信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<MaterialDto> selectById(@PathVariable Long id) {
        Material material = new Material();
        material = materialService.getById(id);
        Long categoryId=material.getCategoryId();
        Category category=categoryService.getById(categoryId);

        MaterialDto materialDto=new MaterialDto();
        BeanUtils.copyProperties(material,materialDto);
        materialDto.setCategory(category);
        return R.success(materialDto);
    }

    /**
     * 添加材料
     *
     * @param material
     * @return
     */
    @PostMapping
    public R<String> addWarehouse(@RequestBody Material material) {
        //添加数据到数据库
        if (material != null){
            materialService.save(material);
            //清理redis
            Set keys=redisTemplate.keys("material_*");
            redisTemplate.delete(keys);//删除redis的指定key
        }

        return R.success("添加材料成功");
    }

    /**
     * 查询全部
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> selectAll(int page,int pageSize,String name,String categoryName,String status,String warehouseName){

        MaterialDto materialDto=new MaterialDto();

        //将材料分类名称封装到分类实体
            CategoryDto categoryDto=new CategoryDto();
            if(categoryName!=null)
            categoryDto.setCategoryName(categoryName);
            //设置材料分类名称
            materialDto.setCategoryDto(categoryDto);


        //设置材料名称
        if(name!=null)
        materialDto.setName(name);

        //设置入库状态
        if(status!=null)
        materialDto.setStatus(Integer.valueOf(status));

        if(warehouseName!=null)
            materialDto.setWarehouseName(warehouseName);

        Page<MaterialDto> dtoPage = dtoService.selectAllByNameAndCategoryName(page, pageSize, materialDto);


        return R.success(dtoPage);
    }

    /**
     * 修改材料
     *
     * @param material
     * @return
     */
    @PutMapping
    public R<String> editWarehouse(@RequestBody Material material) {
        //修改材料信息
        if(material!=null){
            materialService.updateById(material);
            //清理redis
            //清理redis
            Set keys=redisTemplate.keys("material_*");
            redisTemplate.delete(keys);//删除redis的指定key
        }
        return R.success("修改材料成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        //遍历ids单个修改is——deleted为1
        for (Long id:ids){
            if(id!=null){
                //判断当前材料是否还有库存
                Material material = materialService.getById(id);
                if(material.getAvailable()>0&&material.getStatus()==1){
                    return R.error("不允许删除，当前材料还有库存");
                }
                material.setIsDeleted(1);
                //更新
                materialService.updateById(material);
                //清理redis
                //清理redis
                Set keys=redisTemplate.keys("material_*");
                redisTemplate.delete(keys);//删除redis的指定key
            }
        }
        log.info(String.valueOf(ids));
        return R.success("删除成功");
    }

    /**
     * 未入库查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/only")
    public R<Page> onlyShow(int page,int pageSize,String name){
       Page<MaterialDto> dtoPage=materialService.NoInWarehouse(page,pageSize,name);
        return R.success(dtoPage);
    }

    /**
     * 按分类名称查询查询
     * @param categoryId
     * @return
     */
    @GetMapping("/categoryId")
    public R<List<Material>> selectByCategoryId(Long categoryId){
        //添加条件构造器
        LambdaQueryWrapper<Material> lqw=new LambdaQueryWrapper<>();
        //添加categoryId条件
        lqw.eq(Material::getCategoryId,categoryId).eq(Material::getIsSubmit,0).eq(Material::getIsDeleted,0);
        //添加按创建时间排序
        lqw.orderByAsc(Material::getCreateTime);

        List<Material> list=materialService.list(lqw);
        return R.success(list);
    }

    /**
     * 按材料名称查询
     * @param name
     * @return
     */
    @GetMapping("/name")
    public R<List<Material>> selectByName(String name,String status){
        //添加条件构造器
        LambdaQueryWrapper<Material> lqw=new LambdaQueryWrapper<>();
        //添加categoryId条件,未删除，未入库，
        if(status==null){
            lqw.like(Material::getName,name).eq(Material::getIsSubmit,0).eq(Material::getIsDeleted,0);
        }else{
            lqw.like(Material::getName,name).eq(Material::getStatus,1).eq(Material::getIsDeleted,0);
        }
        //添加时间升序条件
        lqw.orderByAsc(Material::getCreateTime);
        List<Material> list=materialService.list(lqw);
        return R.success(list);
    }
}
