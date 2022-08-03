package com.liuwy.service.impl;

import java.util.UUID;

import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.liuwy.exception.SpikeException;
import com.liuwy.service.AuthService;
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
    private RedisTemplate redisTemplate;
    @Override
    public String createToken() {
        UUID randomUUID = UUID.randomUUID();
        StringBuffer stringBuffer = new StringBuffer();
        String token = stringBuffer.append(SpikeConstant.AUTH_PRE).append(randomUUID).toString();
        redisTemplate.opsForValue().set(token, token, 600, TimeUnit.SECONDS);
        return token;
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
        if (!redisTemplate.hasKey(token)) {
            throw new SpikeException("token不存在！");
        }
        if (!redisTemplate.hasKey(token)) {
            throw new SpikeException("token清理失败！");
        }
        return true;
    }
}