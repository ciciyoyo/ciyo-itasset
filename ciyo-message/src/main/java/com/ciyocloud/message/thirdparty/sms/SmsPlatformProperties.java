package com.ciyocloud.message.thirdparty.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置类
 *
 * @author codeck
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "platform.sms")
public class SmsPlatformProperties {

    /**
     * secretId
     */
    private String secretId;


    private SmsTypeEnum type;
    /**
     * 秘钥
     */
    private String secretKey;
    /**
     * appId 腾讯云使用
     */
    private String appId;
    /**
     * 短信签名 如【xx 平台】 需要去短信平台申请
     */
    private String sign;


}
