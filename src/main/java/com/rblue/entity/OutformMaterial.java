package com.rblue.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutformMaterial implements Serializable {

    //出库单材料表
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long outformId;//出库单Id

    private Long materialId; //材料Id

    private Double amount;//材料取出量

    private Integer number;//取出次数

    private Double backAmount; //退料量




}
