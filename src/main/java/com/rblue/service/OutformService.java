package com.rblue.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rblue.dto.InformDto;
import com.rblue.dto.OutformDto;
import com.rblue.entity.Outform;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface OutformService extends IService<Outform> {

    public String saveOutform(OutformDto outformDto);

    public Page<OutformDto> selectAll(Long id, int page, int pageSize , String beginTime, String endTime, String isSubmit, String checkStatus,String orderByTime);

    //查询单条记录
    public OutformDto selectById(Long id);

    //更新
    public String updateOutform(OutformDto outformDto);

    //提交出库单
    public String submitOutform(Long[] ids);


    //返回材料到仓库
    public String back(OutformDto outformDto);

    //下载出库单
    public void download(Long id, HttpServletResponse response, String haveBack) throws IOException;
}
