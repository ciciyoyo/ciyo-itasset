package com.ciyocloud.system.entity;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.OrderBy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.constant.UserConstants;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * 菜单权限表 sys_menu
 *
 * @author codeck
 */
@Data
@TableName("sys_menu")
public class SysMenuEntity extends SysBaseEntity {


    /**
     * 菜单名称
     */
    private String menuName;


    private String langKey;
    /**
     * 父菜单名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 父菜单ID
     */
    @OrderBy(asc = true)
    private Long parentId;

    /**
     * 显示顺序
     */
    @OrderBy(asc = true)
    private Integer orderNum;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 是否为外链（0是 1否）
     */
    private Integer isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    private Integer isCache;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    private String visible;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    private String status;


    /**
     * 权限字符串
     */
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;


    /**
     * 子菜单
     */
    @TableField(exist = false)
    private List<SysMenuEntity> children = new ArrayList<SysMenuEntity>();

    public String getPerms() {
        return ObjectUtil.isNotNull(this.perms) ? perms : StrUtil.EMPTY;
    }

    public void addInit() {
        if (null == this.getParentId()) {
            this.setParentId(0L);
        }
        if (null == this.getOrderNum()) {
            this.setOrderNum(0);
        }
        if (null == this.getIsFrame()) {
            this.setIsFrame(UserConstants.NO_FRAME);
        }
        if (null == this.getIsCache()) {
            this.setIsCache(0);
        }
        if (StrUtil.isBlank(this.getVisible())) {
            this.setVisible(SystemConstants.NORMAL_STATUS);
        }
        if (StrUtil.isBlank(this.getStatus())) {
            this.setStatus(SystemConstants.NORMAL_STATUS);
        }
    }
}
