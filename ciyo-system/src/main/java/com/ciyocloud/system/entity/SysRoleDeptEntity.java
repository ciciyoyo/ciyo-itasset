package com.ciyocloud.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import lombok.Data;

/**
 * 角色和部门关联 sys_role_dept
 *
 * @author codeck
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDeptEntity extends SysBaseEntity {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;


}
