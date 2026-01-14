package com.ciyocloud.common.xss;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 防止XSS攻击的过滤器
 *
 * @author ruoyi
 */
public class XssFilter implements Filter {
    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();

    private List<String> richTextUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = filterConfig.getInitParameter("excludes");
        String tempRichTextUrls = filterConfig.getInitParameter("richTextUrls");
        if (StrUtil.isNotEmpty(tempExcludes)) {
            excludes.addAll(StrUtil.splitTrim(tempExcludes, ","));
        }
        if (StrUtil.isNotEmpty(tempRichTextUrls)) {
            richTextUrls.addAll(StrUtil.splitTrim(tempRichTextUrls, ","));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        if (handleRichTextURL(req, resp)) {
            XssRichTextHttpServletRequestWrapper xssRequest = new XssRichTextHttpServletRequestWrapper((HttpServletRequest) request);
            chain.doFilter(xssRequest, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        return excludes.contains(url);
    }

    private boolean handleRichTextURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        return richTextUrls.contains(url);
    }

    @Override
    public void destroy() {

    }
}
