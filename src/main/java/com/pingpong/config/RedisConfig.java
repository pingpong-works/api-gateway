package com.pingpong.config;

import com.pingpong.property.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableRedisRepositories
@Configuration
public class RedisConfig {

    /**
     * Redis 설정 정보를 Environment에서 읽어와 RedisProperties 객체를 생성하는 Bean
     * @param environment Spring의 Environment 객체
     * @return RedisProperties 설정 정보가 담긴 객체
     */
    @Bean
    public RedisProperties redisProperties(Environment environment) {
        String host = environment.getProperty("spring.redis.host");
        int port = Integer.parseInt(environment.getProperty("spring.redis.port", "6379"));
        String password = environment.getProperty("spring.redis.password");
        int database = Integer.parseInt(environment.getProperty("spring.redis.database", "0"));

        return RedisProperties.builder()
                .host(host)
                .port(port)
                .password(password)
                .database(database)
                .build();
    }

    /**
     * RedisConnectionFactory를 생성하는 Bean
     * @param redisProperties Redis 설정 정보를 담고 있는 객체
     * @return RedisConnectionFactory 객체
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
        configuration.setPassword(redisProperties.getPassword());
        configuration.setDatabase(redisProperties.getDatabase());
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * RedisTemplate을 생성하는 Bean
     * @param redisProperties Redis 설정 정보를 담고 있는 객체
     * @return RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisProperties redisProperties) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory(redisProperties));
        return redisTemplate;
    }
}
