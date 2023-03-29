package com.rblue.dto;

import com.rblue.entity.Category;
import com.rblue.entity.Material;
import com.rblue.entity.Warehouse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MaterialDto extends Material implements Serializable {

    private Category category;

    private CategoryDto categoryDto;

    private Warehouse warehouse;

    private String warehouseName;//仓库名称

    private Double amount;//取出量

    private Integer number;//取出次数

    private Double backAmount; //退料量

}
