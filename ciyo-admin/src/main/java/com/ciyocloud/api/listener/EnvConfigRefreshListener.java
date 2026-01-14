//package com.ciyocloud.api.listener;
//
//import com.github.binarywang.wxpay.service.WxPayService;
//import com.ciyocloud.ai.config.AiConfiguration;
//import com.ciyocloud.common.constant.ConfigConstants;
//import com.ciyocloud.common.util.JsonUtils;
//import com.ciyocloud.common.util.SpringContextUtils;
//import com.ciyocloud.envconfig.entity.event.EnvConfigRefreshEvent;
//import com.ciyocloud.form.thirdparty.faceidentify.FaceConfiguration;
//import com.ciyocloud.form.thirdparty.ocr.OcrConfiguration;
//import com.ciyocloud.form.thirdparty.translate.TranslateConfiguration;
//import com.ciyocloud.integration.config.DingTalkConfiguration;
//import com.ciyocloud.integration.config.DingTalkProperties;
//import com.ciyocloud.integration.config.LarkConfiguration;
//import com.ciyocloud.integration.config.LarkProperties;
//import com.ciyocloud.message.thirdparty.email.MailConfiguration;
//import com.ciyocloud.message.thirdparty.sms.SmsConfiguration;
//import com.ciyocloud.storage.cloud.OssStorageFactory;
//import com.ciyocloud.wx.cp.config.WxCpConfiguration;
//import com.ciyocloud.wx.cp.config.WxCpProperties;
//import com.ciyocloud.wx.mp.config.WxMpConfiguration;
//import com.ciyocloud.wx.mp.config.WxMpProperties;
//import com.ciyocloud.wx.pay.config.WxPayConfiguration;
//import com.ciyocloud.wx.pay.config.WxPayProperties;
//import lombok.extern.slf4j.Slf4j;
//import me.chanjar.weixin.cp.api.WxCpService;
//import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
/// **
// * @author : codeck
// * @description :  系统配置更新
// * @create :  2021/11/30 10:40
// **/
//@Component
//@Slf4j
//public class EnvConfigRefreshListener {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    /**
//     * 更新系统环境配置
//     *
//     * @param event 环境配置更新事件
//     */
//    @EventListener
//    public void envConfigRefreshEvent(EnvConfigRefreshEvent event) {
//        log.debug("配置更新:{}", JsonUtils.objToJson(event.getConfig()));
//        switch (event.getConfig().getEnvKey().trim()) {
//            case ConfigConstants.FILE_ENV_CONFIG:
//                OssStorageFactory.build();
//                break;
//            case ConfigConstants.SMS_ENV_CONFIG:
//                SmsConfiguration.buildMsgSender();
//                break;
//            case ConfigConstants.EMAIL_ENV_CONFIG:
//                MailConfiguration.buildMailSender();
//                break;
//            case ConfigConstants.OCR_ENV_CONFIG:
//                OcrConfiguration.buildOcr();
//                break;
//            case ConfigConstants.FACE_ENV_CONFIG:
//                FaceConfiguration.buildFace();
//                break;
//            case ConfigConstants.DEEP_SEEK_ENV_CONFIG:
//                AiConfiguration.buildDeepSeek();
//                break;
//            case ConfigConstants.TRANSLATE_ENV_CONFIG:
//                TranslateConfiguration.buildTranslate();
//                break;
//            case ConfigConstants.WX_MP_ENV_CONFIG:
//                WxMpProperties.MpConfig mpConfig = JsonUtils.jsonToObj(JsonUtils.objToJson(event.getConfig().getEnvValue()), WxMpProperties.MpConfig.class);
//                WxMpServiceImpl wxService = SpringContextUtils.getBean(WxMpServiceImpl.class);
//                WxMpConfiguration.setWxMpConfig(wxService, mpConfig, redisTemplate);
//                break;
//            case ConfigConstants.WX_PAY_ENV_CONFIG:
//                WxPayProperties properties = JsonUtils.jsonToObj(JsonUtils.objToJson(event.getConfig().getEnvValue()), WxPayProperties.class);
//                WxPayService wxPayService = SpringContextUtils.getBean(WxPayService.class);
//                Optional.ofNullable(properties).ifPresent(p -> {
//                    WxPayConfiguration.setPayConfig(wxPayService, p);
//                });
//                break;
//            case ConfigConstants.WX_CP_ENV_CONFIG:
//                WxCpProperties cpProperties = JsonUtils.jsonToObj(JsonUtils.objToJson(event.getConfig().getEnvValue()), WxCpProperties.class);
//                WxCpService wxCpService = SpringContextUtils.getBean(WxCpService.class);
//                Optional.ofNullable(cpProperties).ifPresent(p -> {
//                    WxCpConfiguration.setWxCpConfig(wxCpService, p, redisTemplate);
//                });
//                break;
//            case ConfigConstants.DING_TALK_ENV_CONFIG:
//                DingTalkProperties configs = JsonUtils.jsonToObj(JsonUtils.objToJson(event.getConfig().getEnvValue()),
//                        DingTalkProperties.class);
//                Optional.ofNullable(configs).ifPresent(DingTalkConfiguration::setDingTalkConfig);
//                break;
//            case ConfigConstants.LARK_ENV_CONFIG:
//                LarkProperties larkProperties = JsonUtils.jsonToObj(JsonUtils.objToJson(event.getConfig().getEnvValue()),
//                        LarkProperties.class);
//                Optional.ofNullable(larkProperties).ifPresent(LarkConfiguration::updateLarkConfig);
//                break;
//            default:
//                break;
//        }
//
//    }
//
//
//}
