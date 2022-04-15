package com.bootcamp.reactive.pago_de_servicios_ux.config;

import com.bootcamp.reactive.pago_de_servicios_ux.entities.Servicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class ReactiveRedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration();
        redisStandaloneConfig.setHostName(redisHost);
        redisStandaloneConfig.setPort(redisPort);
        return new LettuceConnectionFactory(redisStandaloneConfig);
    }

    @Bean
    public ReactiveRedisOperations<String, Servicio> redisOperations(LettuceConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Servicio> serializationContext = RedisSerializationContext
                .<String, Servicio>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(new GenericToStringSerializer<>(Servicio.class))
                .hashKey(new StringRedisSerializer())
                //.hashValue(new JdkSerializationRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
