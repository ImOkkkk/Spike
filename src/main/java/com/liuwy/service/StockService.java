package com.liuwy.service;

import com.liuwy.exception.SpikeException;
import com.liuwy.pojo.Stock;

/**
 * @author:
 * @date: created in 20:45 2021/4/15
 * @version:
 */
public interface StockService {
    /**
     * 预热Redis
     * @param id  商品id
     * @return
     */
    Stock redisPreheat(String id);

    /**
     * 秒杀之前的准备
     * @param id  商品id
     * @param count  用于秒杀的数量
     * @return
     */
    void initStockBefore(String id, Integer count);

    /**
     * 创建商品
     * @param stock
     */
    Stock createStock(Stock stock);

    /**
     * 根据商品code查询商品详情
     * @param code
     * @return
     */
    Stock getStockByCode(String code);

    /**
     * 根据商品id查询商品详情
     * @param id
     * @return
     */
    Stock getStockById(String id);

    /**
     * 抢购商品，生成订单
     * @param id
     */
    void sale(String id);
}