//package com.ciyocloud.message.thirdparty.wxmp;
//
//
//import cn.hutool.core.util.IdUtil;
//import cn.hutool.core.util.ObjectUtil;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.ciyocloud.message.entity.SysMsgTemplateEntity;
//import com.ciyocloud.message.entity.enums.MsgTypeEnum;
//import com.ciyocloud.message.service.SysMsgTemplateService;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import me.chanjar.weixin.mp.api.WxMpService;
//import me.chanjar.weixin.mp.bean.template.WxMpTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
/// **
// * 微信模板消息
// */
//@Service
//@RequiredArgsConstructor
//public class WxMpTemplateMsgService {
//
//    private final WxMpService wxService;
//    private final SysMsgTemplateService sysMsgTemplateService;
//
//    /**
//     * 获取全部模版
//     *
//     * @return 模版列表
//     */
//    @SneakyThrows
//    public List<WxMpTemplate> getAllTemplateList() {
//        return wxService.getTemplateMsgService().getAllPrivateTemplate();
//    }
//
//
//    /**
//     * 同步微信的消息模版到系统
//     */
//    public void syncWxTemplateToSys() {
//        List<WxMpTemplate> allTemplateList = getAllTemplateList();
//        for (WxMpTemplate wxMpTemplate : allTemplateList) {
//            String templateId = wxMpTemplate.getTemplateId();
//            SysMsgTemplateEntity templateEntity = sysMsgTemplateService.getOne(Wrappers.<SysMsgTemplateEntity>lambdaQuery()
//                    .eq(SysMsgTemplateEntity::getThirdTemplateId, templateId));
//            if (ObjectUtil.isNull(templateEntity)) {
//                templateEntity = new SysMsgTemplateEntity();
//                templateEntity.setTemplateCode(IdUtil.nanoId());
//            }
//            templateEntity.setThirdTemplateId(templateId);
//            // 把content 中的 {{first.DATA}}  替换成 ${first.DATA} 以便于后面使用
//            // 使用正则表达式匹配{{...}}中的内容
//            Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
//            Matcher matcher = pattern.matcher(wxMpTemplate.getContent());
//            // 逐个替换匹配到的内容
//            StringBuffer replacedString = new StringBuffer();
//            while (matcher.find()) {
//                String replacement = "\\${" + matcher.group(1) + "}";
//                matcher.appendReplacement(replacedString, replacement);
//            }
//            matcher.appendTail(replacedString);
//            // 移除默认全部的.data
//            templateEntity.setTemplateContent(replacedString.toString().replaceAll("\\.DATA", ""));
//            templateEntity.setTemplateName(wxMpTemplate.getTitle());
//            templateEntity.setTemplateType(MsgTypeEnum.WX_MP);
//            sysMsgTemplateService.saveOrUpdate(templateEntity);
//        }
//    }
//
//}
