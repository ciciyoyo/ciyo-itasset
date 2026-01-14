package com.ciyocloud.common.entity.security;

import cn.hutool.core.collection.CollUtil;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户身份权限
 *
 * @author codeck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SysUserVO user;


    public LoginUserEntity(SysUserVO user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    @JsonIgnore
    public Long getUserId() {
        if (null == user) {
            return null;
        }
        return user.getId();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @JsonIgnore
    public String getUsername() {
        return user.getUserName();
    }
}
