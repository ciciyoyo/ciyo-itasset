package com.ciyocloud.system.request;

import lombok.Data;

/**
 * 用户登录对象
 *
 * @author codeck
 */
@Data
public class LoginRequest {
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;


}
