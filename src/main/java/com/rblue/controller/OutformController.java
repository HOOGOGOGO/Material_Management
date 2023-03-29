package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.R;
import com.rblue.dto.MaterialDto;
import com.rblue.dto.OutformDto;
import com.rblue.entity.*;
import com.rblue.service.ManagerService;
import com.rblue.service.MaterialService;
import com.rblue.service.OutformMaterialService;
import com.rblue.service.OutformService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/outform")
public class OutformController {

    @Autowired
    private ManagerService managerService;
    @Autowired
    private MaterialService materialService;

    @Autowired
    private OutformService outformService;

    @Autowired
    private OutformMaterialService outformMaterialService;


    /**
     * 查询仓库内的材料
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/categoryId")
    public R<List<Material>> selectByCategoryId(Long categoryId) {
        //添加条件构造器
        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        //添加categoryId条件:分类id条件和入库条件和未删除条件
        lqw.eq(Material::getCategoryId, categoryId).eq(Material::getStatus, 1).eq(Material::getIsDeleted, 0).gt(Material::getAvailable, 0);
        lqw.orderByAsc(Material::getCreateTime);
        List<Material> list = materialService.list(lqw);
        return R.success(list);

    }

    /**
     * 保存出库单
     *
     * @param outformDto
     * @return
     */
    @PostMapping("/save")
    public R<String> saveOutform(@RequestBody OutformDto outformDto) {
        String result = null;
        if (outformDto != null) {
            result = outformService.saveOutform(outformDto);

        }
        if (result.equals("success"))
            return R.success("填写成功");
        else
            return R.error(result);
    }

    /**
     * 查询所有的出库单
     *
     * @param id
     * @param page
     * @param pageSize
     * @param beginTime
     * @param endTime
     * @param isSubmit
     * @param checkStatus
     * @return
     */
    @GetMapping("/page")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public R<Page> getAllInform(Long id, int page, int pageSize, String beginTime, String endTime, String isSubmit, String checkStatus, String orderByTime) {
        Page<OutformDto> dtoPage = outformService.selectAll(id, page, pageSize, beginTime, endTime, isSubmit, checkStatus, orderByTime);
        return R.success(dtoPage);

    }

    /**
     * 查询单个出库记录
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<OutformDto> selectById(@PathVariable Long id) {
        if (id != null) {
            //根据id 查询信息
            OutformDto dto = outformService.selectById(id);
            return R.success(dto);
        }
        return R.error("系统繁忙，请重试");
    }

    /**
     * 更新出库单
     *
     * @param outformDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody OutformDto outformDto) {
        String result = outformService.updateOutform(outformDto);
        if (result.equals("success"))
            return R.success("修改成功");
        return R.error(result);
    }


    /**
     * 批量提交出库单
     *
     * @param ids
     * @return
     */
    @PutMapping("/submit/{ids}")
    public R<String> SubmitOutform(@PathVariable Long[] ids) {

        String result = outformService.submitOutform(ids);
        if (result.equals("success")) {
            return R.success("提交成功");
        }
        return R.error(result);
    }

    /**
     * 删除出库单记录
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> deleteOutform(@PathVariable Long id) {
        LambdaQueryWrapper<Outform> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Outform::getId, id);
        Outform outform = new Outform();
        outform.setIsDeleted(1);
        outformService.update(outform, lqw);
        //删除出库材料表
        LambdaQueryWrapper<OutformMaterial> lq = new LambdaQueryWrapper<>();
        lq.eq(OutformMaterial::getOutformId, id);
        outformMaterialService.remove(lq);

        return R.success("删除成功");

    }

    /**
     * 调整出库状态
     *
     * @param id
     * @return
     */
    @PutMapping("/outStatus/{id}")
    public R<String> updateOutStatus(@PathVariable Long id) {
        //添加条件
        if (id != null) {

            Outform outform = new Outform();
            outform.setId(id);
            //改为出库状态
            outform.setOutStatus(1);
            //更新记录
            outformService.updateById(outform);
            return R.success("出库成功");
        }
        return R.error("出库失败");
    }

    /**
     * 材料退库
     * @param outformDto
     * @return
     */
    @PutMapping("/back")
    public R<String> backMaterial(@RequestBody OutformDto outformDto) {
        String result = outformService.back(outformDto);
        if (!result.equals("success"))
            return R.error(result);
        return R.success("退料成功");
    }

