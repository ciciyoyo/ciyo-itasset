package com.ciyocloud.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ciyocloud.message.entity.SysMsgTemplateEntity;
import org.apache.ibatis.annotations.Select;

/**
 * @author : codeck
 * @description : 消息模板
 * @create :  2021/06/07 16:37
 **/
public interface SysMsgTemplateMapper extends BaseMapper<SysMsgTemplateEntity> {
    @Select("SELECT * FROM sys_msg_template WHERE template_code = #{code}")
    SysMsgTemplateEntity selectByCode(String code);
}
