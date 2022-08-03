package com.liuwy.pojo;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("商品订单")
public class StockOrder {
    @ApiModelProperty("商品订单id")
    private Integer id;
    @ApiModelProperty("商品id")
    private Integer sid;
    @ApiModelProperty("订单创建时间")
    private Date createTime;
}
