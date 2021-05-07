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
     * 通过乐观锁和Redis限流的方式秒杀
     * @param id  商品id
     * @return
     */
    void saleByOptimistic(String id) throws SpikeException;

    /**
     * 通过乐观锁和Redis限流的方式秒杀
     * @param id  商品id
     * @return
     */
    void saleByOptimisticLimit(String id, Integer limit) throws SpikeException;

    /**
     * 通过乐观锁和Redis校验库存的方式秒杀
     * @param id  商品id
     * @return
     */
    void saleByOptimisticRedis(String id) throws SpikeException;

    /**
     * 通过乐观锁和Redis限流及Kafka的方式秒杀
     * @param id  商品id
     * @return
     */
    void saleByOptimisticLimitKafka(String id, Integer limit) throws SpikeException;

    void createOrderByKafka(Stock stock) throws SpikeException;
}