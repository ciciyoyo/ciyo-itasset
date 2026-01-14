package com.ciyocloud.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.common.entity.SysBaseEntity;
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author codeck
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenuEntity extends BaseEntity {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;


}
