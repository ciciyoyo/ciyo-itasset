package com.ciyocloud.system.request;

import com.ciyocloud.common.entity.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询请求
 *
 * @author codeck
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePageReq extends BaseRequest {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符串
     */
    private String roleKey;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;
}
