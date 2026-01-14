package com.ciyocloud.system.service.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.MessageUtils;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.exception.UserPasswordNotMatchException;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.util.LoginUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录校验方法
 *
 * @author codeck
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysLoginService {

    private final AuthenticationManager authenticationManager;


    private final SysUserService userService;

    private final LoginUtils loginUtils;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 结果
     */
    public LoginUserEntity login(String username, String password) {
        boolean locked = loginUtils.isLocked(username);
        if (locked) {
            throw new BaseException(MessageUtils.message("user.password.retry.limit"));
        }
        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                loginUtils.addLoginFailCount(username);
                throw new UserPasswordNotMatchException(MessageUtils.message("user.password.not.match"));
            } else {
                throw new BaseException(e.getMessage());
            }
        }
        LoginUserEntity loginUser = (LoginUserEntity) authentication.getPrincipal();
        log.debug("loginUser:{}", loginUser);
        recordLoginInfo(loginUser.getUser());
        return loginUser;
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(SysUserVO user) {
        user.setLoginDate(LocalDateTime.now());
        SysUserEntity sysUserEntity = new SysUserEntity();
        sysUserEntity.setLoginDate(LocalDateTime.now());
        userService.update(sysUserEntity,
                Wrappers.<SysUserEntity>lambdaQuery().eq(SysUserEntity::getId, user.getId()));
    }

    /**
     * 是否需要修改密码
     */
    public boolean isNeedChangePassword(SysUserVO user) {
        return loginUtils.isNeedChangePassword(user);
    }
}
