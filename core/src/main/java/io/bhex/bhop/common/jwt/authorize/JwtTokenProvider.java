/*
 ************************************
 * @项目名称: bhcard
 * @文件名称: JwtTokenProvider
 * @Date 2018/05/27
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.bhop.common.jwt.authorize;

import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("jwtTokenProvider")
public class JwtTokenProvider {
    @Resource
    private Environment environment;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private AdminLoginUserService adminLoginUserService;

    public static final String USER_TOKEN_KEY = "login_token|uid_%s";

    private String secret;

    private Long tokenExpireSeconds;

    @PostConstruct
    public void init() {
        this.secret = environment.getProperty(Authorize.JWT_SECRET, String.class);
        this.tokenExpireSeconds = environment.getProperty(Authorize.JWT_TOKEN_EXPIRE, Long.class);
    }

    public String generateToken(Long orgId, String subject) {
        long now = (new Date()).getTime();
        Date expiration = new Date(now + this.tokenExpireSeconds * 1000);
        String compact = Jwts.builder()
                .setSubject(subject)
                .claim(Authorize.AUTHORIZE_KEY, subject)
                .signWith(SignatureAlgorithm.HS256, secret)
                .setExpiration(expiration)
                .compact();

        String key = String.format(USER_TOKEN_KEY, subject);
        String jwtToken = redisTemplate.opsForValue().get(key);
        try {
            if (StringUtils.isNotEmpty(jwtToken)) {
                log.info("Generate Token: Repeated login, the previous user was kicked out. subject => {}.", subject);
                adminLoginUserService.repeatedLoginAlarm(orgId, Long.parseLong(subject));
            } else {
                adminLoginUserService.loginAlarm(orgId, Long.parseLong(subject));
            }
        } catch (Exception e) {
            log.info("{}", e); //ignore
        }

        redisTemplate.opsForValue().set(key, compact, tokenExpireSeconds, TimeUnit.SECONDS);
        return compact;
    }

    public String parseSubject(String jwtToken) throws BizException {
        if (StringUtils.isEmpty(jwtToken)) {
            return null;
        }
        try {
            String subject = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .getSubject();

            String key = String.format(USER_TOKEN_KEY, subject);
            String token = redisTemplate.opsForValue().get(key);

            if (StringUtils.isEmpty(token)) { //没有存在redis中，使用的是过期的autoken
                log.info("Old autoken login. subject => {}.", subject);
                throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR);
            }

            if (StringUtils.isNotEmpty(token) && !jwtToken.equals(token)) {
                log.info("Generate Token: Repeated login, the previous user was kicked out. subject => {}.", subject);
                throw new BizException(ErrorCode.REPEATED_LOGIN_KICKED_OUT);
            }
            return subject;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.info("parseSubject error", e);
        }
        return null;
    }
}


