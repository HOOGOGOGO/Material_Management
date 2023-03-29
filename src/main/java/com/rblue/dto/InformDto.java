package com.rblue.dto;

import com.rblue.entity.Inform;
import com.rblue.entity.Material;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InformDto extends Inform implements Serializable {

    private String userName; //提交人的名字
    private List<MaterialDto> materials;//材料集合
}
