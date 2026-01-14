package com.ciyocloud.message.thirdparty.sms;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.ciyocloud.common.constant.ConfigConstants.SMS_ENV_CONFIG;

/**
 * @author : codeck
 * @description : 短信平台配置
 * @create :  2021/07/19 10:59
 **/
@RequiredArgsConstructor
public class SmsConfiguration {

    private static final String CONFIG_KEY = StrUtil.format(RedisKeyConstants.ENV_CONFIG, SMS_ENV_CONFIG);
    @Getter
    private static SmsService smsService;

    static {
        buildMsgSender();
    }

    public static synchronized void buildMsgSender() {
        SmsPlatformProperties properties = JsonUtils.jsonToObj(SpringContextUtils.getBean(RedisUtils.class).get(CONFIG_KEY, String.class), SmsPlatformProperties.class);
        if (ObjectUtil.isNull(properties)) {
            return;
        }
        switch (properties.getType()) {
            case ALIYUN:
                smsService = new AliyunSmsServiceImpl(properties);
                break;
            case TENCENT_CLOUD:
                smsService = new TencentSmsServiceImpl(properties);
                break;
        }
    }

}
