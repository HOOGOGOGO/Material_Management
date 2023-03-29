package com.rblue.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//材料分类管理
@Data
public class CategoryDto implements Serializable {
    private Long categoryId;

    private String categoryName;

    private String categoryDescription;


    private LocalDateTime categoryCreateTime;


    private LocalDateTime categoryUpdateTime;


    private Long categoryCreateUser;

    private Long categoryUpdateUser;

    //是否删除
    private Integer categoryIsDeleted;
}
