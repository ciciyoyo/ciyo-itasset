package com.ciyocloud.system.service.security;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.system.constant.SystemConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ciyocloud.system.constant.SystemConstants.BLACKLIST_KEY;

/**
 * token验证处理
 *
 * @author codeck
 */
@Component
@Slf4j
public class TokenService {
    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    /**
     * 过期间隔 20分钟
     */
    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;
    /**
     * 令牌自定义标识
     */
    @Value("${token.header}")
    private String header;
    /**
     * 令牌秘钥
     */
    @Value("${token.secret}")
    private String secret;
    /**
     * 令牌有效期（默认120分钟）
     */
    @Value("${token.expireTime}")
    private long expireTime;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUserEntity getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        try {
            if (StrUtil.isNotEmpty(token)) {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(SystemConstants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUserEntity loginUserEntity = redisUtils.get(userKey, LoginUserEntity.class);
                // 黑名单机制 被拉黑的用户所有token全部失效
                if (null != loginUserEntity && redisUtils.exists(BLACKLIST_KEY + loginUserEntity.getUserId())) {
                    log.error("账号被拉黑:{}", loginUserEntity.getUserId());
                    return null;
                }
                return loginUserEntity;
            }
        } catch (Exception e) {
            log.error("解析用户身份信息失败", e);
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUserEntity loginUser) {
        if (ObjectUtil.isNotNull(loginUser) && StrUtil.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser, null);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token) {
        if (StrUtil.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisUtils.remove(userKey);
        }
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createToken(LoginUserEntity loginUser) {
        String token = IdUtil.fastUUID();
        loginUser.setToken(token);
        refreshToken(loginUser, null);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SystemConstants.LOGIN_USER_KEY, token);
        return createToken(claims);
    }

    /**
     * 创建令牌 移动端使用 不自动过期
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createMobileToken(LoginUserEntity loginUser) {
        // 使用md5id
        String token = IdUtil.fastUUID();
        loginUser.setToken(token);
        // 超长时间 等于基本不过期
        refreshToken(loginUser, 999999L);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SystemConstants.LOGIN_USER_KEY, token);
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser 登录信息
     */
    public void verifyToken(LoginUserEntity loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser, null);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser        登录信息
     * @param customExpireTime 自定义过期时间
     */
    public void refreshToken(LoginUserEntity loginUser, Long customExpireTime) {
        loginUser.setLoginTime(System.currentTimeMillis());
        if (null == customExpireTime) {
            customExpireTime = expireTime;
        }
        // 刷新token 默认增加120分钟
        loginUser.setExpireTime(loginUser.getLoginTime() + customExpireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        redisUtils.set(userKey, loginUser, customExpireTime, TimeUnit.MINUTES);
    }


    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request 请求
     * @return token
     */
    public String getToken(HttpServletRequest request) {
        String token = StrUtil.isNotEmpty(request.getHeader(header)) ? request.getHeader(header) : request.getParameter(header);
        if (StrUtil.isNotEmpty(token) && token.startsWith(SystemConstants.TOKEN_PREFIX)) {
            token = token.replace(SystemConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return SystemConstants.LOGIN_TOKEN_KEY + uuid;
    }
}
