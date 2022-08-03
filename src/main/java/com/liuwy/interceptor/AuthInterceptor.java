package com.liuwy.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.liuwy.annotation.AuthChecker;
import com.liuwy.exception.SpikeException;
import com.liuwy.service.AuthService;
import com.liuwy.util.LocaleMessageSourceUtil;

/**
 * 拦截器实现鉴权
 *
 * @author:
 * @date: created in 17:02 2021/4/25
 * @version:
 */
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        AuthChecker authChecker = handlerMethod.getMethodAnnotation(AuthChecker.class);
        if (authChecker != null) {
            try {
                return authService.checkToken(request);
            } catch (Exception e) {
                throw new SpikeException(LocaleMessageSourceUtil.getMessage("spike.check.token.fail"));
            }
        }
        return true;
    }
}