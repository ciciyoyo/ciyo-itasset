package com.ciyocloud.api.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.ciyocloud.common.constant.ResponseCodeConstants;
import com.ciyocloud.common.util.ResponseUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SpringContextUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

/**
 * @author : smalljop
 * @description : 滑动验证码校验
 * @create : 2020-12-14 15:51
 **/
public class ValidateCodeFilter implements Filter {

    private final List<String> validateUrls = CollUtil.newArrayList(
            "/login", "/common/phone/code");
    @Setter
    private String contextPath;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestPath = httpServletRequest.getRequestURI().replaceFirst(contextPath, "");
        if (!validateUrls.contains(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        String code = request.getParameter("slideCode");
        if (StrUtil.isBlank(code)) {
            ResponseUtils.outJson(response, Result.failed(ResponseCodeConstants.NEED_VERIFICATION, ""));
            return;
        }
        CaptchaService captchaService = SpringContextUtils.getBean(CaptchaService.class);
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaVerification(code);
        if (!captchaService.verification(vo).isSuccess()) {
            ResponseUtils.outJson(response, Result.failed(ResponseCodeConstants.FAIL, ResponseCodeConstants.VALIDATE_CODE_FAIL_MSG));
            return;
        }
        filterChain.doFilter(request, response);
    }

}
