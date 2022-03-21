package com.lsx.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置Key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //设置Value的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        //设置hashKey的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //设置hashValue的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();

        return  redisTemplate ;
    }
}
