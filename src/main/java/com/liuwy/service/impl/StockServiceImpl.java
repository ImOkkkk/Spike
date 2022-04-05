package com.liuwy.service.impl;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.liuwy.annotation.DoubleCache;
import com.liuwy.dao.StockDao;
import com.liuwy.dao.StockOrderDao;
import com.liuwy.enums.CacheType;
import com.liuwy.exception.SpikeException;
import com.liuwy.pojo.Stock;
import com.liuwy.pojo.StockOrder;
import com.liuwy.service.StockService;
import com.liuwy.util.LocaleMessageSourceUtil;
import com.liuwy.util.RedisService;
import com.liuwy.util.SpikeConstant;

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
    private RedisService redisService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource(name = "orderSendQueue")
    private BlockingQueue<Object> queue;
    @Autowired
    @Value("${spring.kafka.template.default-topic:SPIKE_TOPIC}")
    private String kafkaTopic;

    @Override
    @Transactional
    public Stock redisPreheat(String id) {
        Stock stock = stockDao.selectStockById(id);
        if (stock == null) {
            LocaleMessageSourceUtil.getMessage("spike.stock.no.exist.stock");
        }
        // 删除旧缓存
        redisService.remove(SpikeConstant.STOCK_COUNT + id);
        redisService.remove(SpikeConstant.STOCK_SALE + id);
        redisService.remove(SpikeConstant.STOCK_VERSION + id);
        // 缓存预热
        redisService.set(SpikeConstant.STOCK_COUNT + id, String.valueOf(stock.getCount()));
        redisService.set(SpikeConstant.STOCK_SALE + id, String.valueOf(stock.getSale()));
        redisService.set(SpikeConstant.STOCK_VERSION + id, String.valueOf(stock.getVersion()));
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
    @Transactional
    public void saleByOptimistic(String id) throws SpikeException {
        //校验库存
        Stock stock = checkStockCount(id);
        //乐观锁更新
        Integer updateCount = stockDao.updateByOptimistic(stock);
        if (updateCount == 0) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.fail.of.buy"));
        }
        //创建订单
        createOrder(stock);

    }

    @Override
    @Transactional
    public void saleByOptimisticLimit(String id, Integer limit) {
        if (redisService.limit(limit)) {
            Stock stock = checkStockCount(id);
            Integer updateCount = stockDao.updateByOptimistic(stock);
            if (updateCount == 0) {
                throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.fail.of.buy"));
            }
            createOrder(stock);
        }
    }

    @Override
    @Transactional
    public void saleByOptimisticRedis(String id) throws SpikeException {
        Stock stock = checkStockCountByRedis(id);
        // 乐观锁更新库存和Redis
        saleStockOptimsticWithRedis(stock);
        // 创建订单
        createOrder(stock);
    }

    @Override
    @Transactional
    public void saleByOptimisticLimitKafka(String id, Integer limit) throws SpikeException {
        if (redisService.limit(limit)) {
            Stock stock = checkStockCountByRedis(id);
            kafkaTemplate.send(kafkaTopic, JSONObject.toJSONString(stock));
            logger.info("kafka消息发送成功！");
            updateStockWithRedis(stock);
        } else {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.stock.buy.processing"));
        }
    }

    @Override
    public void createOrderByKafka(Stock stock) throws SpikeException {
        //乐观锁更新
        Integer updateCount = stockDao.updateByOptimistic(stock);
        if (updateCount == 0) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.fail.of.buy"));
        }
        Integer result = createOrder(stock);
        if (result == 1) {
            logger.info("Kafka 消费 Topic 创建订单成功");
        } else {
            logger.info("Kafka 消费 Topic 创建订单失败");
        }
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

    private void saleStockOptimsticWithRedis(Stock stock) throws SpikeException {
        int res = stockDao.updateByOptimistic(stock);
        if (res == 0) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.fail.update.stock"));
        }
        // 更新 Redis
        updateStockWithRedis(stock);
    }

    private void updateStockWithRedis(Stock stock) {
        redisService.decre(SpikeConstant.STOCK_COUNT + stock.getId());
        redisService.incre(SpikeConstant.STOCK_SALE + stock.getId());
        redisService.incre(SpikeConstant.STOCK_VERSION + stock.getId());
    }

    private Stock checkStockCountByRedis(String id) throws SpikeException {
        Integer count = Integer.parseInt((String)redisService.get(SpikeConstant.STOCK_COUNT + id));
        Integer sale = Integer.parseInt((String)redisService.get(SpikeConstant.STOCK_SALE + id));
        Integer version = Integer.parseInt((String)redisService.get(SpikeConstant.STOCK_VERSION + id));
        if (count < 1) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.stock.out.of.stock", count));
        }
        Stock stock = new Stock();
        stock.setId(Integer.valueOf(id));
        stock.setCount(count);
        stock.setSale(sale);
        stock.setVersion(version);
        stock.setName("手机");
        return stock;
    }

    private Stock checkStockCount(String id) throws SpikeException {
        Stock stock = stockDao.selectStockById(id);
        if (stock == null) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.stock.no.exist.stock"));
        }
        if (stock.getCount() < 1) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.stock.out.of.stock"),
                String.valueOf(stock.getCount()));
        }
        return stock;
    }

    /**
     * 创建订单
     */
    private Integer createOrder(Stock stock) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setCreateTime(new Date());
        Integer result = stockOrderDao.insertSelective(order);
        if (result == 0) {
            throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.fail.create.order"));
        }
        return result;
    }
}