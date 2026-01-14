package com.ciyocloud.system.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.system.constant.SysRedisKeyConstants;
import com.ciyocloud.system.entity.SysDictDataEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 字典工具类
 *
 * @author codeck
 */
public class DictUtils {
    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public static void setDictCache(String key, List<SysDictDataEntity> dictDatas) {
        SpringContextUtils.getBean(RedisUtils.class).set(getCacheKey(key), dictDatas);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    public static List<SysDictDataEntity> getDictCache(String key) {
        Object cacheObj = SpringContextUtils.getBean(RedisUtils.class).get(getCacheKey(key));
        if (ObjectUtil.isNotNull(cacheObj)) {
            List<SysDictDataEntity> dictDatas = Convert.toList(SysDictDataEntity.class, cacheObj);
            return dictDatas;
        }
        return null;
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String dictValue) {
        return getDictLabel(dictType, dictValue, SEPARATOR);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典值
     */
    public static String getDictValue(String dictType, String dictLabel) {
        return getDictValue(dictType, dictLabel, SEPARATOR);
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @param separator 分隔符
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String dictValue, String separator) {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictDataEntity> datas = getDictCache(dictType);
        if (StringUtils.containsAny(separator, dictValue) && ObjectUtil.isNotEmpty(datas)) {
            for (SysDictDataEntity dict : datas) {
                for (String value : dictValue.split(separator)) {
                    if (value.equals(dict.getDictValue())) {
                        propertyString.append(dict.getDictLabel() + separator);
                        break;
                    }
                }
            }
        } else {
            for (SysDictDataEntity dict : datas) {
                if (dictValue.equals(dict.getDictValue())) {
                    return dict.getDictLabel();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @param separator 分隔符
     * @return 字典值
     */
    public static String getDictValue(String dictType, String dictLabel, String separator) {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictDataEntity> datas = getDictCache(dictType);

        if (StringUtils.containsAny(separator, dictLabel) && ObjectUtil.isNotEmpty(datas)) {
            for (SysDictDataEntity dict : datas) {
                for (String label : dictLabel.split(separator)) {
                    if (label.equals(dict.getDictLabel())) {
                        propertyString.append(dict.getDictValue() + separator);
                        break;
                    }
                }
            }
        } else {
            for (SysDictDataEntity dict : datas) {
                if (dictLabel.equals(dict.getDictLabel())) {
                    return dict.getDictValue();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    public static void removeDictCache(String key) {
        SpringContextUtils.getBean(RedisUtils.class).remove(getCacheKey(key));
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache() {
        SpringContextUtils.getBean(RedisUtils.class).removePattern(SysRedisKeyConstants.SYS_DICT_KEY + "*");
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {
        return SysRedisKeyConstants.SYS_DICT_KEY + configKey;
    }
}
