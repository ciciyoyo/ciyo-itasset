package com.ciyocloud.job.job;

import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.job.service.SysJobLogService;
import org.springframework.stereotype.Component;

/**
 * 清除日志任务
 *
 * @author codeck
 */
@Component("cleanJobLogTask")
public class CleanJobLogJob {


    public void cleanAll() {
        SpringContextUtils.getBean(SysJobLogService.class).cleanAll();
        System.out.println("执行清除日志任务");
    }
}
