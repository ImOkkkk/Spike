package com.liuwy.util;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author:
 * @date: created in 10:06 2021/4/16
 * @version:
 */

@Component
public class UtilInit implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(UtilInit.class);

    @Autowired
    private MessageSource messageSource;

    @Value("${spring.kafka.template.default-topic:SPIKE_TOPIC}")
    private String kafkaTopic;

    @Override
    public void run(ApplicationArguments args) {
        LocaleMessageSourceUtil.messageSource = this.messageSource;
        logger.info("init in18....");
    }
}