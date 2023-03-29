package com.rblue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.dto.CategoryDto;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Category;
import com.rblue.mapper.MaterialMapper;
import com.rblue.service.CategoryService;
import com.rblue.service.MaterialService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rblue.entity.Material;

import java.util.List;
import java.util.stream.Collectors;



@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    @Autowired
    private CategoryService categoryService;

    //查询未入库材料信息
    public Page NoInWarehouse(int page,int pageSize,String name){
        //分页构造器
        Page<Material> materialPage=new Page<>(page,pageSize);
        Page<MaterialDto> dtoPage=new Page<>();
        //添加条件
        LambdaQueryWrapper<Material> lqw=new LambdaQueryWrapper<>();
        //是否有查询材料名称
        lqw.like(name!=null,Material::getName,name);
        //查找未入库的材料
        lqw.eq(Material::getStatus,0).eq(Material::getIsDeleted,0);

        this.page(materialPage,lqw);

        //复制除材料信息外
        BeanUtils.copyProperties(materialPage,dtoPage,"records");

        List<MaterialDto> lists=materialPage.getRecords().stream().map(item->{
            //遍历材料信息，查询相应的分类信息，并插入到材料dto实体类中
            //获取分类id
            Long id=item.getCategoryId();
            //根据id查询分类信息
            Category category=categoryService.getById(id);
            //创建dto对象
            MaterialDto dto=new MaterialDto();
            //复制
            BeanUtils.copyProperties(item,dto);
            //创建CategoryDto对象
            CategoryDto categoryDto=new CategoryDto();
            categoryDto.setCategoryId(category.getId());
            categoryDto.setCategoryName(category.getName());
            categoryDto.setCategoryDescription(category.getDescription());
            dto.setCategoryDto(categoryDto);

            return dto;
        }).collect(Collectors.toList());
        //将记录存回
        dtoPage.setRecords(lists);
        return  dtoPage;
    }
}
