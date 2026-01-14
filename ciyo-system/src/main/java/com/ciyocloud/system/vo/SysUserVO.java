package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysUserEntity;
import lombok.Data;

@Data
public class SysUserVO extends SysUserEntity {

    /**
     * 企微账号Id
     */
    private String cpWxName;


    /**
     * 微信公众号账号
     */
    private String wxMpName;
}
