package com.ciyocloud.system.constant;

/**
 * @author : codeck
 * @description :
 * @create : 2020-11-11 18:16
 **/
public interface SystemConstants {

    /**
     * 登录用户 redis key
     */
    String LOGIN_TOKEN_KEY = "login_tokens:";
    /**
     * 令牌前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    String LOGIN_USER_KEY = "login_user_key";

    /**
     * 黑名单前缀 被删除或者禁用的用户id全部失效
     */
    String BLACKLIST_KEY = "blacklist:userId:";

    /**
     * 用户ID
     */
    String JWT_USERID = "userid";

    /**
     * 用户名称
     */
    String JWT_USERNAME = "sub";

    /**
     * 用户头像
     */
    String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    String JWT_AUTHORITIES = "authorities";

    /**
     * 正常状态
     */
    String NORMAL_STATUS = "0";

    /**
     * 停用状态
     */
    String DEACTIVATE_STATUS = "1";

    /**
     * 数据权限单表 无别名
     */
    String NONE_ALIAS = "none";


    /**
     * 是否状态
     */
    String YES_STATUS = "Y";


    /**
     * 是否状态
     */
    String NO_STATUS = "N";


    /**
     * 正常状态
     */
    String NORMAL = "0";

    /**
     * 异常状态
     */
    String DISABLE = "1";
}
