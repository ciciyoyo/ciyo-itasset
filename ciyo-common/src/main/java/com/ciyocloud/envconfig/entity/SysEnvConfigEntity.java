package com.ciyocloud.envconfig.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.common.mybatis.handler.JacksonTypeHandler;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 系统环境配置对象 sys_env_config
 *
 * @author codeck-gen
 * @since 2021-12-28 15:06:17
 */
@Data
@TableName(value = "sys_env_config", autoResultMap = true)
public class SysEnvConfigEntity extends BaseEntity {


    /**
     * 配置key
     */
    @NotBlank
    private String envKey;

    /**
     * 参数键值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> envValue;


}
