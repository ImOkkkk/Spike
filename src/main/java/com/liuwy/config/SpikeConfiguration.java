package com.liuwy.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author:
 * @date: created in 19:48 2021/4/26
 * @version:
 */
@Configuration
public class SpikeConfiguration {

    /**
     * 数据入库线程池注入.<br/>
     *
     * @author
     * @return
     */
    @Bean(destroyMethod = "shutdown", name = "stockThreadPool")
    public ThreadPoolExecutor stockThreadPool() {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("订单入库线程-%d").build();
        return new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), tf,
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(destroyMethod = "shutdown", name = "scheduledThreadPool")
    public ScheduledThreadPoolExecutor scheduledThreadPool() {
        ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("定时线程-%d").build();
        return new ScheduledThreadPoolExecutor(1, tf, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public BlockingQueue<Object> orderSendQueue() {
        return new ArrayBlockingQueue<Object>(30000);
    }

    @Bean("rateLimiterScript")
    public DefaultRedisScript<Long> rateLimiterScript(){
        DefaultRedisScript<Long> rateLimiterScript = new DefaultRedisScript<>();
        rateLimiterScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rateLimiter.lua")));
        rateLimiterScript.setResultType(Long.class);
        return rateLimiterScript;
    }

    @Bean("deductStockScript")
    public DefaultRedisScript<Long> deductStockScript(){
        DefaultRedisScript<Long> rateLimiterScript = new DefaultRedisScript<>();
        rateLimiterScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deductStock.lua")));
        rateLimiterScript.setResultType(Long.class);
        return rateLimiterScript;
    }
}