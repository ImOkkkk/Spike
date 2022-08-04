package com.liuwy.controller;

import com.liuwy.annotation.AuthChecker;
import com.liuwy.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liuwy.exception.SpikeException;
import com.liuwy.pojo.Stock;
import com.liuwy.service.AuthService;
import com.liuwy.service.StockService;
import com.liuwy.util.SpikeResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author:
 * @date: created in 20:47 2021/4/15
 * @version:
 */

@Api("商品秒杀相关接口")
@RestController
@RequestMapping("/stock")
public class StockController {
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockService stockService;
    @Autowired
    private AuthService authService;

    @ApiOperation("获取token")
    @GetMapping("/createToken")
    public SpikeResponse createToken() {
        String token = authService.createToken();
        return SpikeResponse.successWithData(token);
    }

    @ApiOperation("预热Redis")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/redisPreheat")
    @AuthChecker
    public SpikeResponse RedisPreheat(@RequestParam(name = "id") String id) {
        stockService.redisPreheat(id);
        return SpikeResponse.success();
    }

    @ApiOperation("秒杀之前的准备")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "商品id"),
        @ApiImplicitParam(name = "count", value = "用于秒杀的数量")})
    @GetMapping("/initBefore")
//    @AuthChecker
    public SpikeResponse initBefore(String id, Integer count) {
        stockService.initStockBefore(id, count);
        return SpikeResponse.success();
    }

    @ApiOperation("创建商品")
    @PostMapping("/createStock")
//    @AuthChecker
    public SpikeResponse createStock(@RequestBody Stock stock){
        stock = stockService.createStock(stock);
        return SpikeResponse.successWithData(stock);
    }

    @ApiOperation("查询商品详情")
    @GetMapping("/getStockByCode")
//    @AuthChecker
    public SpikeResponse getStockByCode(String code){
        Stock stock = stockService.getStockByCode(code);
        return SpikeResponse.successWithData(stock);
    }

    @ApiOperation("查询商品详情")
    @GetMapping("/getStockById")
    public SpikeResponse getStockById(String id){
        Stock stock = stockService.getStockById(id);
        return SpikeResponse.successWithData(stock);
    }

    @ApiOperation("抢购商品，生成订单")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/sale")
    @RateLimiter(key = "RATE_LIMIT:SALE", time = 1, count = 1000)
    public SpikeResponse sale(@RequestParam(name = "id") String id) {
        try {
            stockService.sale(id);
        } catch (SpikeException e) {
            logger.error("Exception: " + e.getMessage());
        }
        return SpikeResponse.success();
    }

}