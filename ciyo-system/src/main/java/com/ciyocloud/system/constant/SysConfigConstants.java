package com.ciyocloud.system.constant;

/**
 * 系统配置常量
 *
 * @author : smalljop
 */
public interface SysConfigConstants {

    /**
     * 是否开启注册
     */
    String REGISTER_ENABLE = "sys.account.register";

    /***
     * 默认注册角色
     */
    String REGISTER_ROLE = "sys.account.register.roleId";
    /**
     * 默认注册部门
     */
    String REGISTER_DEPT = "sys.account.register.deptId";

    /**
     * 默认密码
     */
    String DEFAULT_PASSWORD = "sys.user.initPassword";

    /**
     * 登录失败锁定次数
     */
    String LOGIN_FAIL_COUNT = "sys.loginFail.lock.count";

    /**
     * 登录失败锁定时间
     */
    String LOGIN_FAIL_LOCK_TIME = "sys.loginFail.lock.time";

    /**
     * 是否开启首次登录修改密码
     */
    String FIRST_LOGIN_MODIFY_PASSWORD = "sys.first.login.modify.password";
}
