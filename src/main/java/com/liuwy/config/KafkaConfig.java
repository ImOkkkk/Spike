package com.liuwy.config;

import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author:
 * @date: created in 19:50 2021/4/27
 * @version:
 */
@Configuration
@ConditionalOnProperty(name = {"spike.common.jms.type"}, havingValue = "kafka")
@EnableKafka
public class KafkaConfig {
    @Value("${spike.common.kafka.broker.address:127.0.0.1:9092}")
    private String brokerAddress;
    @Value("${spike.common.kafka.zookeeper.connect:127.0.0.1:2181}")
    private String zookeeperConnect;
    @Value("${spike.common.kafka.producer.acks:all}")
    private String acks;
    @Value("${spike.common.kafka.producer.batchSize:16384}")
    private String batchSize;
    @Value("${spike.common.kafka.producer.maxRequestSize:33554432}")
    private String maxRequestSize;
    @Value("${spike.common.kafka.producer.bufferMemory:33554432}")
    private String bufferMemory;
    @Value("${spike.common.kafka.producer.metadataRefresh:-1}")
    private String metadataRefresh;
    @Value("${spike.common.kafka.producer.retries:0}")
    private String retries;
    @Value("${spike.common.kafka.producer.metadata.max.age:120000}")
    private String metadataMaxAge;
    @Value("${spike.common.kafka.producer.max.block.ms:6000}")
    private String maxBlockMS;
    @Value("${spike.common.kafka.producer.compression.type:}")
    private String compressionType;
    @Value("${spike.common.kafka.consumer.groupId:spike}")
    private String groupId;
    @Value("${spike.common.kafka.consumer.auto.offset.reset:earliest}")
    private String offsetReset;
    @Value("${spike.common.kafka.consumer.autoCommitFlag:true}")
    private String autoCommitFlag;
    @Value("${spike.common.kafka.consumer.autoCommitInterval:1000}")
    private String autoCommitInterval;
    @Value("${spike.common.kafka.consumer.sessionTimeout:15000}")
    private String sessionTimeout;
    @Value("${spike.common.kafka.consumer.fetch.messageMaxBytes:52428800}")
    private String fetch_max_bytes;
    @Value("${spike.common.kafka.consumer.fetch.max.wait.ms:6000}")
    private String fetch_max_wait_ms;
    @Value("${spike.common.kafka.consumer.max.poll.interval.ms:120000}")
    private String max_poll_interval_ms;
    @Value("${spike.common.kafka.listener.concurrency:1}")
    private Integer kafka_listener_concurrency;
    @Value("${spike.common.kafka.listener.pollTimeout:1000}")
    private Integer kafka_listener_pollTimeout;
    @Value("${spike.common.kafka.records.task.size:50}")
    private int kafkaTaskSize;

    private KafkaConsumer<String, String> consumer;

    @PostConstruct
    private void init() {
        this.consumer = new KafkaConsumer<>(
            initConfig(brokerAddress, groupId, autoCommitInterval, sessionTimeout, kafkaTaskSize, offsetReset));// 初始化配置
    }

    private Properties initConfig(@Value("${spike.common.kafka.broker.address:127.0.0.1:9092}") String brokerAddress,
        @Value("${spike.common.kafka.consumer.groupId:spike}") String groupId,
        @Value("${spike.common.kafka.consumer.autoCommitInterval:1000}") String autoCommitInterval,
        @Value("${spike.common.kafka.consumer.sessionTimeout:15000}") String sessionTimeout,
        @Value("${spike.common.kafka.records.task.size:50}") int kafkaTaskSize,
        @Value("${spike.common.kafka.consumer.auto.offset.reset:earliest}") String offsetReset) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokerAddress);
        properties.put("group.id", groupId);
        properties.put("enable.auto.commit", true);
        properties.put("auto.commit.interval.ms", autoCommitInterval);
        properties.put("session.timeout.ms", sessionTimeout);
        properties.put("max.poll.records", kafkaTaskSize);
        properties.put("auto.offset.reset", offsetReset);
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        return properties;
    }

    public KafkaConsumer<String, String> getConsumer(String topic) {
        this.consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }
}