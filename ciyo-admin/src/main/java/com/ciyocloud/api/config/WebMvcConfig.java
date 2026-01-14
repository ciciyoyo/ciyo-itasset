package com.ciyocloud.api.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.api.web.interceptor.WebDataPermissionCleanupInterceptor;
import com.ciyocloud.common.converter.IntCodeToEnumConverterFactory;
import com.ciyocloud.common.converter.StringCodeToEnumConverterFactory;
import com.ciyocloud.storage.cloud.OssStorageConfig;
import com.ciyocloud.storage.cloud.OssStorageFactory;
import com.ciyocloud.storage.enums.OssTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    private final List<String> staticResources = CollUtil.newArrayList("/**/*.html",
            "/**/*.js",
            "/**/*.css",
            "/**/*.woff",
            "/**/*.ttf");


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
            registry.addResourceHandler(config.getAccessPathPattern())
                    .addResourceLocations("file:" + uploadFolder);
        }
        //这句不要忘了，否则项目默认静态资源映射会失效
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        // swagger 配置
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebDataPermissionCleanupInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(staticResources);
    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringCodeToEnumConverterFactory());
        registry.addConverterFactory(new IntCodeToEnumConverterFactory());
    }

}
