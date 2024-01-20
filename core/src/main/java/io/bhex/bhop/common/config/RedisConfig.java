package io.bhex.bhop.common.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.bhex.base.redis.client.CacheClient;
import io.bhex.broker.common.redis.StringKeySerializer;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Description:redis client config
 * @Date: 2018/9/27 下午3:27
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Configuration
public class RedisConfig {

    public static final CacheKey CACHE_NAME_RESET_PASSWORD_TOKEN = CacheKey.RESET_PASSWORD_TOKEN;

    public static final CacheKey CACHE_NAME_RESET_PASSWORD_EMAIL_CAPTCHA = CacheKey.RESET_PASSWORD_EMAIL_CAPTCHA;

    public static final CacheKey CACHE_NAME_RESET_PASSWORD_EMAIL_DUPLICATE = CacheKey.RESET_PASSWORD_EMAIL_DUPLICATE;

    public static final CacheKey CACHE_NAME_LOGIN_CURRENT_AU_TOKEN = CacheKey.LOGIN_CURRENT_AU_TOKEN;

    public enum CacheKey {
        LOGIN_CURRENT_AU_TOKEN(10, TimeUnit.MINUTES, "login_current_token|"),
        RESET_PASSWORD_TOKEN(10, TimeUnit.MINUTES, "reset_pw_token|"),
        RESET_PASSWORD_EMAIL_CAPTCHA(10, TimeUnit.MINUTES, "reset_pw_email_captcha|"),
        RESET_PASSWORD_EMAIL_DUPLICATE(1, TimeUnit.MINUTES, "reset_pw_email_duplicate|");

        Integer time;

        TimeUnit timeUnit;

        String name;

        CacheKey(Integer time, TimeUnit timeUnit, String name) {
            this.time = time;
            this.timeUnit = timeUnit;
            this.name = name;
        }

        public long getTime() {
            return time;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public String getName() {
            return name;
        }

        public Integer getExpirSeconds() {
            return (int) this.timeUnit.toSeconds(this.time);
        }
    }

//    @Resource
//    private Environment environment;

//    @Bean("redisClient")
//    public CacheClient redisClient() {
//        String address = environment.getProperty("spring.redis.url", String.class);
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(address), "redis url is empty");
//        return new CacheClient(address);
//    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisClusterConfig() {
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(5))
                .build();

        ClusterClientOptions clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build();

        return clientConfigurationBuilder -> clientConfigurationBuilder.clientOptions(clientOptions);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setKeySerializer(new StringKeySerializer("admin-"));
        template.setHashKeySerializer(new StringKeySerializer("admin-"));
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

//    private
//    @Value("${spring.redis.host}")
//    String host;
//
//    private
//    @Value("${spring.redis.port}")
//    int port;
//
//    private
//    @Value("${REDIS_PASSWORD}")
//    String password;

//    @Bean
//    public CacheManager redisCacheManager() {
//        try {
//            RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
//            cacheManager.setUsePrefix(true);
//            cacheManager.setCachePrefix((cacheName) -> (cacheName + "|").getBytes());
//            Map<String, Long> expireMap = new HashMap<>();
//            for (CacheKey cacheKey : CacheKey.values()) {
//                expireMap.put(cacheKey.getName(), cacheKey.getTimeUnit().toSeconds(cacheKey.getTime()));
//            }
//
//            cacheManager.setExpires(expireMap);
//            cacheManager.setDefaultExpiration(TimeUnit.DAYS.toSeconds(30));
//            return cacheManager;
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("Get Redis Error.", e);
//        }
//        return null;
//    }


//    public JedisConnectionFactory connectionFactory() {
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setTestOnCreate(true);
//        config.setTestWhileIdle(true);
//        config.setMaxTotal(200);
//        JedisConnectionFactory factory = new JedisConnectionFactory();
//        factory.setPoolConfig(config);
//        factory.setHostName(host);
//        factory.setPort(port);
////        factory.setPassword(password);
//        factory.afterPropertiesSet();
//        return factory;
//    }
//    @Bean
//    public CacheManager cacheManager() {
//        return new RedisCacheManager(
//                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory()),
//                this.getRedisCacheConfigurationWithTtl(600L), // 默认策略，未配置的 key 会使用这个
//                this.getRedisCacheConfigurationMap() // 指定 key 策略
//        );
//    }
//
//    private Map<String, RedisCacheConfiguration> getRedisCacheConfigurationMap() {
//        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
//
//        for (CacheKey cacheKey : CacheKey.values()) {
//            redisCacheConfigurationMap.put(cacheKey.getName(), this.getRedisCacheConfigurationWithTtl(cacheKey.getTimeUnit().toSeconds(cacheKey.getTime())));
//        }
//        return redisCacheConfigurationMap;
//    }
//
//    private RedisCacheConfiguration getRedisCacheConfigurationWithTtl(Long seconds) {
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
//        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(
//                RedisSerializationContext
//                        .SerializationPair
//                        .fromSerializer(jackson2JsonRedisSerializer)
//        ).entryTtl(Duration.ofSeconds(seconds));
//
//        return redisCacheConfiguration;
//    }
}
