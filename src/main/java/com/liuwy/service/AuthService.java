package com.liuwy.service;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    /**
     * 创建token
     * @return
     */
    String createToken();
    /**
     * 检验token
     * @param request
     * @return
     */
    boolean checkToken(HttpServletRequest request) throws Exception;
}
