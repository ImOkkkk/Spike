package com.liuwy.config;

import com.liuwy.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器实现鉴权
 *
 * @author:
 * @date: created in 9:27 2021/4/26
 * @version:
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  /**
   * 直接放行路径
   */
  private final String[] EXCLUDE_PATHS = {
      "/stock/createToken"
  };

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(tokenInterceptor()).addPathPatterns("/**")
        .excludePathPatterns(EXCLUDE_PATHS);
  }

  @Bean
  public TokenInterceptor tokenInterceptor() {
    return new TokenInterceptor();
  }
}