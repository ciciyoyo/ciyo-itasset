package com.ciyocloud.api.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.api.security.SaAuthInterceptor;
import com.ciyocloud.api.web.interceptor.WebDataPermissionCleanupInterceptor;
import com.ciyocloud.common.converter.IntCodeToEnumConverterFactory;
import com.ciyocloud.common.converter.StringCodeToEnumConverterFactory;
import com.ciyocloud.storage.cloud.OssStorageConfig;
import com.ciyocloud.storage.cloud.OssStorageFactory;
import com.ciyocloud.storage.enums.OssTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.*;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * web mvc 配置
 *
 * @author codeck
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * html静态资源   js静态资源    css静态资源
     */
    private final List<String> staticResources = CollUtil.newArrayList("/**/*.html", "/**/*.js", "/**/*.css", "/**/*.woff", "/**/*.woff2", "/**/*.ttf", "/**/*.ico", "/**/*.png", "/**/*.gif", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg", "/**/*.bmp");
    @Value("${platform.api-prefix:/api}")
    private String apiPrefix;


    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // SpringBoot2.4.0 [allowedOriginPatterns]代替[allowedOrigins]
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        // 设置访问源请求头
        config.addAllowedHeader(CorsConfiguration.ALL);
        // 设置访问源请求方法
        config.addAllowedMethod(CorsConfiguration.ALL);
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }



    /**
     * 配置本地文件上传的虚拟路径和静态化的文件生成路径
     * 备注：这是一种图片上传访问图片的方法，推荐使用nginx反向代理访问图片
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        OssStorageConfig config = OssStorageFactory.getConfig();
        if (ObjectUtil.isNotNull(config) && OssStorageFactory.getConfig().getOssType() == OssTypeEnum.LOCAL) {
            // 文件上传
            String uploadFolder = config.getUploadFolder();
            uploadFolder = StringUtils.appendIfMissing(uploadFolder, File.separator);
            registry.addResourceHandler(config.getAccessPathPattern()).addResourceLocations("file:" + uploadFolder);
        }
        //这句不要忘了，否则项目默认静态资源映射会失效
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        // swagger 配置
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 认证拦截器
        registry.addInterceptor(new SaAuthInterceptor(apiPrefix)).addPathPatterns(apiPrefix + "/**").excludePathPatterns(staticResources);

        // 注册数据权限清理拦截器
        registry.addInterceptor(new WebDataPermissionCleanupInterceptor()).addPathPatterns(apiPrefix + "/**").excludePathPatterns(staticResources);

    }


    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        // 给所有 @RestController 添加统一前缀，从配置读取
        if (StringUtils.isNotEmpty(apiPrefix)) {
            configurer.addPathPrefix(apiPrefix, c -> c.isAnnotationPresent(RestController.class));
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringCodeToEnumConverterFactory());
        registry.addConverterFactory(new IntCodeToEnumConverterFactory());
    }

    /**
     * 解决单页应用（SPA）的历史模式（History Mode）导致的 404 问题
     * 当请求的是 HTML 页面且返回 404 时，转发到 /index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 确保默认访问根路径也能转发到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }


    @Bean
    public ErrorViewResolver spaErrorViewResolver() {
        return (request, status, model) -> {
            if (status == HttpStatus.NOT_FOUND) {
                String requestUri = request.getRequestURI();
                // 排除配置的 API 前缀路径
                if (StringUtils.isNotEmpty(apiPrefix) && requestUri.startsWith(apiPrefix)) {
                    return null;
                }
                return new ModelAndView("forward:/index.html");
            }
            return null;
        };
    }
}

