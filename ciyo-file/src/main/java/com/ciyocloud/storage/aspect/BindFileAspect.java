package com.ciyocloud.storage.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.storage.annotation.BindFile;
import com.ciyocloud.storage.event.FileBizIdUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件业务绑定切面
 *
 * @author ciyo
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BindFileAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    @AfterReturning(value = "@annotation(bindFile)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, BindFile bindFile, Object result) {
        try {
            // 1. 解析上下文
            EvaluationContext context = getEvaluationContext(joinPoint, result);

            // 2. 获取文件ID列表
            List<Long> fileIds = getFileIds(bindFile.fileIds(), context);
            List<String> fileUrls = getFileUrls(bindFile.fileUrls(), context);

            if (CollUtil.isEmpty(fileIds) && CollUtil.isEmpty(fileUrls)) {
                return;
            }

            // 3. 获取业务ID
            String bizId = getBizId(bindFile.bizId(), context);
            if (StrUtil.isBlank(bizId)) {
                log.warn("BindFile: bizId is blank, expression: {}", bindFile.bizId());
                return;
            }

            // 4. 发布事件
            if (CollUtil.isNotEmpty(fileIds)) {
                eventPublisher.publishEvent(new FileBizIdUpdateEvent(this, fileIds, bizId, bindFile.bizType(), bindFile.bizField()));
            }
            if (CollUtil.isNotEmpty(fileUrls)) {
                eventPublisher.publishEvent(new FileBizIdUpdateEvent(this, fileUrls, bizId, bindFile.bizType(), bindFile.bizField(), true));
            }

        } catch (Exception e) {
            log.error("BindFile aspect error", e);
        }
    }

    private EvaluationContext getEvaluationContext(JoinPoint joinPoint, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 设置返回值
        context.setVariable("result", result);

        // 设置参数变量
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return context;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getFileIds(String expressionStr, EvaluationContext context) {
        if (StrUtil.isBlank(expressionStr)) {
            return null;
        }
        Expression expression = parser.parseExpression(expressionStr);
        Object value = expression.getValue(context);

        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(item -> {
                        if (item instanceof Long) {
                            return (Long) item;
                        } else if (item instanceof String) {
                            return Long.valueOf((String) item);
                        } else if (item instanceof Integer) {
                            return ((Integer) item).longValue();
                        }
                        return null;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } else if (value instanceof Number) {
            return cn.hutool.core.collection.ListUtil.of(((Number) value).longValue());
        } else if (value instanceof String) {
            String strVal = (String) value;
            if (StrUtil.contains(strVal, ",")) {
                return StrUtil.split(strVal, ',').stream()
                        .filter(StrUtil::isNotBlank)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }
            if (StrUtil.isNotBlank(strVal)) {
                return cn.hutool.core.collection.ListUtil.of(Long.valueOf(strVal));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getFileUrls(String expressionStr, EvaluationContext context) {
        if (StrUtil.isBlank(expressionStr)) {
            return null;
        }
        Expression expression = parser.parseExpression(expressionStr);
        Object value = expression.getValue(context);

        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(item -> item != null ? item.toString() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } else if (value instanceof String) {
            // Handle single URL case if expression returns a String
            String strVal = (String) value;
            if (StrUtil.contains(strVal, ",")) {
                return StrUtil.split(strVal, ',');
            }
            if (StrUtil.isNotBlank(strVal)) {
                return cn.hutool.core.collection.ListUtil.of(strVal);
            }
        }
        return null;
    }

    private String getBizId(String expressionStr, EvaluationContext context) {
        if (StrUtil.isBlank(expressionStr)) {
            return null;
        }
        Expression expression = parser.parseExpression(expressionStr);
        Object value = expression.getValue(context);
        return value != null ? value.toString() : null;
    }

}
