package com.liuwy.service.impl;

import com.liuwy.context.WebContext;
import com.liuwy.service.AuthService;
import com.liuwy.util.JwtTokenUtil;
import com.liuwy.util.SpikeConstant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        String token = cacheToken("userid1", "username1");
        WebContext.setCurrentUser("userid1");
        return token;
    }


    private String cacheToken(String userId, String userName) {
        String key = String.format(SpikeConstant.USER_TOKEN_KEY, userId);
        String res = null;
        Object r = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(r)) {
            res = (String) r;
            redisTemplate.boundValueOps(key).set(r, TimeUnit.DAYS.toSeconds(7), TimeUnit.SECONDS);
        } else {
            res = JwtTokenUtil.generateToken(userId, userName);
            redisTemplate.boundValueOps(key).set(res, TimeUnit.DAYS.toSeconds(7), TimeUnit.SECONDS);
        }
        return res;
    }
}