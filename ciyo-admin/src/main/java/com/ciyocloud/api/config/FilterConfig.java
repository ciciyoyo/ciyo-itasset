package com.ciyocloud.api.config;

import cn.dev33.satoken.filter.SaTokenContextFilterForJakartaServlet;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.api.web.filter.HttpTraceLogFilter;
import com.ciyocloud.api.web.filter.ValidateCodeFilter;
import com.ciyocloud.common.xss.XssFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * @author codeck
 * @description 过滤器配置
 * @create 2019-01-29 16:27
 **/
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    @Setter
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private XssUrlProperties xssUrlProperties;

    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    public FilterRegistrationBean xssFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns(StrUtil.split(xssUrlProperties.getUrlPatterns(), ",").toArray(new String[0]));
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", xssUrlProperties.getExcludes());
        initParameters.put("richTextUrls", CollUtil.join(xssUrlProperties.getRichTextUrls(), ","));
        registration.setInitParameters(initParameters);
        return registration;
    }

    /**
     * 请求日志
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "platform.request", name = "trace-log", havingValue = "true")
    public FilterRegistrationBean httpTraceLogFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new HttpTraceLogFilter());
        registration.addUrlPatterns("/*");
        registration.setName("httpTraceLogFilter");
        registration.setOrder(Integer.MAX_VALUE - 2);
        return registration;
    }


    /**
     * 验证码过滤器
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "aj.captcha", name = "enable", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean validateCodeFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
        validateCodeFilter.setContextPath(contextPath);
        registration.setFilter(validateCodeFilter);
        registration.addUrlPatterns("/*");
        registration.setName("validateCodeFilter");
        registration.setOrder(Integer.MAX_VALUE - 1);
        return registration;
    }


    /**
     * 修复撒 token 奇怪的时候找不到上下文啥的  比如在 sse 场景
     * @return
     */
    @Bean
    public FilterRegistrationBean<SaTokenContextFilterForJakartaServlet> saTokenFilterRegistration() {
        final var reg = new FilterRegistrationBean<>(new SaTokenContextFilterForJakartaServlet());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.setAsyncSupported(true);
        reg.setDispatcherTypes(
                DispatcherType.ASYNC,
                DispatcherType.REQUEST
        );
        return reg;
    }

}
