package com.ratelimiter.ratelimiter.service;


import com.ratelimiter.ratelimiter.config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {

    private final JedisPool jedisPool;
    private final RateLimiterProperties properties;

    private final String TOKENS_KEY_PREFIX = "rate_limiter:tokens:";
    private static final String LAST_REFILL_KEY_PREFIX = "rate_limiter:last_refill:";

    //rate_limiter:tokens:192.168.1.100 --> Current Token Count
    //rate_limiter:last_refill:192.168.1.100 --> Last Refill Timestamp

    public boolean isALlowed(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource()) {
            refillTokens(clientId, jedis);

            String tokenStr = jedis.get(tokenKey);

            long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();
            if(currentTokens <= 0){
                return false;
            }

            long decremented = jedis.decr(tokenKey);
            return decremented >= 0;
        }
    }

    public long getCapacity(String clientId) {

        return properties.getCapacity();
    }

    public long getAvailableTokens(String clientId) {

        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource()) {

            refillTokens(clientId, jedis);
            String tokenStr = jedis.get(tokenKey);
            return tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();

        }
    }

    public void refillTokens(String clientId, Jedis jedis) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;

        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);

        if(lastRefillStr == null){
            jedis.set(tokenKey, String.valueOf(properties.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }

        long lastRefillTime = Long.parseLong(lastRefillStr);
        long elapsedTime = now - lastRefillTime;

        if(elapsedTime <= 0){
            return;
        }

        long tokensToAdd = elapsedTime * properties.getRefillRate() / 1000;  //Convert msec to sec

        String tokenStr = jedis.get(tokenKey);
        long  currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();
        long newTokens = Math.min(properties.getCapacity(), currentTokens + tokensToAdd);

        jedis.set(tokenKey, String.valueOf(newTokens));
        jedis.set(lastRefillKey,String.valueOf(now));

    }
}
