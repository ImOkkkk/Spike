package com.liuwy.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.liuwy.exception.SpikeException;

import cn.hutool.core.util.StrUtil;

/**
 * AOP方式鉴权
 *
 * @author:
 * @date: created in 9:34 2021/4/26
 * @version:
 */

@Component
@Aspect
public class AuthAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    //定义一个 Pointcut, 使用切点表达式函数来描述对哪些Join point使用advice.
    @Pointcut("@annotation(com.liuwy.annotation.AuthChecker)")
    public void AuthPointCut(){

    }

    //定义advice
    @Around("AuthPointCut()")
    public Object checkAuth(ProceedingJoinPoint proceedingJoinPoint){
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = getToken(servletRequest);
        if (!redisTemplate.hasKey(token)) {
            throw new SpikeException("token不存在！");
        }
        /*if (!redisService.remove(token)) {
            throw new SpikeException("token清理失败！");
        }*/
        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public String getToken(HttpServletRequest servletRequest){
        String token = servletRequest.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = servletRequest.getParameter("token");
            if (StrUtil.isBlank(token)) {
                throw new SpikeException("请求违法！");
            }
        }
        return token;
    }
}