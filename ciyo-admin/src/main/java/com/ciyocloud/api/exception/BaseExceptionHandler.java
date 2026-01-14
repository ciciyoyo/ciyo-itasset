package com.ciyocloud.api.exception;

import cn.hutool.core.exceptions.ValidateException;
import com.ciyocloud.common.constant.ResponseCodeConstants;
import com.ciyocloud.common.exception.AuthorizationException;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.exception.PromptException;
import com.ciyocloud.common.util.ExceptionNotifyUtils;
import com.ciyocloud.common.util.Result;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.idempotent.exception.RepeatSubmitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常处理器
 *
 * @author codeck
 */
@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {


    @ExceptionHandler(NoHandlerFoundException.class)
    public Result handlerNoFoundException(NoHandlerFoundException e) {
        log.error(e.getMessage(), e);
        return Result.failed(ResponseCodeConstants.NOT_FOUND, "{sys.path.error}");
    }


    @ExceptionHandler(AuthorizationException.class)
    public Result handlerAuthorizationException(AuthorizationException e) {
        log.error(e.getMessage(), e);
        return Result.failed(ResponseCodeConstants.UNAUTHORIZED, "{sys.login.expired}");
    }


    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.getMessage(), e);
        return Result.failed("{sys.data.exists}");
    }

    @ExceptionHandler({ValidateException.class})
    public Result handleValidateException(ValidateException e) {
        return Result.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("System Exception", e);
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("System Exception", e);
        return Result.failed("发生异常" + e.getMessage());
    }

    /**
     * 方法参数未通过校验
     *
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("params error", e);
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String message = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";"));
        return Result.failed(message);
    }


    @ExceptionHandler(RepeatSubmitException.class)
    public Result handleRepeatSubmitException(RepeatSubmitException e) {
        return Result.success(e.getMessage());
    }


    /**
     * 基础异常
     */
    @ExceptionHandler({BaseException.class, PromptException.class})
    public Result handleBaseException(BaseException e) {
        return Result.restResult(null, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public Result handleSqlException(SQLException e) {
        log.error("sql错误", e);
        return Result.failed("server error");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error(e.getMessage(), e);
        SpringContextUtils.getBean(ExceptionNotifyUtils.class).notify(e);
        return Result.failed(e.getMessage());
    }
}
