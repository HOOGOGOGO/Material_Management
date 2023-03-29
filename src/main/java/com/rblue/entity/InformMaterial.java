package com.rblue.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InformMaterial implements Serializable {

    //入库单材料表
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long informId; //订单号，自动生成

    private Long materialId; //材料id

}
