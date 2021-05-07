package com.liuwy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liuwy.annotation.AuthChecker;
import com.liuwy.exception.SpikeException;
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
    public SpikeResponse RedisPreheat(String id) {
        stockService.redisPreheat(id);
        return SpikeResponse.success();
    }

    @ApiOperation("秒杀之前的准备")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "商品id"),
        @ApiImplicitParam(name = "count", value = "用于秒杀的数量")})
    @GetMapping("/initBefore")
    @AuthChecker
    public SpikeResponse initBefore(String id, Integer count) {
        stockService.initStockBefore(id, count);
        return SpikeResponse.success();
    }

    @ApiOperation("通过乐观锁的方式秒杀")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/saleByOptimistic")
    public SpikeResponse saleByOptimistic(String id) {
        try {
            stockService.saleByOptimistic(id);
        } catch (SpikeException e) {
            logger.error("Exception: " + e.getMessage());
        }
        return SpikeResponse.success();
    }

    @ApiOperation("通过乐观锁和Redis限流的方式秒杀")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/saleByOptimisticLimit")
    public SpikeResponse saleByOptimisticLimit(String id, Integer limit) {
        try {
            stockService.saleByOptimisticLimit(id, limit);
        } catch (SpikeException e) {
            logger.error("Exception: " + e.getMessage());
        }
        return SpikeResponse.success();
    }

    @ApiOperation("通过乐观锁和Redis校验库存的方式秒杀")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/saleByOptimisticRedis")
    public SpikeResponse saleByOptimisticRedis(String id) {
        try {
            stockService.saleByOptimisticRedis(id);
        } catch (SpikeException e) {
            logger.error("Exception: " + e.getMessage());
        }
        return SpikeResponse.success();
    }

    /*
     *不保证库存正确，只是用来练习使用Kafka和多线程
     */
    @ApiOperation("通过乐观锁和Redis限流及Kafka的方式秒杀")
    @ApiImplicitParam(name = "id", value = "商品id")
    @GetMapping("/saleByOptimisticLimitKafka")
    public SpikeResponse saleByOptimisticLimitKafka(String id, Integer limit) {
        try {
            stockService.saleByOptimisticLimitKafka(id, limit);
        } catch (SpikeException e) {
            logger.error("Exception: " + e.getMessage());
        }
        return SpikeResponse.success();
    }
}