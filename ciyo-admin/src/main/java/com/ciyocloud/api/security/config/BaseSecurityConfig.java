package com.ciyocloud.security.config;

import com.ciyocloud.common.util.SpringContextUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@Component
public class BaseSecurityConfig {


    public void buildBasicPath(HttpSecurity httpSecurity) throws Exception {
        // 获得 @PermitAll 带来的 URL 列表，免登录
        Multimap<HttpMethod, String> permitAllUrls = getPermitAllUrlsFromAnnotations();
        // 设置 URL 安全权限
        httpSecurity
                // 过滤请求
                .authorizeHttpRequests(registry -> registry
                        // 对于登录login 允许匿名访问
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/*.html",
                                "/**/*.html",
                                "/**/*.css",
                                "/**/*.woff",
                                "/**/*.js"
                        ).permitAll()
                        // 1.2 设置 @PermitAll 无需认证
                        .requestMatchers(HttpMethod.GET, permitAllUrls.get(HttpMethod.GET).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.POST, permitAllUrls.get(HttpMethod.POST).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.PUT, permitAllUrls.get(HttpMethod.PUT).toArray(new String[0])).permitAll()
                        .requestMatchers(HttpMethod.DELETE, permitAllUrls.get(HttpMethod.DELETE).toArray(new String[0])).permitAll()
                        // 允许对于网站静态资源的无授权访问 本地存储
                        .requestMatchers("/**/u/**").permitAll()
                        // //表单填写需要的接口
                        .requestMatchers("/**/form/ext/**").permitAll()
                        .requestMatchers("/**/wx/**").permitAll()
                        .requestMatchers("/**/captcha/**").permitAll()
                        .requestMatchers("/profile/**").anonymous()
                        // 员工部门选择组件所需要数据
                        // 公开查看表单数据
                        .requestMatchers("/swagger-ui.html").anonymous()
                        .requestMatchers("/swagger-resources/**").anonymous()
                        .requestMatchers("/webjars/**").anonymous()
                        .requestMatchers("/**/v3/api-docs/**").permitAll()
                        // 放行websocket接口
                        .requestMatchers("/websocket").anonymous()
                        // 端点 用来看信息
                        .requestMatchers("/actuator/health").anonymous()
                        .requestMatchers("/actuator/info").anonymous()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // 移动端
                        // 除上面外的所有请求全部需要鉴权认证
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
    }

    /**
     * 获取所有接口上的注解，并判断是否有 @PermitAll 注解
     *
     * @return
     */
    private Multimap<HttpMethod, String> getPermitAllUrlsFromAnnotations() {
        Multimap<HttpMethod, String> result = HashMultimap.create();
        // 获得接口对应的 HandlerMethod 集合
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringContextUtils.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        // 获得有 @PermitAll 注解的接口  注解要加在方法上 紧挨着方法名
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.hasMethodAnnotation(PermitAll.class)) {
                continue;
            }
            if (entry.getKey().getPathPatternsCondition() == null) {
                continue;
            }
            Set<String> urls = entry.getKey().getPathPatternsCondition().getPatternValues();
            // 根据请求方法，添加到 result 结果
            entry.getKey().getMethodsCondition().getMethods().forEach(requestMethod -> {
                switch (requestMethod) {
                    case GET:
                        result.putAll(HttpMethod.GET, urls);
                        break;
                    case POST:
                        result.putAll(HttpMethod.POST, urls);
                        break;
                    case PUT:
                        result.putAll(HttpMethod.PUT, urls);
                        break;
                    case DELETE:
                        result.putAll(HttpMethod.DELETE, urls);
                        break;
                    default:
                        break;
                }
            });
        }
        return result;
    }
}
