package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.BaseContext;
import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.dto.OutformDto;
import com.rblue.entity.*;
import com.rblue.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/checkOut")
public class CheckOutController {
    @Autowired
    private OutformService outformService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private OutformMaterialService outformMaterialService;

    @Autowired
    private MaterialService materialService;

    @GetMapping("/page")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public R<Page> getAllInform(Long id, int page, int pageSize , String beginTime, String endTime,String checkStatus,String outStatus,String orderByTime,String haveBack) {
        //创建分页构造
        Page<Outform> outformPage = new Page<>(page, pageSize);
        Page<OutformDto> dtoPage = new Page<>();
        //创建条件构造器
        LambdaQueryWrapper<Outform> lqw = new LambdaQueryWrapper<>();
        //入库单id是否存在
        lqw.like(id != null, Outform::getId, id);
        //审核情况是否存在
        if(checkStatus!=null) {
            lqw.ne(checkStatus.equals("未审核"), Outform::getCheckStatus, 1);//要求显示未审核的
            lqw.eq(checkStatus.equals("已审核"), Outform::getCheckStatus, 1); //要求显示已审核的,仓库管理员出库
        }
        //出库情况是否存在
        if(outStatus!=null) {
            lqw.eq(outStatus.equals("未出库"), Outform::getOutStatus, 0);//要求显示未出库的，仓库管理员
        }
        //时间段是否存在
        if(beginTime!=null&&endTime!=null){
            //转换成日期型
            LocalDateTime bTime= LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime eTime= LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lqw.gt(Outform::getCreateTime,bTime).lt(Outform::getCreateTime,eTime);
        }
        //退料情况是否存在
        if(haveBack!=null){
            lqw.eq(Outform::getBackStatus,1);
        }
       //添加排序条件,默认按紧急程度排序
        if(orderByTime==null){
            lqw.orderByDesc(Outform::getUrgentStatus);
        }else{ //按时间降序
            lqw.orderByDesc(Outform::getCreateTime);
        }
        //添加已提交、未删除条件
        lqw.eq(Outform::getIsSubmit,1).eq(Outform::getIsDeleted,0);
        //得到查询结果
        outformService.page(outformPage, lqw);
        //复制
        BeanUtils.copyProperties(outformPage, dtoPage, "records");
        //遍历查询结果获取创建人id并查看名字，插入到informDto中
        List<OutformDto> dtoList = outformPage.getRecords().stream().map(item -> {
            //创建dto对象
            OutformDto dto = new OutformDto();
            //复制
            BeanUtils.copyProperties(item, dto);
            //获取创建人id
            Long createUser = item.getCreateUser();
            //查询得到姓名
            Manager manager = managerService.getById(createUser);
            String userName = manager.getName();
            //加入姓名
            dto.setUserName(userName);
            return dto;
        }).collect(Collectors.toList());
        //将记录存回dtoPage
        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    @PutMapping("/outform")
    public R<String> checkInformOne(@RequestBody Outform outform){
    if(outform!=null){
            //设置审核人id
        outform.setCheckId(BaseContext.getCurrentId());
            if(outform.getCheckStatus()==2){ //若审核未通过需要解除提交状态,并把材料全部解除提交状态
                outform.setIsSubmit(0);
                //根据入库单号查找全部材料
                //创建条件
                LambdaQueryWrapper<OutformMaterial> lqw=new LambdaQueryWrapper<>();
                lqw.eq(OutformMaterial::getOutformId,outform.getId());
                List<OutformMaterial> outformMaterialList=outformMaterialService.list(lqw);
                //遍历材料
                for(OutformMaterial outformMaterial:outformMaterialList){
                    Long materialId=outformMaterial.getMaterialId();
                    //创建条件
                    Material material=new Material();
                    //设置id和将材料库存增加未提交
                    material.setId(materialId);
                    //根据id查询材料记录得到材料库存
                    material=materialService.getById(materialId);
                    material.setAvailable(outformMaterial.getAmount()+material.getAvailable());
                    materialService.updateById(material);
                };
            }
            outformService.updateById(outform);
        }
        return R.success("审核完成");
    }

    /**
     * 批量审核
     * @param ids
     * @return
     */
    @PutMapping("/outform/{ids}")
    public R<String> checkOutformMore(@PathVariable Long[] ids){
        if(ids.length>0){
            //创建出库单集合
            List<Outform> list=new ArrayList<>();
            //获取所有需要审核的informId
            for(Long id:ids){
                //修改审核信息
                Outform out=outformService.getById(id);
                if(out.getCheckStatus()==1){
                    return R.error("出库单："+id+"已经审核过，请勿重复提交");
                }
                //设置审核人id，审核状态
                out.setCheckStatus(1);
                out.setCheckId(BaseContext.getCurrentId());
                list.add(out);

            }
            outformService.updateBatchById(list);
        }
        return R.success("审核完成");
    }
}
