package com.ciyocloud.storage.annotation;

import java.lang.annotation.*;

/**
 * 自动绑定文件业务ID注解
 *
 * @author ciyo
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindFile {

    /**
     * 业务类型
     */
    String bizType();

    /**
     * 业务字段 (可选)
     */
    String bizField() default "";

    /**
     * 获取文件ID集合的SpEL表达式 (二选一)
     * <p>例如: #dto.fileIds</p>
     */
    String fileIds() default "";

    /**
     * 获取文件URL集合的SpEL表达式 (二选一)
     * <p>例如: #dto.fileUrls</p>
     */
    String fileUrls() default "";

    /**
     * 获取业务ID的SpEL表达式
     * <p>默认为返回值中的id: #result.id</p>
     */
    String bizId() default "#result.id";
}
