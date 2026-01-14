package com.ciyocloud.job.manager;

import com.ciyocloud.job.constant.ScheduleConstants;
import com.ciyocloud.job.entity.SysJobEntity;
import com.ciyocloud.job.entity.SysJobLogEntity;
import com.ciyocloud.job.mapper.SysJobLogMapper;
import com.ciyocloud.job.util.CronUtils;
import com.ciyocloud.job.util.JobInvokeUtil;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务调度管理器
 *
 * @author codeck
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduleManager {

    private static final String JOB_Did_KEY = "sys:job:dedup:";
    private static final String JOB_EXEC_KEY = "sys:job:exec:";
    private final ThreadPoolTaskScheduler taskScheduler;
    private final SysJobLogMapper sysJobLogMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 调度任务
     */
    public void scheduleJob(SysJobEntity job) {
        cancelJob(job.getId());
        if (ScheduleConstants.Status.NORMAL.getValue().equals(job.getStatus())) {
            if (CronUtils.isValid(job.getCronExpression())) {
                ScheduledFuture<?> future = taskScheduler.schedule(createTask(job), new CronTrigger(job.getCronExpression()));
                if (future != null) {
                    scheduledTasks.put(job.getId(), future);
                }
            }
        }
    }

    /**
     * 取消任务
     */
    public void cancelJob(Long jobId) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobId);
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * 立即运行一次
     */
    public void run(SysJobEntity job) {
        taskScheduler.execute(createTask(job));
    }

    /**
     * 销毁时停止所有任务
     */
    @PreDestroy
    public void destroy() {
        scheduledTasks.values().forEach(future -> future.cancel(true));
        scheduledTasks.clear();
    }

    /**
     * 创建任务逻辑
     */
    private Runnable createTask(SysJobEntity job) {
        return () -> {
            // 1. 分布式去重锁: 避免多节点重复执行同一时刻的任务
            // 使用短暂的锁(例如5秒)，谁拿到锁谁执行，其他节点该次跳过
            // 如果任务频率极高(小于5s)，此逻辑需优化
            String dedupKey = JOB_Did_KEY + job.getId();
            Boolean dedupLock = stringRedisTemplate.opsForValue().setIfAbsent(dedupKey, "RUN", Duration.ofSeconds(5));
            if (Boolean.FALSE.equals(dedupLock)) {
                // 如果是手动触发(run一次)，可能不需要去重？
                // 但这里统一处理，如果5s内已经有节点跑了，就跳过
                return;
            }

            // 2. 判断是否允许并发
            // 0允许 1禁止
            boolean concurrent = job.getConcurrent() != null && job.getConcurrent() == 0;
            String execLockKey = JOB_EXEC_KEY + job.getId();
            String execLockVal = UUID.randomUUID().toString();

            if (!concurrent) {
                // 禁止并发：尝试获取执行锁
                // TTL设置长一点防止死锁，例如1小时
                Boolean execLock = stringRedisTemplate.opsForValue().setIfAbsent(execLockKey, execLockVal, Duration.ofHours(1));
                if (Boolean.FALSE.equals(execLock)) {
                    log.info("任务[{}]禁止并发，上一次任务未结束，本次跳过", job.getJobName());
                    return;
                }
            }

            SysJobLogEntity logEntity = new SysJobLogEntity();
            logEntity.setJobName(job.getJobName());
            logEntity.setJobGroup(job.getJobGroup());
            logEntity.setInvokeTarget(job.getInvokeTarget());
            logEntity.setStartTime(LocalDateTime.now());
            long start = System.currentTimeMillis();
            try {
                log.info("任务开始执行 - 名称:{}", job.getJobName());
                JobInvokeUtil.invokeMethod(job);
                logEntity.setStatus(0);
                logEntity.setJobMessage("任务执行成功");
                log.info("任务执行结束 - 名称:{} 耗时:{}ms", job.getJobName(), System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("任务执行失败 - 名称:{}", job.getJobName(), e);
                logEntity.setStatus(1);
                logEntity.setJobMessage("任务执行失败");
                String exceptionInfo = e.getMessage();
                if (e.getCause() != null) {
                    exceptionInfo = e.getCause().getMessage();
                }
                logEntity.setExceptionInfo(exceptionInfo != null && exceptionInfo.length() > 2000 ? exceptionInfo.substring(0, 2000) : exceptionInfo);
            } finally {
                // 如果禁止并发，且当前线程持有锁，则释放
                if (!concurrent) {
                    String currentVal = stringRedisTemplate.opsForValue().get(execLockKey);
                    if (execLockVal.equals(currentVal)) {
                        stringRedisTemplate.delete(execLockKey);
                    }
                }
                logEntity.setStopTime(LocalDateTime.now());
                sysJobLogMapper.insert(logEntity);
            }
        };
    }
}
