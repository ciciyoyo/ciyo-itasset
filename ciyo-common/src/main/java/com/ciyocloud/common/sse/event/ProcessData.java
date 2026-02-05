package com.ciyocloud.common.sse.event;

import cn.hutool.core.util.NumberUtil;

import java.math.BigDecimal;

/**
 * 进度数据 - 使用 JDK 18 Record 简化定义
 *
 * @param rate    进度百分比（0-100）
 * @param url     结果URL（可选）
 * @param current 当前处理数
 * @param total   总数
 * @param tips    提示信息
 * @author codeck
 * @create 2026/01/31
 */
public record ProcessData(
        int rate,
        String url,
        long current,
        long total,
        String tips
) {
    /**
     * 最大完成率
     */
    private static final int MAX_PROCESS_RATE = 100;

    /**
     * 仅包含进度百分比
     */
    public ProcessData(int rate) {
        this(rate, "", 0, 0, "");
    }

    /**
     * 进度百分比 + 消息
     */
    public ProcessData(int rate, String message) {
        this(rate, message, 0, 0, "");
    }

    /**
     * 当前数/总数自动计算百分比
     */
    public ProcessData(long current, long total) {
        this(calculateRate(current, total), "", current, total, "");
    }

    /**
     * 完成状态 + URL
     */
    public static ProcessData completeWithUrl(String url) {
        return new ProcessData(MAX_PROCESS_RATE, url, 0, 0, "");
    }

    /**
     * 完成状态 + 提示信息
     */
    public static ProcessData completeWithTips(String tips) {
        return new ProcessData(MAX_PROCESS_RATE, "", 0, 0, tips);
    }

    /**
     * 仅提示信息（进度为0）
     */
    public static ProcessData tipsOnly(String tips) {
        return new ProcessData(0, "", 0, 0, tips);
    }

    /**
     * 计算进度百分比
     */
    private static int calculateRate(long current, long total) {
        return total > 0
                ? NumberUtil.round(NumberUtil.div(current, total), 2)
                .multiply(new BigDecimal(100))
                .intValue()
                : 0;
    }

    /**
     * 判断是否已完成
     */
    public boolean isComplete() {
        return rate >= MAX_PROCESS_RATE;
    }
}
