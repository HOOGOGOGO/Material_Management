package com.rblue.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//材料分类管理
@Data
public class Warehouse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String address;

    private Integer area;

    private String description;

    //负责人id
    private Long managerId;

    //将当前字段作为公共填充的部分，插入时填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //将当前字段作为公共填充的部分，插入、更新时填充
    @TableField(fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    //是否删除
    private Integer isDeleted;

    //状态
    private Integer status;
}
