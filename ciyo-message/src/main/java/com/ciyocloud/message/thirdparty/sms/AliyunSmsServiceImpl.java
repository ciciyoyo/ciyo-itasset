package com.ciyocloud.message.thirdparty.sms;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.message.entity.MsgSendResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author : codeck
 * @description : 腾讯云短信
 * @create : 2020-12-15 10:33
 **/
@Slf4j
public class AliyunSmsServiceImpl extends SmsService {


    private Client client;

    public AliyunSmsServiceImpl(SmsPlatformProperties properties) {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(properties.getSecretId())
                // 您的AccessKey Secret
                .setAccessKeySecret(properties.getSecretKey());
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        try {
            client = new Client(config);
            this.properties = properties;
        } catch (Exception e) {
            log.error("初始化阿里云短信服务失败", e);
        }
    }


    @Override
    public MsgSendResult sendSms(String phoneNumber, String templateId, Map<String, Object> templateParams) throws Exception {
        // 1.发送短信
        SendSmsRequest sendReq = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(properties.getSign())
                .setTemplateCode(templateId)
                .setTemplateParam(JsonUtils.objToJson(templateParams));
        SendSmsResponse sendResp = client.sendSms(sendReq);
        log.debug("阿里云短信发送结果：{}", JsonUtils.objToJson(sendResp));
        String code = sendResp.body.code;
        Assert.isTrue(StrUtil.equals(code, "OK"), sendResp.body.message);
        return new MsgSendResult(StrUtil.equals(code, "OK"), sendResp.body.toString());
    }


}
