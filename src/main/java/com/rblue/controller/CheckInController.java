package com.rblue.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rblue.common.BaseContext;
import com.rblue.common.R;
import com.rblue.dto.InformDto;
import com.rblue.entity.*;
import com.rblue.service.InformMaterialService;
import com.rblue.service.InformService;
import com.rblue.service.ManagerService;
import com.rblue.service.MaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Line;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/checkIn")
public class CheckInController {
    @Autowired
    private InformService informService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private InformMaterialService informMaterialService;

    @Autowired
    private MaterialService materialService;

    @GetMapping("/page")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public R<Page> getAllInform(Long id, int page, int pageSize , String beginTime, String endTime,String checkStatus,String storageStatus,String orderTime) {
        //创建分页构造
        Page<Inform> informPage = new Page<>(page, pageSize);
        Page<InformDto> dtoPage = new Page<>();
        //创建条件构造器
        LambdaQueryWrapper<Inform> lqw = new LambdaQueryWrapper<>();
        //入库单id是否存在
        lqw.like(id != null, Inform::getId, id);
        //审核情况是否存在
        if(checkStatus!=null) {
            lqw.ne(checkStatus.equals("未审核"), Inform::getCheckStatus, 1);//要求显示未审核的
            lqw.eq(checkStatus.equals("已审核"), Inform::getCheckStatus, 1); //要求显示已审核的
        }
        //入库情况是否存在
        if(storageStatus!=null) {
            lqw.eq(storageStatus.equals("未入库"), Inform::getStorageStatus, 0);//要求显示未入库的
        }
        //时间段是否存在
        if(beginTime!=null&&endTime!=null){
            //转换成日期型
            LocalDateTime bTime= LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime eTime= LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lqw.gt(Inform::getCreateTime,bTime).lt(Inform::getCreateTime,eTime);
        }
        //添加排序条件,默认按紧急程度排序
        if(orderTime==null){
            lqw.orderByDesc(Inform::getUrgentStatus);
        }else{ //按时间降序
            lqw.orderByDesc(Inform::getCreateTime);
        }
        //添加已提交、未删除条件
        lqw.eq(Inform::getIsSubmit,1).eq(Inform::getIsDeleted,0);
        //得到查询结果
        informService.page(informPage, lqw);
        //复制
        BeanUtils.copyProperties(informPage, dtoPage, "records");
        //遍历查询结果获取创建人id并查看名字，插入到informDto中
        List<InformDto> dtoList = informPage.getRecords().stream().map(item -> {
            //创建dto对象
            InformDto dto = new InformDto();
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

    @PutMapping("/inform")
    public R<String> checkInformOne(@RequestBody Inform inform){
    if(inform!=null){
            //设置审核人id
            inform.setCheckId(BaseContext.getCurrentId());
            if(inform.getCheckStatus()==2){ //若审核未通过需要解除提交状态,并把材料全部解除提交状态
                inform.setIsSubmit(0);
                //根据入库单号查找全部材料
                //创建条件
                LambdaQueryWrapper<InformMaterial> lqw=new LambdaQueryWrapper<>();
                lqw.eq(InformMaterial::getInformId,inform.getId());
                List<InformMaterial> informMaterialList=informMaterialService.list(lqw);
                //遍历材料
                for(InformMaterial informMaterial:informMaterialList){
                    Long materialId=informMaterial.getMaterialId();
                    //创建条件
                    Material material=new Material();
                    //设置id和将材料设为未提交
                    material.setId(materialId);
                    material.setIsSubmit(0);
                    materialService.updateById(material);
                };
            }
            informService.updateById(inform);
        }
        return R.success("审核完成");
    }
    @PutMapping("/inform/{ids}")
    public R<String> checkInformMore(@PathVariable Long[] ids){
        if(ids.length>0){
            //获取所有需要审核的informId
            for(Long informId:ids){
                //修改审核信息
                Inform in=new Inform();
                //设置审核人id，审核状态
                in.setId(informId);
                in.setCheckStatus(1);
                in.setCheckId(BaseContext.getCurrentId());
                informService.updateById(in);
            }
        }
        return R.success("审核完成");
    }
}
