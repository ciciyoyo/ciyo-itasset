package com.ciyocloud.api.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.core.exceptions.ValidateException;
import com.ciyocloud.common.constant.ResponseCodeConstants;
import com.ciyocloud.common.exception.AuthorizationException;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.exception.PromptException;
import com.ciyocloud.common.util.ExceptionNotifyUtils;
import com.ciyocloud.common.util.ResponseUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.idempotent.exception.RepeatSubmitException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author codeck
 */
@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {
    @Value("${platform.api-prefix:/api}")
    private String apiPrefix;

    /**
     * 判断是否为 SSE 请求
     * SSE 请求不能返回 JSON Result 对象
     */
    private boolean isSseRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/event-stream");
    }

    // =================================== Sa-Token 认证鉴权异常 ===================================

    /**
     * Sa-Token 未登录异常处理
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("NotLoginException: {}", e.getMessage());

        // SSE 请求不能返回 Result
        if (isSseRequest(request)) {
            log.warn("SSE 请求认证失败，无法返回 Result 对象");
            return null;
        }

        String message;
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                message = "您尚未登录，请登录后操作";
                break;
            case NotLoginException.INVALID_TOKEN:
                message = "登录凭证无效，请重新登录";
                break;
            case NotLoginException.TOKEN_TIMEOUT:
                message = "登录已过期，请重新登录";
                break;
            case NotLoginException.BE_REPLACED:
                message = "您的账号已在其他设备登录";
                break;
            case NotLoginException.KICK_OUT:
                message = "您已被强制下线，请重新登录";
                break;
            default:
                message = "当前会话未登录，请重新登录";
        }
        return Result.failed(ResponseCodeConstants.UNAUTHORIZED, message);
    }

    /**
     * Sa-Token 权限不足异常处理
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("NotPermissionException: {}", e.getMessage());

        // SSE 请求不能返回 Result
        if (isSseRequest(request)) {
            log.warn("SSE 请求权限不足，无法返回 Result 对象");
            return null;
        }

        return Result.failed(ResponseCodeConstants.FORBIDDEN, "权限不足，无法访问");
    }

    /**
     * Sa-Token 角色不足异常处理
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.warn("NotRoleException: {}", e.getMessage());
        return Result.failed(ResponseCodeConstants.FORBIDDEN, "角色权限不足，无法访问");
    }

    /**
     * 自定义授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public Result<?> handleAuthorizationException(AuthorizationException e) {
        log.warn("AuthorizationException: {}", e.getMessage());
        return Result.failed(ResponseCodeConstants.UNAUTHORIZED, "{sys.login.expired}");
    }

    // =================================== 业务逻辑异常 ===================================

    /**
     * 基础业务异常 & 提示型异常
     */
    @ExceptionHandler({BaseException.class, PromptException.class})
    public Result<?> handleBaseException(BaseException e, HttpServletRequest request) {
        log.info("BusinessException: code={}, msg={}", e.getCode(), e.getMessage());

        // SSE 请求不能返回 Result
        if (isSseRequest(request)) {
            log.warn("SSE 请求业务异常，无法返回 Result 对象");
            return null;
        }

        return Result.restResult(null, e.getCode(), e.getMessage());
    }

    /**
     * Hutool 验证异常
     */
    @ExceptionHandler(ValidateException.class)
    public Result<?> handleValidateException(ValidateException e) {
        log.warn("ValidateException: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 防重提交异常
     */
    @ExceptionHandler(RepeatSubmitException.class)
    public Result<?> handleRepeatSubmitException(RepeatSubmitException e) {
        log.warn("RepeatSubmitException: {}", e.getMessage());
        return Result.failed(e.getMessage());
    }

    // =================================== 参数校验与请求异常 ===================================

    /**
     * 方法参数校验异常 (Spring Boot Validation)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleMethodArgumentNotValidException(Exception e) {
        log.warn("ArgumentValidationException: {}", e.getMessage());
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }

        String message = "";
        if (bindingResult != null) {
            message = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));
        }
        return Result.failed(message);
    }

    /**
     * 请求参数缺失
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException: {}", e.getMessage());
        return Result.failed("缺失必要请求参数: " + e.getParameterName());
    }

    /**
     * 404 路径不存在
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("NoHandlerFoundException: {}", e.getMessage());
        return Result.failed(ResponseCodeConstants.NOT_FOUND, "{sys.path.error}");
    }

    // =================================== 数据层异常 ===================================

    /**
     * 数据库唯一键冲突
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("DuplicateKeyException: {}", e.getMessage(), e);
        return Result.failed("{sys.data.exists}");
    }

    /**
     * 数据完整性异常 (如外键约束)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage(), e);
        return Result.failed("数据操作异常，请检查数据完整性");
    }

    /**
     * 通用 SQL 异常
     */
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSqlException(SQLException e) {
        log.error("SQLException: {}", e.getMessage(), e);
        return Result.failed("系统数据库异常");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().startsWith(apiPrefix)) {
            ResponseUtils.outHttpJson(response, Result.success());
            return;
        }
        
        // 检查 index.html 是否存在
        try {
            if (request.getServletContext().getResource("/index.html") == null) {
                log.warn("index.html 不存在，请启动前端项目");
                ResponseUtils.outHttpJson(response, Result.failed("请启动前端项目"));
                return;
            }
        } catch (Exception e) {
            log.error("检查 index.html 失败: {}", e.getMessage());
            ResponseUtils.outHttpJson(response, Result.failed("请启动前端项目"));
            return;
        }
        
        // 转发到 index.html，保持 URL 不变，支持 SPA 路由
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    // =================================== 全局兜底异常 ===================================

    /**
     * 全局未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);

        // 检测是否为 SSE 请求，SSE 请求不能返回 Result
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            log.warn("SSE 请求异常，无法返回 Result 对象: {}", e.getMessage());
            // SSE 请求异常时返回 null，让连接自然关闭
            return null;
        }

        // 异步发送异常通知
        try {
            SpringContextUtils.getBean(ExceptionNotifyUtils.class).notify(e);
        } catch (Exception notifyEx) {
            log.warn("Failed to notify exception: {}", notifyEx.getMessage());
        }
        return Result.failed("系统繁忙，请稍后重试");
    }
}
