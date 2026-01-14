package com.ciyocloud.message.handle.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.message.entity.MsgSendResult;
import com.ciyocloud.message.handle.SendMsgHandle;
import com.ciyocloud.message.thirdparty.sms.SmsConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ciyocloud.message.constant.MessageConstants.SMS_SEND_COUNT_LIMIT;
import static com.ciyocloud.message.constant.MessageRedisConstants.SMS_SEND_COUNT_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSendMsgHandle implements SendMsgHandle {

    private final RedisUtils redisUtils;

    @Override
    public MsgSendResult sendMsg(String sentTo, String title, String content, String thirdTemplateId, Map<String, Object> params) {
        try {
            // 获取短信发送上限
            Integer sendSmsLimit = redisUtils.get(SMS_SEND_COUNT_LIMIT, Integer.class);
            boolean isLimitEnabled = sendSmsLimit != null && sendSmsLimit > 0;

            // 计算 Redis key
            String redisKey = StrUtil.format(SMS_SEND_COUNT_KEY, sentTo);
            Integer currentCount = 0;
            // 如果有发送限制，则检查当前已发送次数
            if (isLimitEnabled) {
                currentCount = redisUtils.get(redisKey, Integer.class);
                currentCount = (currentCount == null) ? 0 : currentCount;

                if (currentCount >= sendSmsLimit) {
                    return new MsgSendResult(false, "今日短信发送次数已达上限");
                }
            }

            // 发送短信
            MsgSendResult msgSendResult = SmsConfiguration.getSmsService().sendSms(sentTo, thirdTemplateId, params);
            if (msgSendResult.getSuccess() && isLimitEnabled) {
                // Redis 计数增加
                redisUtils.incr(redisKey, 1);
                // 只在 **第一次创建 key 时** 设置过期时间
                if (currentCount == 0) {
                    long expireTime = DateUtil.between(DateUtil.date(), DateUtil.beginOfDay(DateUtil.tomorrow()), DateUnit.SECOND);
                    redisUtils.setExpire(redisKey, expireTime, TimeUnit.SECONDS);
                }
            }

            return msgSendResult;
        } catch (Exception e) {
            log.error("发送短信错误", e);
            return new MsgSendResult(false, "短信发送失败：" + e.getMessage());
        }
    }


}
