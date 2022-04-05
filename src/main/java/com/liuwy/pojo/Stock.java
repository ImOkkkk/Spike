package com.liuwy.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("商品")
public class Stock {

    @ApiModelProperty("商品id")
    private Integer id;
    @ApiModelProperty("商品名称")
    private String name;
    @ApiModelProperty("商品剩余数量")
    private Integer count;
    @ApiModelProperty("商品已售数量")
    private Integer sale;
    @ApiModelProperty("商品版本")
    private Integer version;
    @ApiModelProperty("商品代码")
    private String code;
}
