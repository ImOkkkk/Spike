package com.liuwy.aspect;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.liuwy.annotation.DoubleCache;
import com.liuwy.enums.CacheType;
import com.liuwy.pojo.Stock;
import com.liuwy.util.SpelUtil;

import lombok.AllArgsConstructor;

/**
 * @author ImOkkkk
 * @date 2022/4/4 20:14
 * @since 1.0
 */
@Component
@Aspect
@AllArgsConstructor
public class DoubleCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleCacheAspect.class);

    @Autowired
    private Cache cache;

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.liuwy.annotation.DoubleCache)")
    public void doubleCacheAspect() {

    }

    @Around("doubleCacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        Method method = signature.getMethod();

        // 拼接解析springEl表达式的map
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            treeMap.put(paramNames[i], args[i]);
        }

        DoubleCache annotation = method.getAnnotation(DoubleCache.class);
        String elResult = SpelUtil.parse(annotation.key(), treeMap);
        String realKey = annotation.cacheName() + elResult;
        if (annotation.TYPE() == CacheType.ADD){
            // 先执行方法，生成id
            Object object = point.proceed();
            if (Objects.nonNull(object)){
                elResult = SpelUtil.parse(annotation.key(), treeMap);
                realKey = annotation.cacheName() + elResult;
                // 写入Redis
                redisTemplate.opsForValue().set(realKey, JSON.toJSONString(object), annotation.redisTimeOut(),
                        TimeUnit.SECONDS);
                // 写入Caffeine
                cache.put(realKey, object);

                return object;
            }
        }else if (annotation.TYPE() == CacheType.PUT) {
            // 强制更新
            Object object = point.proceed();
            redisTemplate.opsForValue().set(realKey, JSON.toJSONString(object), annotation.redisTimeOut(), TimeUnit.SECONDS);
            cache.put(realKey, object);
            return object;
        } else if (annotation.TYPE() == CacheType.DELETE) {
            // 删除
            redisTemplate.delete(realKey);
            cache.invalidate(realKey);
            return point.proceed();
        }

        // 读写，查询Caffeine
        Object caffeineCache = cache.getIfPresent(realKey);
        if (Objects.nonNull(caffeineCache)) {
            LOGGER.info("get data from caffeine");
            return caffeineCache;
        }

        // 查询Redis
        Object redisCache = redisTemplate.opsForValue().get(realKey);
        if (Objects.nonNull(redisCache)) {
            redisCache = JSON.parseObject(redisCache.toString(), Stock.class);
            LOGGER.info("get data from redis");
            cache.put(realKey, redisCache);
            return redisCache;
        }

        LOGGER.info("get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            // 写入Redis
            redisTemplate.opsForValue().set(realKey, JSON.toJSONString(object), annotation.redisTimeOut(),
                TimeUnit.SECONDS);
            // 写入Caffeine
            cache.put(realKey, object);
        }
        return object;
    }
}
