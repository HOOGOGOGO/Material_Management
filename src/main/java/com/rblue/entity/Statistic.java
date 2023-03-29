package com.rblue.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@Data
public class Statistic implements Serializable { //统计类

  private Integer count; //统计数量

  private Set<String> materialNames;//材料名称列表

  private Collection<Double> sums;//材料总出库量列表

  private Collection<Double> backs;//材料总出库量列表
}
