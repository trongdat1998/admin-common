package io.bhex.bhop.common.jwt.config;

import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.jwt.authorize.CookieProvider;
import io.bhex.bhop.common.jwt.authorize.JwtTokenProvider;
import io.bhex.bhop.common.service.AdminLoginUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Optional;

@Configuration
public class JwtConfig {
    @Resource
    private Environment environment;
//
//    @Bean(name = "jwtTokenProvider")
//    public JwtTokenProvider jwtTokenProvider() {
//        String secret = environment.getProperty(Authorize.JWT_SECRET, String.class);
//        Long tokenExpireSeconds;
//        tokenExpireSeconds = environment.getProperty(Authorize.JWT_TOKEN_EXPIRE, Long.class);
//        return new JwtTokenProvider(secret, tokenExpireSeconds, redisTemplate, adminLoginUserService);
//    }

    @Bean
    public CookieProvider cookieProvider() {
        String domain = environment.getProperty("authorize.cookie.domain");
        Boolean secure = environment.getProperty("authorize.cookie.secure", Boolean.class);
        return new CookieProvider(domain, Optional.ofNullable(secure).orElse(true));
    }
}
