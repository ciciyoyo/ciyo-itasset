package com.ciyocloud.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author codeck
 * mybatis 自动填充插件
 * @link https://baomidou.com/guide/typehandler.html
 */
@Slf4j
@Component
public class AutoFillMetaInfoHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        //如果指定了创建时间更新时间就不生成 有的时候更新时间默认不填充值
        if (null != this.getFieldValByName(BaseEntity.Fields.createTime, metaObject)) {
            return;
        }
        this.strictInsertFill(metaObject, BaseEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        try {
            if (null == this.getFieldValByName(SysBaseEntity.Fields.createBy, metaObject)) {
                this.strictInsertFill(metaObject, SysBaseEntity.Fields.createBy, Long.class, SecurityUtils.getUserId());
            }
        } catch (Exception ignored) {
        }
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(BaseEntity.Fields.updateTime, LocalDateTime.now(), metaObject);
        if (null == this.getFieldValByName(SysBaseEntity.Fields.updateBy, metaObject)) {
            try {
                this.strictInsertFill(metaObject, SysBaseEntity.Fields.updateBy, Long.class, SecurityUtils.getUserId());
            } catch (Exception ignored) {
            }
        }
    }
}
