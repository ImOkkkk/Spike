package com.liuwy.listener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.liuwy.config.KafkaConfig;
import com.liuwy.pojo.Stock;
import com.liuwy.task.CreateOrderTask;

/**
 * @author: liuwy
 * @date: created in 19:58 2021/4/19
 * @version:
 */
@Component
public class ConsumerListener {
    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    @Qualifier("stockThreadPool")
    private ThreadPoolExecutor stockThreadPool;

    @Autowired
    @Qualifier("scheduledThreadPool")
    private ScheduledThreadPoolExecutor scheduledThreadPool;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * commitSize:每次提交的对象数
     */
    @Value("${spike.common.receive.commitSize:100}")
    private int commitSize;

    @PostConstruct
    public void consume() {
        KafkaConsumer<String, String> consumer = kafkaConfig.getConsumer("SPIKE_TOPIC");
        scheduledThreadPool.scheduleAtFixedRate(() -> {
            List<Stock> stockList = Lists.newArrayList();
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                String value = record.value();
                Stock stock = JSON.parseObject(value.toString(), Stock.class);
                stockList.add(stock);
            }
            if (!stockList.isEmpty()) {
                try {
                    doTask(stockList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 10, 1000, TimeUnit.MILLISECONDS);
    }

    private void doTask(List<Stock> stockList) throws InterruptedException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        List<List<Stock>> partition = Lists.partition(stockList, commitSize);
        partition.forEach(list -> {
            tasks.add(new CreateOrderTask(sqlSessionFactory, commitSize, list));
        });
        stockThreadPool.invokeAll(tasks);
    }
}