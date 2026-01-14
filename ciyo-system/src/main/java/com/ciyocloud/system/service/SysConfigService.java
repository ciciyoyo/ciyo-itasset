package com.ciyocloud.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysConfigEntity;

import java.util.List;

/**
 * 参数配置 服务层
 *
 * @author codeck
 */
public interface SysConfigService extends IService<SysConfigEntity> {


    /**
     * 根据key获取配置
     *
     * @param configKey
     * @return
     */
    String getConfigValueByKey(String configKey);

    SysConfigEntity getConfigByKey(String configKey);

    /**
     * 保存配置
     *
     * @param config
     * @return
     */
    int saveConfig(SysConfigEntity config);

    /**
     * 更新配置
     *
     * @param config
     * @return
     */
    int updateConfig(SysConfigEntity config);

    /**
     * 删除配置
     *
     * @param configIds
     */
    void deleteConfigByIds(List<Long> configIds);

    /**
     * 加载参数缓存数据
     */
    void loadingConfigCache();

    /**
     * 清空参数缓存数据
     */
    void clearConfigCache();

    /**
     * 重置参数缓存数据
     */
    void resetConfigCache();

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数信息
     * @return 结果
     */
    String checkConfigKeyUnique(SysConfigEntity config);


}
