package com.liuwy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.liuwy.adapter.AuthInterceptor;

/**
 * @author:
 * @date: created in 9:27 2021/4/26
 * @version:
 */
@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {
    //构造器注入
    @Bean
    public AuthInterceptor authInterceptor(){
        return new AuthInterceptor();
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authInterceptor());
        super.addInterceptors(registry);
    }
}