package com.rblue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.entity.Outform;
import com.rblue.entity.OutformMaterial;
import com.rblue.mapper.OutformMapper;
import com.rblue.mapper.OutformMaterialMapper;
import com.rblue.service.OutformMaterialService;
import com.rblue.service.OutformService;
import org.springframework.stereotype.Service;

@Service
public class OutformMaterialServiceImpl extends ServiceImpl<OutformMaterialMapper, OutformMaterial> implements OutformMaterialService {
}
