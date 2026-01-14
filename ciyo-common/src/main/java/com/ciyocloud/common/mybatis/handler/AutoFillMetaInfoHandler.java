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
        // 填充创建时间
        if (metaObject.hasSetter(BaseEntity.Fields.createTime)) {
            this.strictInsertFill(metaObject, BaseEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        }
        // 填充创建人
        if (metaObject.hasSetter(SysBaseEntity.Fields.createBy)) {
            if (null == this.getFieldValByName(SysBaseEntity.Fields.createBy, metaObject)) {
                this.strictInsertFill(metaObject, SysBaseEntity.Fields.createBy, Long.class, SecurityUtils.getUserId());
            }
        }
        // 填充逻辑删除标识
        if (metaObject.hasSetter("deleted")) {
            this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        if (metaObject.hasSetter(BaseEntity.Fields.updateTime)) {
            this.setFieldValByName(BaseEntity.Fields.updateTime, LocalDateTime.now(), metaObject);
        }
        // 更新人
        if (metaObject.hasSetter(SysBaseEntity.Fields.updateBy)) {
            if (null == this.getFieldValByName(SysBaseEntity.Fields.updateBy, metaObject)) {
                this.strictUpdateFill(metaObject, SysBaseEntity.Fields.updateBy, Long.class, SecurityUtils.getUserId());
            }
        }
    }
}
