package com.rblue.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//材料分类管理
@Data
public class Material implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long categoryId;

    private String specification;


    private Double available;

    private String unit;

    private String storageDemand;

    private Double price;


    //将当前字段作为公共填充的部分，插入时填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //将当前字段作为公共填充的部分，插入、更新时填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //是否删除
    private Integer isDeleted;


    //提交状态
    private Integer isSubmit;

    //入库状态
    private Integer status;

    //存放仓库号
    private Long warehouseId;

    //货架号
    private String shelf;
}
