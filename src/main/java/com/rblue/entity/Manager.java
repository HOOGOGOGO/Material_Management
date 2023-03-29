package com.rblue.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Manager implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String sex;

    private String username;

    private String password;

    //用户身份
    private String job;

    private String phone;

    private String email;

    private String idNumber;

    private Integer status;

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
}
