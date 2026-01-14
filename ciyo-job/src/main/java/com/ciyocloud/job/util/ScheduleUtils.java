package com.ciyocloud.job.util;

import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.job.constant.JobConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * 定时任务工具类
 *
 * @author ruoyi
 */
public class ScheduleUtils {

    /**
     * 检查包名是否为白名单配置
     *
     * @param invokeTarget 目标字符串
     * @return 结果
     */
    public static boolean whiteList(String invokeTarget) {
        String packageName = StrUtil.subBefore(invokeTarget, "(", false);
        int count = StringUtils.countMatches(packageName, ".");
        if (count > 1) {
            return StringUtils.containsAnyIgnoreCase(invokeTarget, JobConstants.JOB_WHITELIST_STR);
        }
        try {
            Object obj = SpringContextUtils.getBean(StringUtils.split(invokeTarget, ".")[0]);
            if (obj == null) return false;
            String beanPackageName = obj.getClass().getPackage().getName();
            return StringUtils.containsAnyIgnoreCase(beanPackageName, JobConstants.JOB_WHITELIST_STR)
                    && !StringUtils.containsAnyIgnoreCase(beanPackageName, JobConstants.JOB_ERROR_STR);
        } catch (Exception e) {
            return false;
        }

    }
}
