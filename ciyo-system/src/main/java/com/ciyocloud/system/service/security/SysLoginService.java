package com.ciyocloud.system.service.security;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.MessageUtils;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.entity.enums.UserStatusEnum;
import com.ciyocloud.system.exception.UserPasswordNotMatchException;
import com.ciyocloud.system.service.security.SysPermissionService;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.util.LoginUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final SysUserService userService;

    private final LoginUtils loginUtils;

    private final SysPermissionService permissionService;

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

        // 查询用户
        SysUserEntity user = userService.getUserByUserName(username);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            loginUtils.addLoginFailCount(username);
            throw new BaseException(StrUtil.format(MessageUtils.message("user.not.exists"), username));
        } else if (UserStatusEnum.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new BaseException(StrUtil.format(MessageUtils.message("user.is.deleted"), username));
        } else if (UserStatusEnum.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new BaseException(StrUtil.format(MessageUtils.message("user.is.locked"), username));
        }

        // 验证密码
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            loginUtils.addLoginFailCount(username);
            throw new UserPasswordNotMatchException(MessageUtils.message("user.password.not.match"));
        }

        // 创建登录用户信息
        LoginUserEntity loginUser = createLoginUser(user);
        log.debug("loginUser:{}", loginUser);
        recordLoginInfo(loginUser.getUser());
        return loginUser;
    }

    /**
     * 创建登录用户
     */
    public LoginUserEntity createLoginUser(SysUserEntity user) {
        SysUserVO sysUserVO = JsonUtils.jsonToObj(JsonUtils.objToJson(user), SysUserVO.class);
        if (sysUserVO != null) {
            sysUserVO.setPassword(user.getPassword());
        }
        return new LoginUserEntity(sysUserVO, permissionService.getMenuPermission(sysUserVO));
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(SysUserVO user) {
        user.setLoginDate(LocalDateTime.now());
        SysUserEntity sysUserEntity = new SysUserEntity();
        sysUserEntity.setLoginDate(LocalDateTime.now());
        userService.update(sysUserEntity, Wrappers.<SysUserEntity>lambdaQuery().eq(SysUserEntity::getId, user.getId()));
    }

    /**
     * 是否需要修改密码
     */
    public boolean isNeedChangePassword(SysUserVO user) {
        return loginUtils.isNeedChangePassword(user);
    }
}
