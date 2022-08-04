package com.liuwy.interceptor;

import cn.hutool.core.util.StrUtil;
import com.liuwy.context.WebContext;
import com.liuwy.util.SpikeConstant;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author ImOkkkk
 * @date 2022/8/4 10:03
 * @since 1.0
 */
public class TokenInterceptor extends BaseInterceptor {

  @Autowired
  private RedisTemplate redisTemplate;

  @Override
  public boolean checkToken(HttpServletResponse response, String token) {
    if (WebContext.getIsExpired()) {
      setResponseBody(response, 1401,
          "登录过期");
      return false;
    }
    if (WebContext.getCurrentUser() == null) {
      setResponseBody(response, 2401,
          "用户未登录");
      return false;
    }
    Object res = redisTemplate.opsForValue()
        .get(String.format(SpikeConstant.USER_TOKEN_KEY, WebContext.getCurrentUser()));
    if (Objects.isNull(res)) {
      setResponseBody(response, 3401,
          "登录已过期，请重新登录!");
      return false;
    }
    if (!StrUtil.equals((String) res, token)) {
      setResponseBody(response, 4401,
          "登录用户不存在!");
      return false;
    }
    return true;
  }

}
