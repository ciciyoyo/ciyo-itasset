package com.ciyocloud.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.message.entity.SysMsgTemplateEntity;


/**
 * @author : codeck
 * @description : 消息模板
 * @create :  2021/06/07 16:37
 **/
public interface SysMsgTemplateService extends IService<SysMsgTemplateEntity> {

    /**
     * 获取模板
     *
     * @param code
     * @return
     */
    SysMsgTemplateEntity getByCode(String code);


    /**
     * 保存
     *
     * @param entity
     * @return
     */
    boolean saveMsgTemplate(SysMsgTemplateEntity entity);

    /**
     * 修改
     *
     * @param entity
     * @return
     */
    boolean updateMsgTemplate(SysMsgTemplateEntity entity);


}
