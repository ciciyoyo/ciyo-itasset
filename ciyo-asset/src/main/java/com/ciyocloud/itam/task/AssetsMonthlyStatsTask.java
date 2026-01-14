package com.ciyocloud.itam.task;

import com.ciyocloud.itam.service.AssetsMonthlyStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 资产月度统计定时任务
 *
 * @author codeck
 * @since 2026-01-02
 */
@Slf4j
@Component("assetsMonthlyStatsTask")
@RequiredArgsConstructor
public class AssetsMonthlyStatsTask {

    private final AssetsMonthlyStatsService assetsMonthlyStatsService;

    /**
     * 每日统计任务 (默认统计昨天)
     * 建议配置 cron: 0 0 1 * * ? (每天凌晨1点执行)
     */
    public void calculateDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始执行资产每日统计任务, 日期: {}", yesterday);
        assetsMonthlyStatsService.calculateDailyStats(yesterday);
        log.info("资产每日统计任务执行完成, 日期: {}", yesterday);
    }

    /**
     * 手动执行统计任务
     *
     * @param dateStr 统计日期 (yyyy-MM-dd), 为空则统计昨天
     */
    public void calculateDailyStats(String dateStr) {
        LocalDate date;
        if (!StringUtils.hasText(dateStr)) {
            date = LocalDate.now().minusDays(1);
        } else {
            try {
                date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.error("日期格式错误: {}, 请使用 yyyy-MM-dd 格式", dateStr);
                return;
            }
        }

        log.info("开始执行手动资产统计任务, 日期: {}", date);
        assetsMonthlyStatsService.calculateDailyStats(date);
        log.info("手动资产统计任务执行完成, 日期: {}", date);
    }
}
