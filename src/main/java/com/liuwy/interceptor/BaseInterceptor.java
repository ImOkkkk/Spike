package com.liuwy.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.liuwy.annotation.AuthChecker;
import com.liuwy.context.WebContext;
import com.liuwy.exception.SpikeException;
import com.liuwy.util.JwtTokenUtil;
import com.liuwy.util.SpikeResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 拦截器实现鉴权
 *
 * @author:
 * @date: created in 17:02 2021/4/25
 * @version:
 */
public class BaseInterceptor extends HandlerInterceptorAdapter {

  @SneakyThrows
  static void setResponseBody(HttpServletResponse response, int errorCode, String msg) {
    response.setContentType("text/html;charset=utf-8");
    String jsonString = JSON.toJSONString(SpikeResponse.failWithMsg(msg));
    response.getWriter().write(jsonString);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    HandlerMethod handlerMethod = (HandlerMethod) handler;
    AuthChecker authChecker = handlerMethod.getMethodAnnotation(AuthChecker.class);
    if (authChecker != null) {
      if (!authenticated(request, response)) {
        clearThreadContext();
        return false;
      }
    }
    return true;
  }

  @SneakyThrows
  private boolean authenticated(HttpServletRequest request, HttpServletResponse response) {
    String token = request.getHeader("Authorization");
    if (StrUtil.isEmpty(token)) {
      throw new SpikeException("登录已过期，请重新登录！");
    }
    JwtTokenUtil.parseTokenInfo(token);
    return checkToken(response, token);
  }

  private void clearThreadContext() {
    WebContext.removeCurrentUser();
    WebContext.removeIsExpired();
  }

  boolean checkToken(HttpServletResponse response, String token) {
    return true;
  }
}