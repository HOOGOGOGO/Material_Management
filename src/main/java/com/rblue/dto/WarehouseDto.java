package com.rblue.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.rblue.entity.Warehouse;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//材料分类管理
@Data
public class WarehouseDto extends Warehouse implements Serializable {

    //查询负责人姓名
    private String managerName;
}
