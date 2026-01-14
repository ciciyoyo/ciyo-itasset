package com.ciyocloud.system.vo;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysMenuEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 路由显示信息
 *
 * @author codeck
 */
@Data
@NoArgsConstructor
public class MetaVO {
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private Boolean isKeepAlive;


    /**
     * 固定
     */
    private Boolean isAffix = false;
    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private Boolean isHide;

    /**
     * 跳转地址
     */
    private String isLink;

    /**
     * 是否内嵌ifama打开
     */
    private Boolean isIframe;


    /**
     * 路由位置
     */
    private String location;

    public MetaVO(SysMenuEntity menu) {
        this.title = StrUtil.isNotBlank(menu.getLangKey()) ? menu.getLangKey() : menu.getMenuName();
        this.icon = menu.getIcon();
        this.isKeepAlive = null != menu.getIsCache() && 0 == menu.getIsCache();
        this.isHide = "1".equals(menu.getVisible());
        if (Validator.isUrl(menu.getPath())) {
            this.isLink = menu.getPath();
        }
        this.isIframe = Objects.equals(menu.getIsFrame(), UserConstants.YES_FRAME);
    }


}
