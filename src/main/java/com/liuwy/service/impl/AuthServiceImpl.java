package com.liuwy.service.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.liuwy.exception.SpikeException;
import com.liuwy.service.AuthService;
import com.liuwy.util.RedisService;
import com.liuwy.util.SpikeConstant;

import cn.hutool.core.util.StrUtil;

/**
 * @author:
 * @date: created in 13:49 2021/4/25
 * @version:
 */

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    RedisService redisService;
    @Override
    public String createToken() {
        UUID randomUUID = UUID.randomUUID();
        StringBuffer stringBuffer = new StringBuffer();
        String token = stringBuffer.append(SpikeConstant.AUTH_PRE).append(randomUUID).toString();
        boolean result = redisService.setExp(token, token, 120L);
        if (result){
            return token;
        }
        return null;
    }

    @Override
    public boolean checkToken(HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
            if (StrUtil.isBlank(token)) {
                throw new SpikeException("请求违法！");
            }
        }
        if (!redisService.isExist(token)) {
            throw new SpikeException("token不存在！");
        }
        if (!redisService.remove(token)) {
            throw new SpikeException("token清理失败！");
        }
        return true;
    }
}