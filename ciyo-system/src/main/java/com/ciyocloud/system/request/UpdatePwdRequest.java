package com.ciyocloud.system.request;

import lombok.Data;


/**
 * 修改密码
 */
@Data
public class UpdatePwdRequest {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
