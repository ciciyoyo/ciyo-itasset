package com.ciyocloud.system.constant;

/**
 * 用户常量信息
 *
 * @author codeck
 */
public interface UserConstants {
    /**
     * 平台内系统用户的唯一标志
     */
    String SYS_USER = "SYS_USER";

    /**
     * 正常状态
     */
    String NORMAL = "0";

    /**
     * 异常状态
     */
    String EXCEPTION = "1";

    /**
     * 用户封禁状态
     */
    String USER_DISABLE = "1";

    /**
     * 角色封禁状态
     */
    String ROLE_DISABLE = "1";

    /**
     * 部门正常状态
     */
    String DEPT_NORMAL = "0";

    /**
     * 部门停用状态
     */
    String DEPT_DISABLE = "1";

    /**
     * 字典正常状态
     */
    String DICT_NORMAL = "0";

    /**
     * 是否为系统默认（是）
     */
    String YES = "Y";

    /**
     * 是否菜单外链（是）
     */
    Integer YES_FRAME = 0;

    /**
     * 是否菜单外链（否）
     */
    Integer NO_FRAME = 1;


    /**
     * 菜单位置 后台
     */
    String LOCATION_BACKSTAGE = "1";

    /**
     * 菜单位置
     */
    String LOCATION_FRONT = "2";

    /**
     * 菜单类型（目录）
     */
    String TYPE_DIR = "M";

    /**
     * 菜单类型（菜单）
     */
    String TYPE_MENU = "C";

    /**
     * 菜单类型（按钮）
     */
    String TYPE_BUTTON = "F";

    /**
     * Layout组件标识
     */
    String LAYOUT = "Layout";

    /**
     * ParentView组件标识
     */
    String PARENT_VIEW = "/index/index";
    /**
     * 外链组件
     */
    String LINK_VIEW = "layout/routerView/link";
    /**
     * iframe组件
     */
    String IFRAME_VIEW = "layout/routerView/iframe";
    /**
     * 前台组件
     */
    String FRONT_VIEW = "views/client/layout/index.vue";

    /**
     * s
     * 校验返回结果码
     */
    String UNIQUE = "0";
    String NOT_UNIQUE = "1";

}
