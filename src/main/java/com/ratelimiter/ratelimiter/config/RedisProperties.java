package com.ratelimiter.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
@Data
@ConfigurationProperties(prefix = "spring-redis")
public class RedisProperties {
    private String host = "localhost";
    private int port = 6379;
    private int timeout = 2000;


    //Jedis --> java client library for Redis.
    //Lets java applications comminicate with redis server.
    @Bean
    public JedisPool getJedisPool() {
        //Jedispool keeps multiple connections ready to reuse.
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        return new JedisPool(poolConfig, host, port, timeout);
    }
}
