package com.liuwy.config;

import cn.hutool.core.util.StrUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ImOkkkk
 * @date 2022/5/10 14:14
 * @since 1.0
 */
@Configuration
public class RedissonConfig {

  @Value("${spring.redis.host}")
  private String address;

  @Value("${spring.redis.port}")
  private String port;

  @Value("${spring.redis.password:}")
  private String password;

  private static final String prefix = "redis://";

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    SingleServerConfig singleServerConfig = config.useSingleServer();
    singleServerConfig.setAddress(prefix + address + ":" +port);
    if (StrUtil.isNotBlank(password)) {
      singleServerConfig.setPassword(password);
    }
    RedissonClient redissonClient = Redisson.create(config);
    return redissonClient;
  }

}
