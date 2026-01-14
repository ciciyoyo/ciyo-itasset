package com.ciyocloud.itam.util;

import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.system.service.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 资产权限工具类
 * 用于根据资产类型动态检查权限
 *
 * @author codeck
 */
@Component("assetPerms")
@RequiredArgsConstructor
public class AssetPermissionUtils {

    private final PermissionService permissionService;

    /**
     * 检查是否有某种资产类型的某个权限
     *
     * @param assetType 资产类型编码 (e.g. "device", "accessory")
     * @param action    动作 (e.g. "page", "add", "update", "delete", "export", "query")
     * @return boolean
     */
    public boolean hasAssetPermi(String assetType, String action) {
        if (assetType == null || assetType.isEmpty()) {
            return false;
        }

        String resource = getResourceName(assetType);
        if (resource == null) {
            return false;
        }

        String permission = "itam:" + resource + ":" + action;
        return permissionService.hasPermi(permission);
    }

    /**
     * 根据资产类型获取资源名称(复数形式)
     *
     * @param assetType 资产类型
     * @return 资源名称
     */
    private String getResourceName(String assetType) {
        if (AssetType.DEVICE.getCode().equals(assetType)) {
            return "devices";
        } else if (AssetType.ACCESSORY.getCode().equals(assetType)) {
            return "accessories";
        } else if (AssetType.CONSUMABLE.getCode().equals(assetType)) {
            return "consumables";
        } else if (AssetType.LICENSE.getCode().equals(assetType)) {
            return "licenses";
        } else if (AssetType.SERVICE.getCode().equals(assetType)) {
            return "services";
        }
        // 默认处理 (尝试加s?) 或者返回null
        // 如果传入的是其他类型，可以考虑返回 null 或者直接返回 assetType + "s"
        return null;
    }
}
