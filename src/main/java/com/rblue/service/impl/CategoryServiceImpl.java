package com.rblue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rblue.entity.Category;
import com.rblue.mapper.CategoryMapper;
import com.rblue.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
