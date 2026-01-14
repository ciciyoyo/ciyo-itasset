package com.ciyocloud.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author codeck
 */
@Data
@TableName("sys_user_role")
public class SysUserRoleEntity extends SysBaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;


}
