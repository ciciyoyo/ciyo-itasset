package com.ciyocloud.system.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录VO
 *
 * @author : codeck
 * @since :  2025/06/11 16:27
 **/
@Builder
@Data
public class SysUserLoginVO {

    /**
     * token
     */
    private String token;

    /**
     * 是否需要修改密码
     */
    private boolean needChangePassword;
}
