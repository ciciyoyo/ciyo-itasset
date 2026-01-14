package com.ciyocloud.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : codeck
 * @description : 系统基础环境配置
 * @create :  2022/01/13 13:39
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemEnvConfig {

    /**
     * 系统名称
     */
    private String name;


    /**
     * logo地址
     */
    private String logoImg;

    /**
     * 管理后台logo
     */
    private String adminLogoImg;

    /**
     * 前端项目域名
     */
    private String webBaseUrl;


    /**
     * 允许三方授权直接登录 无需注册
     */
    private Boolean allowThirdPartyLogin;


    /**
     * 支持的第三方登录类型
     */
    private List<String> thirdPartyLoginTypeList;

    /**
     * 开启强制收集手机号
     */
    private boolean enableSmsVerification;

    /**
     * 是否强制修改账号信息 第一次 防止用户去小程序之类登录不记得密码
     */
    private boolean forceAccountCompletion;

}
