package com.ciyocloud.system.constant;

/**
 * @author : codeck
 * @description :
 * @create :  2021/07/06 16:01
 **/
public interface SysRedisKeyConstants {
    /**
     * 参数管理 cache key
     */
    String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 登录失败次数
     */
    String LOGIN_FAIL_COUNT_KEY = "login_fail_count:";

    /**
     * 登录失败锁定状态
     */
    String LOGIN_LOCK_STATUS_KEY = "login_lock_status:";
}
