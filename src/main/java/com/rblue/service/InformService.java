package com.rblue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rblue.dto.InformDto;
import com.rblue.entity.Inform;

public interface InformService extends IService<Inform> {
    //保存
    public void saveInform(InformDto indto);
    //更新
    public void updateInform(InformDto indto);


    //查询单个
    public InformDto selectById(Long id);

    //材料入库
    public String inWarehouse(InformDto informDto);
}
