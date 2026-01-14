package com.ciyocloud.itam.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.itam.entity.CategoriesEntity;
import com.ciyocloud.itam.enums.AssetCodeMode;
import com.ciyocloud.itam.service.CategoriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 资产编号生成工具类
 *
 * @author codeck
 * @since 2026-01-02
 */
@Component
@RequiredArgsConstructor
public class AssetCodeUtils {

    private static final String REDIS_KEY_PREFIX = "itam:asset_code:";
    private final CategoriesService categoriesService;
    private final RedisUtils redisUtils;

    /**
     * 根据分类ID生成资产编号
     *
     * @param categoryId 分类ID
     * @param mode       生成模式
     * @param length     定长自增模式下的序号长度（仅在 INCREMENT 模式下有效，默认6位）
     * @return 资产编号
     */
    public String generate(Long categoryId, AssetCodeMode mode, Integer length) {
        CategoriesEntity category = categoriesService.getById(categoryId);
        if (category == null || StrUtil.isBlank(category.getCode())) {
            throw new RuntimeException("分类不存在或未配置分类编码");
        }
        return generate(category.getCode(), mode, length);
    }

    /**
     * 根据分类编码生成资产编号
     *
     * @param categoryCode 分类编码
     * @param mode         生成模式
     * @param length       定长自增模式下的序号长度（仅在 INCREMENT 模式下有效，默认6位）
     * @return 资产编号
     */
    public String generate(String categoryCode, AssetCodeMode mode, Integer length) {
        if (StrUtil.isBlank(categoryCode)) {
            throw new IllegalArgumentException("分类编码不能为空");
        }

        if (AssetCodeMode.TIMESTAMP.equals(mode)) {
            // 时间戳模式：分类编码 + yyyyMMddHHmmssSSS
            return categoryCode + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");
        } else {
            // 定长自增模式：分类编码 + 指定长度自增序列
            String key = REDIS_KEY_PREFIX + categoryCode;
            long sequence = redisUtils.incr(key, 1);
            int seqLength = length != null ? length : 6;
            return categoryCode + StrUtil.fillBefore(String.valueOf(sequence), '0', seqLength);
        }
    }

    /**
     * 默认生成方式：根据分类ID生成，定长自增，6位长度
     *
     * @param categoryId 分类ID
     * @return 资产编号
     */
    public String generate(Long categoryId) {
        return generate(categoryId, AssetCodeMode.INCREMENT, 6);
    }
}
