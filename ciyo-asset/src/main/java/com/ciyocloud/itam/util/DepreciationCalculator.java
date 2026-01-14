package com.ciyocloud.itam.util;

import com.ciyocloud.itam.entity.DepreciationsEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 折旧计算工具类
 * 提供资产折旧价值计算功能
 *
 * @author codeck
 * @since 2026-01-01
 */
@Slf4j
public class DepreciationCalculator {

    /**
     * 计算资产当前价值
     *
     * @param purchaseCost 购买成本
     * @param purchaseDate 购买日期
     * @param currentDate  当前日期
     * @param rule         折旧规则
     * @return 当前价值
     */
    public static BigDecimal calculateCurrentValue(BigDecimal purchaseCost,
                                                   LocalDate purchaseDate,
                                                   LocalDate currentDate,
                                                   DepreciationsEntity rule) {
        if (purchaseCost == null || purchaseCost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (purchaseDate == null || currentDate == null || rule == null) {
            return purchaseCost;
        }

        if (currentDate.isBefore(purchaseDate)) {
            return purchaseCost;
        }

        // 计算已过月数
        long monthsPassed = ChronoUnit.MONTHS.between(purchaseDate, currentDate);
        if (monthsPassed < 0) {
            monthsPassed = 0;
        }

        // 计算折旧率
        BigDecimal totalDepreciationRatio = calculateDepreciationRatio(monthsPassed, rule);

        // 限制最大折旧率 1.0
        if (totalDepreciationRatio.compareTo(BigDecimal.ONE) > 0) {
            totalDepreciationRatio = BigDecimal.ONE;
        }

        // 计算当前价值
        BigDecimal depreciationAmount = purchaseCost.multiply(totalDepreciationRatio);
        BigDecimal currentValue = purchaseCost.subtract(depreciationAmount);

        // 处理最低价值 (Floor)
        currentValue = applyFloorValue(currentValue, purchaseCost, rule);

        // 确保非负
        if (currentValue.compareTo(BigDecimal.ZERO) < 0) {
            currentValue = BigDecimal.ZERO;
        }

        return currentValue.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算累计折旧
     *
     * @param purchaseCost 购买成本
     * @param currentValue 当前价值
     * @return 累计折旧
     */
    public static BigDecimal calculateAccumulatedDepreciation(BigDecimal purchaseCost,
                                                              BigDecimal currentValue) {
        if (purchaseCost == null || currentValue == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal depreciation = purchaseCost.subtract(currentValue);
        return depreciation.compareTo(BigDecimal.ZERO) < 0 ?
                BigDecimal.ZERO : depreciation.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算折旧率
     *
     * @param monthsPassed 已过月数
     * @param rule         折旧规则
     * @return 总折旧率
     */
    private static BigDecimal calculateDepreciationRatio(long monthsPassed,
                                                         DepreciationsEntity rule) {
        BigDecimal totalDepreciationRatio = BigDecimal.ZERO;

        List<DepreciationsEntity.RulePeriod> stages = rule.getStages();
        if (CollectionUtils.isEmpty(stages)) {
            // 无阶段配置，使用简单直线折旧
            return calculateStraightLineDepreciation(monthsPassed, rule);
        }

        // 分阶段计算折旧
        long currentPeriodStartMonth = 0;

        for (DepreciationsEntity.RulePeriod stage : stages) {
            // 转换周期为月
            long stageMonths = convertToMonths(stage.getPeriod(), stage.getUnit());
            BigDecimal stageRatio = stage.getRatio();

            if (stageMonths <= 0) {
                continue;
            }

            if (monthsPassed >= currentPeriodStartMonth + stageMonths) {
                // 完整经过该阶段
                totalDepreciationRatio = totalDepreciationRatio.add(stageRatio);
                currentPeriodStartMonth += stageMonths;
            } else if (monthsPassed > currentPeriodStartMonth) {
                // 处于该阶段中间
                long monthsInThisStage = monthsPassed - currentPeriodStartMonth;
                // 线性折算该阶段的比例: (monthsInThisStage / stageMonths) * stageRatio
                BigDecimal partialRatio = stageRatio
                        .multiply(new BigDecimal(monthsInThisStage))
                        .divide(new BigDecimal(stageMonths), 6, RoundingMode.HALF_UP);
                totalDepreciationRatio = totalDepreciationRatio.add(partialRatio);
                break; // 之后的阶段还没到
            } else {
                break; // 还没到该阶段
            }
        }

        return totalDepreciationRatio;
    }

    /**
     * 简单直线折旧计算
     *
     * @param monthsPassed 已过月数
     * @param rule         折旧规则
     * @return 折旧率
     */
    private static BigDecimal calculateStraightLineDepreciation(long monthsPassed,
                                                                DepreciationsEntity rule) {
        // 如果规则中定义了总月数，使用直线折旧
        if (rule.getMonths() != null && rule.getMonths() > 0) {
            BigDecimal depreciationRatio = new BigDecimal(monthsPassed)
                    .divide(new BigDecimal(rule.getMonths()), 6, RoundingMode.HALF_UP);

            // 考虑残值率
            if (rule.getFloorType() != null && "percent".equalsIgnoreCase(rule.getFloorType())
                    && rule.getFloorVal() != null) {
                // 最大折旧率 = 1 - 残值率
                BigDecimal maxDepreciationRatio = BigDecimal.ONE.subtract(rule.getFloorVal());
                if (depreciationRatio.compareTo(maxDepreciationRatio) > 0) {
                    depreciationRatio = maxDepreciationRatio;
                }
            }

            return depreciationRatio;
        }

        return BigDecimal.ZERO;
    }

    /**
     * 应用最低价值限制
     *
     * @param currentValue 当前计算的价值
     * @param purchaseCost 购买成本
     * @param rule         折旧规则
     * @return 应用限制后的价值
     */
    private static BigDecimal applyFloorValue(BigDecimal currentValue,
                                              BigDecimal purchaseCost,
                                              DepreciationsEntity rule) {
        if (rule.getFloorType() == null || rule.getFloorVal() == null) {
            return currentValue;
        }

        if ("amount".equalsIgnoreCase(rule.getFloorType())) {
            // 绝对金额作为最低价值
            if (currentValue.compareTo(rule.getFloorVal()) < 0) {
                return rule.getFloorVal();
            }
        } else if ("percent".equalsIgnoreCase(rule.getFloorType())) {
            // 百分比作为最低价值（残值率）
            BigDecimal minValue = purchaseCost.multiply(rule.getFloorVal());
            if (currentValue.compareTo(minValue) < 0) {
                return minValue;
            }
        }

        return currentValue;
    }

    /**
     * 转换周期为月数
     *
     * @param period 周期数
     * @param unit   周期单位
     * @return 月数
     */
    private static long convertToMonths(Integer period, DepreciationsEntity.PeriodUnit unit) {
        if (period == null || unit == null) {
            return 0;
        }

        switch (unit) {
            case YEAR:
                return period * 12L;
            case MONTH:
                return period;
            case DAY:
                return period / 30L; // 近似值
            default:
                return period;
        }
    }

    /**
     * 计算资产剩余使用寿命（月）
     *
     * @param purchaseDate 购买日期
     * @param currentDate  当前日期
     * @param rule         折旧规则
     * @return 剩余使用寿命（月），如果无法计算返回 null
     */
    public static Long calculateRemainingLifeMonths(LocalDate purchaseDate,
                                                    LocalDate currentDate,
                                                    DepreciationsEntity rule) {
        if (purchaseDate == null || currentDate == null || rule == null) {
            return null;
        }

        // 计算总寿命（月）
        Long totalLifeMonths = calculateTotalLifeMonths(rule);
        if (totalLifeMonths == null) {
            return null;
        }

        // 计算已使用月数
        long usedMonths = ChronoUnit.MONTHS.between(purchaseDate, currentDate);
        if (usedMonths < 0) {
            usedMonths = 0;
        }

        // 计算剩余月数
        long remainingMonths = totalLifeMonths - usedMonths;
        return remainingMonths > 0 ? remainingMonths : 0L;
    }

    /**
     * 计算总使用寿命（月）
     *
     * @param rule 折旧规则
     * @return 总使用寿命（月），如果无法计算返回 null
     */
    public static Long calculateTotalLifeMonths(DepreciationsEntity rule) {
        if (rule == null) {
            return null;
        }

        // 优先使用 stages 计算
        List<DepreciationsEntity.RulePeriod> stages = rule.getStages();
        if (!CollectionUtils.isEmpty(stages)) {
            long totalMonths = 0;
            for (DepreciationsEntity.RulePeriod stage : stages) {
                totalMonths += convertToMonths(stage.getPeriod(), stage.getUnit());
            }
            return totalMonths > 0 ? totalMonths : null;
        }

        // 使用 months 字段
        if (rule.getMonths() != null && rule.getMonths() > 0) {
            return rule.getMonths().longValue();
        }

        return null;
    }

    /**
     * 判断资产是否已完全折旧
     *
     * @param currentValue 当前价值
     * @param purchaseCost 购买成本
     * @param rule         折旧规则
     * @return true 如果已完全折旧
     */
    public static boolean isFullyDepreciated(BigDecimal currentValue,
                                             BigDecimal purchaseCost,
                                             DepreciationsEntity rule) {
        if (currentValue == null || purchaseCost == null) {
            return false;
        }

        // 如果当前价值为0，认为已完全折旧
        if (currentValue.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        // 如果有残值率，检查是否已达到残值
        if (rule != null && "percent".equalsIgnoreCase(rule.getFloorType())
                && rule.getFloorVal() != null) {
            BigDecimal residualValue = purchaseCost.multiply(rule.getFloorVal());
            // 如果当前价值等于或接近残值，认为已完全折旧
            return currentValue.subtract(residualValue).abs()
                    .compareTo(new BigDecimal("0.01")) <= 0;
        }

        return false;
    }
}
