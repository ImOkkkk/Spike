package com.liuwy.service.impl;
import com.liuwy.annotation.DoubleCache;
import com.liuwy.dao.StockDao;
import com.liuwy.dao.StockOrderDao;
import com.liuwy.enums.CacheType;
import com.liuwy.pojo.Stock;
import com.liuwy.pojo.StockOrder;
import com.liuwy.service.StockService;
import com.liuwy.util.LocaleMessageSourceUtil;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author:
 * @date: created in 20:56 2021/4/15
 * @version:
 */
@Service
public class StockServiceImpl implements StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    @Autowired
    private StockDao stockDao;
    @Autowired
    private StockOrderDao stockOrderDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    @Qualifier("deductStockScript")
    private DefaultRedisScript deductStockScript;
    @Resource(name = "orderSendQueue")
    private BlockingQueue<Object> queue;

    @Override
    @Transactional
    public Stock redisPreheat(String id) {
        Stock stock = stockDao.selectStockById(id);
        if (stock == null) {
            LocaleMessageSourceUtil.getMessage("spike.stock.no.exist.stock");
        }
        redisTemplate.opsForHash().put(id, "total", stock.getCount());
        redisTemplate.opsForHash().put(id, "ordered", 0);
        return stock;
    }

    @Override
    @Transactional
    public void initStockBefore(String id, Integer count) {
        //准备商品
        stockDao.initStockBefore(id, count);
        //清理订单
        stockOrderDao.initOrderBefore(id);
        //重置redis缓存
        redisPreheat(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DoubleCache(cacheName = "order", key = "#stock.id", TYPE = CacheType.ADD)
    public Stock createStock(Stock stock) {
        stockDao.createStock(stock);
        return stock;
    }

    @Override
    @DoubleCache(cacheName = "order", key = "#code", TYPE = CacheType.FULL)
    public Stock getStockByCode(String code) {
        return stockDao.selectStockByCode(code);
    }

    @Override
    @DoubleCache(cacheName = "order", key = "#id", TYPE = CacheType.FULL)
    public Stock getStockById(String id) {
        return stockDao.selectStockById(id);
    }

    @Override
    public void sale(String id) {
        // Redis分布式锁
        RLock lock = redissonClient.getLock("LOCK:SALE");
        // 锁5秒后自动释放，防止死锁
        try {
            boolean hasLock = lock.tryLock(5, TimeUnit.SECONDS);
            if (hasLock){
                try {
                    //扣减库存
                    Long number = (Long) redisTemplate.execute(deductStockScript,
                        Collections.singletonList(id), 1);
                    if (number!= null && number.intValue() == 1){
                        //异步下单
                        StockOrder order = new StockOrder();
                        order.setSid(Integer.valueOf(id));
                        order.setCreateTime(new Date());
                        queue.offer(order);
                    }else {
                        //库存不足
                        LocaleMessageSourceUtil.getMessage("spike.stock.out.of.stock");
                    }
                }finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}