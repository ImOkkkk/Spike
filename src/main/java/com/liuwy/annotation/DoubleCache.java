package com.liuwy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.liuwy.enums.CacheType;

/**
 * @author ImOkkkk
 * @date 2022/4/4 20:06
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleCache {
    String cacheName();

    String key();

    long redisTimeOut() default 120;

    CacheType TYPE() default CacheType.FULL;
}
