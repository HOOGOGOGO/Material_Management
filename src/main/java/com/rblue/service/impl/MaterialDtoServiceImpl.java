package com.rblue.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.dto.CategoryDto;
import com.rblue.dto.MaterialDto;
import com.rblue.entity.Category;
import com.rblue.entity.Warehouse;
import com.rblue.mapper.MaterialDtoMapper;
import com.rblue.service.MaterialDtoService;
import com.rblue.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MaterialDtoServiceImpl extends ServiceImpl<MaterialDtoMapper, MaterialDto> implements MaterialDtoService {

    @Autowired
    private MaterialDtoMapper materialDtoMapper;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Page<MaterialDto> selectAllByNameAndCategoryName(int page, int pageSize, MaterialDto materialDto) {
        //当前页的材料列表
        List<MaterialDto> rows = new ArrayList<>();
        //计算总记录数
        Integer totalCount = 0;
        if (materialDto.getWarehouseName() == null && materialDto.getName() == null && materialDto.getCategoryDto().getCategoryName() == null) {

            //先从redis中获取缓存数据
            //设置当前页和页大小构成key
            String materialKey = "material_" + page + "_" + pageSize+"_"+materialDto.getStatus();//material_1_8
            String totalKey="material_total_" + page + "_" + pageSize+"_"+materialDto.getStatus();//material_total_1_8
            //如果存在无需查询数据库,直接从缓存中获取
            rows = (List<MaterialDto>) redisTemplate.opsForValue().get(materialKey);
            totalCount= (Integer) redisTemplate.opsForValue().get(totalKey);
            if (rows != null&&totalCount !=0) {//存在不用处理

            } else {//如果不存在，需要查询数据库，将查询结果缓存到redis中
                //begin为
                int begin = (page - 1) * pageSize;
                //按照材料分类名和材料名称查询
                if (materialDto.getWarehouseName() == null) {
                    rows = materialDtoMapper.selectAllByNameAndCategoryName(begin, pageSize, materialDto);
                }
                //遍历所有的材料，再查询所有的仓库存储信息
                rows = rows.stream().map(item -> {
                    MaterialDto dto = new MaterialDto();
                    //复制到新的dto,准备做查询
                    BeanUtils.copyProperties(item, dto);
                    //获取仓库ID
                    Long id = item.getWarehouseId();
                    //根据id 查询仓库信息
                    Warehouse warehouse = warehouseService.getById(id);
                    //将仓库信息复制到dto中
                    dto.setWarehouse(warehouse);
                    return dto;
                }).collect(Collectors.toList());

                if (materialDto.getWarehouseName() == null) {
                    totalCount = materialDtoMapper.selectTotalByCondition(materialDto);
                } else {
                    totalCount = materialDtoMapper.selectTotalByConditionAndWarehouse(materialDto);
                }

                redisTemplate.opsForValue().set(materialKey,rows,60, TimeUnit.MINUTES);//60分钟
                redisTemplate.opsForValue().set(totalKey,totalCount,60,TimeUnit.MINUTES);
            }
        }else{//此时做了条件查询，不从redis查询，直接查询数据库
            if (materialDto.getName() != null && materialDto.getName().length() > 0) {
                materialDto.setName("%" + materialDto.getName() + "%");
            }
            if (materialDto.getCategoryDto().getCategoryName() != null && materialDto.getCategoryDto().getCategoryName().length() > 0) {
                String categoryName = "%" + materialDto.getCategoryDto().getCategoryName() + "%";
                CategoryDto category = new CategoryDto();
                category.setCategoryName(categoryName);
                materialDto.setCategoryDto(category);
            }
            if (materialDto.getWarehouseName() != null && materialDto.getWarehouseName().length() > 0) {
                String name = "%" + materialDto.getWarehouseName() + "%";
                materialDto.setWarehouseName(name);
            }
            //begin为
            int begin = (page - 1) * pageSize;
            //按照材料分类名和材料名称查询
            if (materialDto.getWarehouseName() == null) {
                rows = materialDtoMapper.selectAllByNameAndCategoryName(begin, pageSize, materialDto);
            }
            if (materialDto.getWarehouseName() != null) {
                rows = materialDtoMapper.selectAllByNameAndCategoryNameAndWarehouse(begin, pageSize, materialDto);
            }
            //遍历所有的材料，再查询所有的仓库存储信息
            rows = rows.stream().map(item -> {
                MaterialDto dto = new MaterialDto();
                //复制到新的dto,准备做查询
                BeanUtils.copyProperties(item, dto);
                //获取仓库ID
                Long id = item.getWarehouseId();
                //根据id 查询仓库信息
                Warehouse warehouse = warehouseService.getById(id);
                //将仓库信息复制到dto中
                dto.setWarehouse(warehouse);
                return dto;
            }).collect(Collectors.toList());

            if (materialDto.getWarehouseName() == null) {
                totalCount = materialDtoMapper.selectTotalByCondition(materialDto);
            } else {
                totalCount = materialDtoMapper.selectTotalByConditionAndWarehouse(materialDto);
            }
        }

        Page<MaterialDto> dtoPage = new Page<>(page, pageSize);
        dtoPage.setTotal(totalCount);
        dtoPage.setRecords(rows);

        return dtoPage;

    }

    @Override
    public int selectTotal() {
        return 0;
    }


}
