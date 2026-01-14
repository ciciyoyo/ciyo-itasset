package com.ciyocloud.system.request;

import com.ciyocloud.common.entity.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户分页查询请求
 *
 * @author codeck
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPageReq extends BaseRequest {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
