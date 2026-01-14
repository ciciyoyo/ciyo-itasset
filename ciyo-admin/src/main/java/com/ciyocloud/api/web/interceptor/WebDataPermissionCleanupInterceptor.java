package com.ciyocloud.api.web.interceptor;

import com.ciyocloud.datapermission.helper.DataPermissionHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class WebDataPermissionCleanupInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            // 请求结束后清理 ThreadLocal
            DataPermissionHelper.clear();
        } catch (Exception ignore) {

        }
    }
}
