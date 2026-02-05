package com.ciyocloud.excel.core;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.ciyocloud.common.sse.context.SseContext;
import com.ciyocloud.common.sse.util.SseAsyncProcessUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 支持 SSE 进度推送的 Excel 导入监听器
 * 扩展默认监听器，增加进度推送功能
 *
 * @author codeck
 * @create 2026/01/28
 */
@Slf4j
public class SseProgressExcelListener<T> extends DefaultExcelListener<T> {

    /**
     * 进度标识key
     */
    private final String progressKey;

    /**
     * 用户ID
     */
    private final Long userId;
    /**
     * 进度更新间隔（每处理多少行更新一次进度）
     */
    private final int progressInterval;
    /**
     * 进度更新时间间隔（毫秒）
     */
    private final long progressTimeInterval;
    /**
     * 已处理行数
     */
    private int processedRows = 0;
    /**
     * 成功处理行数
     */
    private int successRows = 0;
    /**
     * 失败处理行数
     */
    private int failureRows = 0;
    /**
     * 总行数（估算）
     */
    private long totalRows = 0;
    /**
     * 最后一次更新进度的时间
     */
    private long lastProgressTime = 0;

    public SseProgressExcelListener(String progressKey, Long userId) {
        this(progressKey, userId, true, 100, 1000);
    }

    public SseProgressExcelListener(String progressKey, Long userId, boolean isValidate, int progressInterval, long progressTimeInterval) {
        super(isValidate);
        this.progressKey = progressKey;
        this.userId = userId;
        this.progressInterval = progressInterval;
        this.progressTimeInterval = progressTimeInterval;
        // 初始化 SSE 上下文
        SseContext.init(userId, progressKey, "web");
        setProcessTips("开始处理Excel文件...");
    }

    /**
     * 设置总行数（用于精确计算进度百分比，可选调用）
     */
    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
        if (totalRows > 0) {
            SseAsyncProcessUtils.setProcess(0, totalRows);
        }
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        try {
            // 调用子类实现的数据处理逻辑
            super.invoke(data, context);
            processData(data, context);

            // 更新进度
            processedRows++;
            successRows++;
            updateProgress();
        } catch (Exception e) {
            // 处理错误
            processedRows++;
            failureRows++;
            String errorMsg = String.format("第%d行数据处理失败: %s", context.readRowHolder().getRowIndex() + 1, e.getMessage());
            SseAsyncProcessUtils.setError(errorMsg);
        }
    }

    /**
     * 处理单行数据的方法，子类可以重写此方法实现具体的业务逻辑
     * 默认实现调用父类的invoke方法
     *
     * @param data    Excel行数据
     * @param context 解析上下文
     */
    protected void processData(T data, AnalysisContext context) {
    }


    @Override
    public void onException(Exception exception, AnalysisContext context) {
        try {
            super.onException(exception, context);
        } catch (Exception e) {
            // 更新错误进度
            String errorMsg = String.format("Excel解析异常: %s", e.getMessage());
            setProcessTips(errorMsg);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            super.doAfterAllAnalysed(context);
            setComplete();
            log.info("Excel文件处理完成，共处理 {} 行数据，成功 {} 行，失败 {} 行", processedRows, successRows, failureRows);
        } finally {
            // 清理 SSE 上下文，防止内存泄漏或线程污染
            SseContext.clear();
        }
    }

    /**
     * 更新进度
     */
    private void updateProgress() {
        long currentTime = System.currentTimeMillis();

        // 按行数间隔或时间间隔更新进度
        if (processedRows % progressInterval == 0 || (currentTime - lastProgressTime) >= progressTimeInterval) {

            if (totalRows > 0) {
                // 有总行数，精确计算进度
                SseAsyncProcessUtils.setProcess(processedRows, totalRows);
                setProcessTips(String.format("正在处理Excel文件... (%d/%d) 成功:%d 失败:%d", processedRows, totalRows, successRows, failureRows));
            } else {
                // 无总行数，按处理行数显示
                setProcessTips(String.format("正在处理Excel文件... (已处理%d行) 成功:%d 失败:%d", processedRows, successRows, failureRows));
            }

            lastProgressTime = currentTime;
            log.debug("更新Excel处理进度: {}/{}, 成功:{}, 失败:{}", processedRows, totalRows, successRows, failureRows);
        }
    }

    /**
     * 设置完成状态（进度100%）
     */
    public void setComplete() {
        // 设置进度为100%
        if (totalRows > 0) {
            SseAsyncProcessUtils.setProcess(100, "");
        }

        String message;
        if (failureRows == 0) {
            message = String.format("Excel导入完成，共处理 %d 行数据，全部成功", processedRows);
        } else {
            message = String.format("Excel导入完成，共处理 %d 行数据，成功 <span style='color: #52c41a; font-weight: bold;'>%d</span> 行，失败 <span style='color: #ff4d4f; font-weight: bold;'>%d</span> 行", processedRows, successRows, failureRows);
        }
        SseAsyncProcessUtils.setFinish(message);
    }

    /**
     * 设置错误状态
     */
    public void setError(String errorMsg) {
        SseAsyncProcessUtils.setError(errorMsg);
    }

    /**
     * 统一设置进度提示信息
     */
    private void setProcessTips(String tips) {
        if (StrUtil.isNotBlank(tips)) {
            SseAsyncProcessUtils.setTips(tips);
        }
    }


}
