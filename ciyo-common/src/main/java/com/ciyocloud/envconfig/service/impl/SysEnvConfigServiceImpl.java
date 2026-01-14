package com.ciyocloud.envconfig.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.constant.ConfigConstants;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.entity.SystemEnvConfig;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.envconfig.entity.SysEnvConfigEntity;
import com.ciyocloud.envconfig.entity.event.EnvConfigRefreshEvent;
import com.ciyocloud.envconfig.mapper.SysEnvConfigMapper;
import com.ciyocloud.envconfig.service.SysEnvConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统环境配置Service业务层处理
 *
 * @author codeck-gen
 * @since 2021-12-28 15:06:17
 */
@Service
@RequiredArgsConstructor
public class SysEnvConfigServiceImpl extends ServiceImpl<SysEnvConfigMapper, SysEnvConfigEntity> implements SysEnvConfigService {

    /**
     * 本地缓存
     */
    private final Map<String, String> cacheMap = new ConcurrentHashMap<>();

    private final RedisUtils redisUtils;


    @PostConstruct
    public void initCache() {
        List<SysEnvConfigEntity> list = this.list();
        list.forEach(config -> {
            cacheMap.put(config.getEnvKey(), JsonUtils.objToJson(config.getEnvValue()));
            redisUtils.set(StrUtil.format(RedisKeyConstants.ENV_CONFIG, config.getEnvKey()), JsonUtils.objToJson(config.getEnvValue()));
        });

    }

    @Override
    public SysEnvConfigEntity getByKey(String key) {
        return baseMapper.selectOne(Wrappers.<SysEnvConfigEntity>lambdaQuery().eq(SysEnvConfigEntity::getEnvKey, key));
    }

    @Override
    public SystemEnvConfig getSystemEnvConfig() {
        return JsonUtils.jsonToObj(getValueByKey(ConfigConstants.SYSTEM_INFO_CONFIG), SystemEnvConfig.class);
    }


    @Override
    public String getValueByKey(String key) {
        String cacheValue = cacheMap.get(key);
        if (StringUtils.isNotEmpty(cacheValue)) {
            return cacheValue;
        }
        String configValue = Convert.toStr(redisUtils.get(StrUtil.format(RedisKeyConstants.ENV_CONFIG, key)));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        SysEnvConfigEntity config = getByKey(key);
        if (ObjectUtil.isNotNull(config)) {
            return JsonUtils.objToJson(config.getEnvValue());
        }
        return StringUtils.EMPTY;
    }


    @Override
    public void saveConfig(SysEnvConfigEntity config) {
        SysEnvConfigEntity envConfig = getByKey(config.getEnvKey());
        if (ObjectUtil.isNull(envConfig)) {
            envConfig = new SysEnvConfigEntity();
        }
        envConfig.setEnvKey(config.getEnvKey());
        envConfig.setEnvValue(config.getEnvValue());
        this.saveOrUpdate(envConfig);
        cacheMap.put(config.getEnvKey(), JsonUtils.objToJson(config.getEnvValue()));
        redisUtils.set(StrUtil.format(RedisKeyConstants.ENV_CONFIG, config.getEnvKey()), JsonUtils.objToJson(config.getEnvValue()));
        SpringContextUtils.publishEvent(new EnvConfigRefreshEvent(this, config));
    }
}
