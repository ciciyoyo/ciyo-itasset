package com.ciyocloud.api.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;

/**
 * Sa-Token 认证拦截器
 * 
 * 该拦截器用于全局路由拦截，配合 @SaCheckLogin、@SaCheckRole、@SaCheckPermission 等注解使用
 * 
 * @author codeck
 */
public class SaAuthInterceptor extends SaInterceptor {

    public SaAuthInterceptor(String apiPrefix) {
        super(handle -> {
            // 所有路由需要登录认证
            SaRouter
                    // 基础路径
                    .match(apiPrefix + "/**")
                    // 排除登录注册接口
                    .notMatch(apiPrefix + "/login", apiPrefix + "/register")
                    // 排除静态资源
                    .notMatch("/", "/*.html", "/**.html", "/**.css", "/**.woff", "/**.js", "/*.ico")
                    // 排除 Swagger 文档
                    .notMatch("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v3/api-docs/**", "/doc.html")
                    // 排除 WebSocket
                    .notMatch("/websocket", "/websocket/**")
                    // 排除 Actuator 健康检查和信息端点
                    .notMatch("/actuator/health", "/actuator/info")
                    // 检查登录状态
                    .check(r -> StpUtil.checkLogin());

            // Actuator 管理端点需要 admin 角色
            SaRouter.match("/actuator/**")
                    .notMatch("/actuator/health", "/actuator/info")
                    .check(r -> StpUtil.checkRole("admin"));
        });
    }
}
