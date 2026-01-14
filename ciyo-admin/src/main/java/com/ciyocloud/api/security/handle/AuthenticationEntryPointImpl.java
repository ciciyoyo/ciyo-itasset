package com.ciyocloud.api.security.handle;

import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.constant.ResponseCodeConstants;
import com.ciyocloud.common.util.ResponseUtils;
import com.ciyocloud.common.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 认证失败处理类 返回未授权
 *
 * @author codeck
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        String msg = StrUtil.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        ResponseUtils.outHttpJson(response, Result.failed(ResponseCodeConstants.UNAUTHORIZED, msg));
    }
}
