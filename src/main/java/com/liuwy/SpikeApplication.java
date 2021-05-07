package com.liuwy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author:
 * @date: created in 20:27 2021/4/15
 * @version:
 */

@SpringBootApplication
@EnableAspectJAutoProxy
public class SpikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpikeApplication.class);
    }
}