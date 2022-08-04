package com.liuwy.aspect;

import cn.hutool.core.util.StrUtil;
import com.liuwy.context.WebContext;
import com.liuwy.exception.SpikeException;
import com.liuwy.util.JwtTokenUtil;
import com.liuwy.util.SpikeConstant;
import java.util.Objects;
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

/**
 * AOP方式鉴权
 *
 * @author:
 * @date: created in 9:34 2021/4/26
 * @version:
 */

//放开注释使用切面校验权限
//@Component
//@Aspect
public class AuthAspect {

  @Autowired
  private RedisTemplate redisTemplate;

  //定义一个 Pointcut, 使用切点表达式函数来描述对哪些Join point使用advice.
  @Pointcut("@annotation(com.liuwy.annotation.AuthChecker)")
  public void AuthPointCut() {

  }

  //定义advice
  @Around("AuthPointCut()")
  public Object checkAuth(ProceedingJoinPoint proceedingJoinPoint) {
    HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    if (!authenticated(servletRequest)) {
      clearThreadContext();
      throw new SpikeException("用户未登录");
    }
    try {
      return proceedingJoinPoint.proceed();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      return null;
    }
  }

  public Boolean authenticated(HttpServletRequest servletRequest) {
    String token = servletRequest.getHeader("Authorization");
    if (StrUtil.isEmpty(token)) {
      throw new SpikeException("登录已过期，请重新登录！");
    }
    JwtTokenUtil.parseTokenInfo(token);
    return checkToken(token);
  }

  private boolean checkToken(String token) {
    if (WebContext.getIsExpired()) {
      return false;
    }
    if (WebContext.getCurrentUser() == null) {
      return false;
    }
    Object res = redisTemplate.opsForValue()
        .get(String.format(SpikeConstant.USER_TOKEN_KEY, WebContext.getCurrentUser()));
    if (Objects.isNull(res)) {
      return false;
    }
    if (!StrUtil.equals((String) res, token)) {
      return false;
    }
    return true;
  }

  private void clearThreadContext() {
    WebContext.removeCurrentUser();
    WebContext.removeIsExpired();
  }
}