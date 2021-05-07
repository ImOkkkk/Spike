package com.liuwy.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

/**
 * @author:
 * @date: created in 12:53 2021/3/24
 * @version:
 */

@Component
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final int FAIL_CODE = 0;

    @Bean
    public DefaultRedisScript defaultRedisScript() {
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("limit.lua")));
        return defaultRedisScript;
    }

    /**
     * 写入缓存
     * 
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LocaleMessageSourceUtil.getMessage("spike.redis.set.error", key));
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     * 
     * @param key
     * @param value
     * @return
     */
    public boolean setExp(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LocaleMessageSourceUtil.getMessage("spike.redis.set.error", key));
        }
        return result;
    }

    /**
     * 判断缓存中是否有对应的value
     * 
     * @param key
     * @return
     */
    public boolean isExist(final String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean decre(final String key) {
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.decrement(key);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LocaleMessageSourceUtil.getMessage("spike.redis.set.error", key));
        }
        return result;
    }

    public Boolean incre(final String key) {
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.increment(key);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(LocaleMessageSourceUtil.getMessage("spike.redis.set.error", key));
        }
        return result;
    }

    /**
     * 读取缓存
     * 
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 删除对应的value
     * 
     * @param key
     */
    public boolean remove(final String key) {
        boolean result = false;
        if (isExist(key)) {
            result = redisTemplate.delete(key);
        }
        return result;
    }

    public Boolean limit(Integer limit) {
        // 解析 Lua 文件
        DefaultRedisScript redisScript = defaultRedisScript();
        // 请求限流
        String key = String.valueOf(System.currentTimeMillis() / 1000);
        // 计数限流
        Long executeResult = (Long)redisTemplate.execute(redisScript, Arrays.asList(key), String.valueOf(limit));
        if (FAIL_CODE != executeResult) {
            logger.info(LocaleMessageSourceUtil.getMessage("spike.success.get.token"));
            return true;
        }
        logger.info(LocaleMessageSourceUtil.getMessage("spike.fail.get.token"));
        return false;
    }
}