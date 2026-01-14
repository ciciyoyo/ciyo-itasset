package com.ciyocloud.common.util;

import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.exception.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全服务工具类
 *
 * @author codeck
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new AuthorizationException("获取用户账户异常" + HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户账户
     **/
    public static Long getUserId() {
        try {
            return getLoginUser().getUser().getId();
        } catch (Exception e) {
            throw new AuthorizationException("获取用户ID异常" + HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUserEntity getLoginUser() {
        try {
            return (LoginUserEntity) getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new AuthorizationException("获取用户信息异常" + HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     * 不会抛出异常 用于可登录可不登录时使用获取信息
     **/
    public static LoginUserEntity getLoginUserNotEx() {
        try {
            return (LoginUserEntity) getAuthentication().getPrincipal();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }


    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 是否为管理员
     *
     * @return 结果
     */
    public static boolean isAdmin() {
        return isAdmin(getUserId());
    }
}
