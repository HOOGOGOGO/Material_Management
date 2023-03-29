package com.rblue.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Outform implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer urgentStatus;//紧急状态

    private String outformDescription;//出库单描述

    private Integer isSubmit;//是否提交审核

    private Long checkId;//审核人id

    private Integer checkStatus;//审核状态

    private String checkDescription;//审核描述

    private Integer outStatus;//取出仓库状态

    private Integer isDeleted;//是否被删除

    private Integer backStatus;//是否退料

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


}
