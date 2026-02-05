
package com.ciyocloud.common.validator;


import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.MessageUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * @description: hibernate 校验工具类
 * 不通过注解使用 通过工具类返回自定义结果
 * @author: codeck
 * @create: 2018-10-12 10:20
 **/
public class ValidatorUtils {

    private static final Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 校验对象
     *
     * @param object 待校验对象
     * @param groups 待校验的组
     * @throws BaseException 校验不通过，BaseException
     */
    public static void validateEntity(Object object, Class<?>... groups)
            throws BaseException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<Object> constraint : constraintViolations) {
                msg.append(constraint.getMessage());
            }
            throw new BaseException(msg.toString());
        }
    }

    public static void validateEntity(Object object)
            throws BaseException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        if (!constraintViolations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<Object> constraint : constraintViolations) {
                String message = constraint.getMessage();
                if (StrUtil.startWith(message, "{") && StrUtil.endWith(message, "}")) {
                    msg.append(MessageUtils.message(message.substring(1, message.length() - 1))).append("<br>");
                } else {
                    msg.append(constraint.getMessage()).append("<br>");
                }
            }
            // 移除最后一个换行符
            msg.delete(msg.length() - 4, msg.length());
            throw new BaseException(msg.toString());
        }
    }
}
