package com.ciyocloud.system.service.security;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.MessageUtils;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.entity.enums.UserStatusEnum;
import com.ciyocloud.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户验证处理
 *
 * @author codeck
 */
@Slf4j
@Primary
@Service(value = "localUserDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    protected final SysUserService userService;

    protected final SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserEntity user = userService.getUserByUserName(username);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException(StrUtil.format(MessageUtils.message("user.not.exists"), username));
        } else if (UserStatusEnum.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new BaseException(StrUtil.format(MessageUtils.message("user.is.deleted"), username));
        } else if (UserStatusEnum.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new BaseException(StrUtil.format(MessageUtils.message("user.is.locked"), username));
        }
        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUserEntity user) {
        SysUserVO sysUserVO = JsonUtils.jsonToObj(JsonUtils.objToJson(user), SysUserVO.class);
        if (sysUserVO != null) {
            sysUserVO.setPassword(user.getPassword());
        }
        return new LoginUserEntity(sysUserVO, permissionService.getMenuPermission(sysUserVO));
    }


}
