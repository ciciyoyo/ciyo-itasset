package com.ciyocloud.system.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.service.SysConfigService;
import com.ciyocloud.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ciyocloud.system.constant.SysConfigConstants.*;
import static com.ciyocloud.system.constant.SysRedisKeyConstants.LOGIN_FAIL_COUNT_KEY;
import static com.ciyocloud.system.constant.SysRedisKeyConstants.LOGIN_LOCK_STATUS_KEY;


@Component
@RequiredArgsConstructor
public class LoginUtils {

    private final RedisUtils redisUtils;

    private final SysConfigService sysConfigService;

    private final SysUserService sysUserService;

    /**
     * 新增登录失败次数
     * 统计1h登录次数
     *
     * @param username 用户名
     */
    public void addLoginFailCount(String username) {
        String value = sysConfigService.getConfigValueByKey(LOGIN_FAIL_COUNT);
        if (StrUtil.isBlank(value)) {
            return;
        }
        Integer numVal = Convert.toInt(value, 0);
        if (StrUtil.isNotBlank(username)) {
            String key = LOGIN_FAIL_COUNT_KEY + username;
            Integer count = redisUtils.get(key, Integer.class);
            if (count == null) {
                count = 0;
            }
            count++;
            // 如果超出了阈值 锁定
            if (count >= numVal) {
                // 默认锁定60min
                String lockMin = sysConfigService.getConfigValueByKey(LOGIN_FAIL_LOCK_TIME);
                redisUtils.set(LOGIN_LOCK_STATUS_KEY + username, count, Convert.toLong(lockMin, 60L), TimeUnit.MINUTES);
            }
            redisUtils.set(key, count, 1L, TimeUnit.HOURS);
        }
    }


    /**
     * 清空登录失败次数
     *
     * @param username 用户名
     */
    public void clearLoginFailCount(String username) {
        if (StrUtil.isNotBlank(username)) {
            String key = LOGIN_FAIL_COUNT_KEY + username;
            redisUtils.remove(key);
        }
    }

    /**
     * 是否被锁定
     *
     * @param username 用户名
     */
    public boolean isLocked(String username) {
        if (StrUtil.isNotBlank(username)) {
            String key = LOGIN_LOCK_STATUS_KEY + username;
            return redisUtils.exists(key);
        }
        return false;
    }

    /**
     * 首次登录是否需要修改密码
     */
    public boolean isNeedChangePassword(SysUserVO user) {
        String value = sysConfigService.getConfigValueByKey(FIRST_LOGIN_MODIFY_PASSWORD);
        // 说明需要修改Miami
        if (StrUtil.isNotBlank(value) && value.equals("1")) {
            Map<String, Object> extraInfo = user.getExtraInfo();
            //是否更改过密码
            Boolean changedPassword = MapUtil.getBool(extraInfo, SysUserEntity.ExtraInfo.Fields.changedPassword, false);
            return !changedPassword;
        }
        return false;
    }

    /**
     * 更新是否修改过密码的状态
     */
    public void updateChangedPasswordStatus(SysUserVO user) {
        Map<String, Object> extraInfo = user.getExtraInfo();
        if (extraInfo == null) {
            extraInfo = MapUtil.newHashMap();
        }
        extraInfo.put("changedPassword", true);
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setId(user.getId());
        userEntity.setExtraInfo(BeanUtil.toBean(extraInfo, SysUserEntity.ExtraInfo.class));
        sysUserService.updateById(userEntity);
    }

}
