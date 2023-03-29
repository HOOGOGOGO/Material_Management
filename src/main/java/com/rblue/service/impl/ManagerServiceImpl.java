package com.rblue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.entity.Manager;
import com.rblue.mapper.ManagerMapper;
import com.rblue.service.ManagerService;
import org.springframework.stereotype.Service;

@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerMapper, Manager> implements ManagerService {


}