    /**
     * 下载单个出库单
     *
     * @param id
     * @return
     */
    @GetMapping("/export")
    public void downloadById(Long id, HttpServletResponse response, String haveBack) throws IOException {
        //调用下载方法
        outformService.download(id, response, haveBack);
    }

    /**
     * 统计材料信息
     * @param ids
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/statistic")
    public R<Statistic> getAllOutformStatistic(Long[] ids, String beginTime, String endTime) {
        //处理时间
        //添加时间条件
        LambdaQueryWrapper<Outform> lqw = new LambdaQueryWrapper<>();
        if (beginTime != null && endTime != null) {
            LocalDateTime bTime = LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime eTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lqw.gt(Outform::getCreateTime, bTime).lt(Outform::getCreateTime, eTime);
        }
        List<Long> idList = new ArrayList<>();//暂存出库id集合
        List<Long> materialIdList = new ArrayList<>();
        if (ids != null) {
            materialIdList = Arrays.asList(ids);//将数组转为集合
        }
        List<Outform> outforms = outformService.list(lqw);
        //获取这个时间段的所有出库Id
        for (Outform outform : outforms) {
            idList.add(outform.getId());
        }
        //获取所有的出库材料单
        List<OutformMaterial> outformMaterialList = outformMaterialService.list();
        //获取所有出现的材料id，存储在材料id集合中
        Map<String, Double> map = new HashMap<>();//收集材料对应的出库量
        Map<String, Double> map2 = new HashMap<>();//收集材料对应的退料量
        List<Long> formList = new ArrayList<>();//记录统计的不重复表单id
        Integer count = 0;

        for (OutformMaterial outformMaterial : outformMaterialList) {
            if (materialIdList.size() > 0) {

                if (idList.contains(outformMaterial.getOutformId()) && materialIdList.contains(outformMaterial.getMaterialId())) { //只有出库单创建时间内在此段时间内,和所要求统计的材料才符合要求


                    if (!formList.contains(outformMaterial.getOutformId())) {//只有当前表单出现一次才统计，否则次数不增加
                        formList.add(outformMaterial.getOutformId());
                        count++;
                    }
                    //根据材料id设置map集合中每个材料对应的出库量和退料量
                    Material material = materialService.getById(outformMaterial.getMaterialId());//获取当前id的材料信息
                    String materialName = material.getName() + "(" + material.getUnit() + "，" + material.getSpecification() + ")";
                    if (!map.containsKey(materialName) && !map2.containsKey(materialName)) {//当前没有此材料的键
                        map.put(materialName, outformMaterial.getAmount());//插入对应的材料名称的出库量键值对
                        map2.put(materialName, outformMaterial.getBackAmount());

                    } else if (map.containsKey(materialName) && map2.containsKey(materialName)) {//当前有此材料的键，值等于原值+新的
                        map.replace(materialName, map.get(materialName) + outformMaterial.getAmount());
                        map2.replace(materialName, map2.get(materialName) + outformMaterial.getBackAmount());

                    }
                }

            }
            else{
                if (idList.contains(outformMaterial.getOutformId())) { //只有出库单创建时间内在此段时间内才符合要求


                    if (!formList.contains(outformMaterial.getOutformId())) {//只有当前表单出现一次才统计，否则次数不增加
                        formList.add(outformMaterial.getOutformId());
                        count++;
                    }
                    //根据材料id设置map集合中每个材料对应的出库量和退料量
                    Material material = materialService.getById(outformMaterial.getMaterialId());//获取当前id的材料信息
                    String materialName = material.getName() + "(" + material.getUnit() + "，" + material.getSpecification() + ")";
                    if (!map.containsKey(materialName) && !map2.containsKey(materialName)) {//当前没有此材料的键
                        map.put(materialName, outformMaterial.getAmount());//插入对应的材料名称的出库量键值对
                        map2.put(materialName, outformMaterial.getBackAmount());

                    } else if (map.containsKey(materialName) && map2.containsKey(materialName)) {//当前有此材料的键，值等于原值+新的
                        map.replace(materialName, map.get(materialName) + outformMaterial.getAmount());
                        map2.replace(materialName, map2.get(materialName) + outformMaterial.getBackAmount());

                    }
                }
            }
        } //此时集合中有所有的材料名字和对应总出库量、退料量

        //创建一个统计类对象，存储统计结果
        Statistic statistic = new Statistic();
        statistic.setMaterialNames(map.keySet());//设置材料名
        statistic.setSums(map.values());//设置对应的材料总量
        statistic.setBacks(map2.values());//设置对应的材料退料量
        statistic.setCount(count);
        return R.success(statistic);
    }


}
