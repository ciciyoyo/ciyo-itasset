package com.ciyocloud.api.security.handle;

import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.entity.security.LoginUserEntity;
import com.ciyocloud.common.util.ResponseUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.system.service.security.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义退出处理类 返回成功
 *
 * @author codeck
 */
@Component
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    private final TokenService tokenService;

    /**
     * 退出处理
     *
     * @return 结果
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        LoginUserEntity loginUser = tokenService.getLoginUser(request);
        if (ObjectUtil.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
        }
        ResponseUtils.outJson(response, Result.success("退出成功"));
    }
}
