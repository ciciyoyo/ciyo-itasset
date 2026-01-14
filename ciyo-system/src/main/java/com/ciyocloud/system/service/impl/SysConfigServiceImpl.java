package com.ciyocloud.system.service.impl;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.system.constant.SysRedisKeyConstants;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysConfigEntity;
import com.ciyocloud.system.mapper.SysConfigMapper;
import com.ciyocloud.system.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements SysConfigService {
    private final SysConfigMapper configMapper;

    private final RedisUtils redisUtils;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        loadingConfigCache();
    }


    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String getConfigValueByKey(String configKey) {
        String configValue = Convert.toStr(redisUtils.get(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        SysConfigEntity config = getConfigByKey(configKey);
        if (ObjectUtil.isNotNull(config)) {
            return config.getConfigValue();
        }
        return StringUtils.EMPTY;
    }


    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public SysConfigEntity getConfigByKey(String configKey) {
        SysConfigEntity config = new SysConfigEntity();
        config.setConfigKey(configKey);
        SysConfigEntity retConfig = configMapper.selectOne(Wrappers.<SysConfigEntity>lambdaQuery().eq(SysConfigEntity::getConfigKey, configKey));
        if (ObjectUtil.isNotNull(retConfig)) {
            redisUtils.set(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig;
        }
        return retConfig;
    }


    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int saveConfig(SysConfigEntity config) {
        int row = configMapper.insert(config);
        if (row > 0) {
            redisUtils.set(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfigEntity config) {
        int row = configMapper.updateById(config);
        if (row > 0) {
            redisUtils.set(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     * @return 结果
     */
    @Override
    public void deleteConfigByIds(List<Long> configIds) {
        for (Long configId : configIds) {
            SysConfigEntity config = this.getById(configId);
            if (StrUtil.equals(UserConstants.YES, config.getConfigType())) {
                throw new BaseException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            this.removeByIds(configIds);
            redisUtils.remove(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache() {
        List<SysConfigEntity> configsList = configMapper.selectList(null);
        for (SysConfigEntity config : configsList) {
            redisUtils.set(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache() {
        redisUtils.removePattern(SysRedisKeyConstants.SYS_CONFIG_KEY + "*");
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public String checkConfigKeyUnique(SysConfigEntity config) {
        Long configId = ObjectUtil.isNull(config.getId()) ? -1L : config.getId();
        SysConfigEntity info = configMapper.selectOne(Wrappers.<SysConfigEntity>lambdaQuery().eq(SysConfigEntity::getConfigKey, config.getConfigKey()));
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != configId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey) {
        return SysRedisKeyConstants.SYS_CONFIG_KEY + configKey;
    }
}
