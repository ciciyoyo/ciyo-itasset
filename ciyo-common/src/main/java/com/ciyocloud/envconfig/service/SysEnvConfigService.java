package com.ciyocloud.envconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.common.entity.SystemEnvConfig;
import com.ciyocloud.envconfig.entity.SysEnvConfigEntity;

/**
 * 系统环境配置Service接口
 *
 * @author codeck-gen
 * @since 2021-12-28 15:06:17
 */
public interface SysEnvConfigService extends IService<SysEnvConfigEntity> {

    /**
     * 获取配置
     *
     * @param key 配置key
     * @return {@link SysEnvConfigEntity}
     */
    SysEnvConfigEntity getByKey(String key);


    /**
     * 获取系统基础配置
     *
     * @return {@link SystemEnvConfig}
     */
    SystemEnvConfig getSystemEnvConfig();

    /**
     * 获取配置值
     *
     * @param key 配置key
     * @return 配置值
     */
    String getValueByKey(String key);

    /**
     * 保存配置
     *
     * @param config 配置
     */
    void saveConfig(SysEnvConfigEntity config);
}
