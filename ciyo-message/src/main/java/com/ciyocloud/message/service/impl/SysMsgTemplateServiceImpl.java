package com.ciyocloud.message.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.message.constant.MessageConstants;
import com.ciyocloud.message.entity.SysMsgTemplateEntity;
import com.ciyocloud.message.mapper.SysMsgTemplateMapper;
import com.ciyocloud.message.service.SysMsgTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : codeck
 * @description : 消息模板
 * @create :  2021/06/07 16:37
 **/
@Service
@RequiredArgsConstructor
public class SysMsgTemplateServiceImpl extends ServiceImpl<SysMsgTemplateMapper, SysMsgTemplateEntity> implements SysMsgTemplateService {

    private final RedisUtils redisUtils;


    @Override
    public SysMsgTemplateEntity getByCode(String code) {
        String json = redisUtils.get(StrUtil.format(MessageConstants.MESSAGE_TEMPLATE_KEY, code), String.class);
        if (StrUtil.isNotBlank(json)) {
            return JsonUtils.jsonToObj(json, SysMsgTemplateEntity.class);
        }
        return baseMapper.selectByCode(code);
    }

    @Override
    public boolean saveMsgTemplate(SysMsgTemplateEntity entity) {
        this.save(entity);
        redisUtils.set(StrUtil.format(MessageConstants.MESSAGE_TEMPLATE_KEY, entity.getTemplateCode()), JsonUtils.objToJson(entity));
        return true;
    }

    @Override
    public boolean updateMsgTemplate(SysMsgTemplateEntity entity) {
        this.updateById(entity);
        redisUtils.set(StrUtil.format(MessageConstants.MESSAGE_TEMPLATE_KEY, entity.getTemplateCode()), JsonUtils.objToJson(entity));
        return true;
    }
}
