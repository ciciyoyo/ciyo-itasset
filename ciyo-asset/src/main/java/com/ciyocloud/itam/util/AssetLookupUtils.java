package com.ciyocloud.itam.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.itam.entity.*;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.service.*;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 资产查找工具类
 * 用于根据名称查找对应的资产ID
 *
 * @author codeck
 * @since 2026-01-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetLookupUtils {

    private final DeviceService deviceService;
    private final AccessoriesService accessoriesService;
    private final ConsumablesService consumablesService;
    private final LicensesService licensesService;
    private final SysUserService sysUserService;
    private final LocationsService locationsService;
    private final SuppliersService suppliersService;
    private final ManufacturersService manufacturersService;
    private final CategoriesService categoriesService;
    private final DepreciationsService depreciationsService;

    /**
     * 根据资产类型和名称查找对应的ID
     *
     * @param assetType 资产类型
     * @param name      资产名称
     * @param rowNum    当前行号（用于错误提示，可选）
     * @return 资产ID
     */
    public Long findAssetIdByName(AssetType assetType, String name, Integer rowNum) {
        if (StrUtil.isBlank(name)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            switch (assetType) {
                case DEVICE:
                    DeviceEntity device = deviceService.getOneSafe(
                            new LambdaQueryWrapper<DeviceEntity>().eq(DeviceEntity::getName, name)
                    );
                    if (device == null) {
                        throw new BusinessException(errorPrefix + String.format("设备 '%s' 不存在", name));
                    }
                    return device.getId();

                case ACCESSORY:
                    AccessoriesEntity accessory = accessoriesService.getOneSafe(
                            new LambdaQueryWrapper<AccessoriesEntity>().eq(AccessoriesEntity::getName, name)
                    );
                    if (accessory == null) {
                        throw new BusinessException(errorPrefix + String.format("配件 '%s' 不存在", name));
                    }
                    return accessory.getId();

                case CONSUMABLE:
                    ConsumablesEntity consumable = consumablesService.getOneSafe(
                            new LambdaQueryWrapper<ConsumablesEntity>().eq(ConsumablesEntity::getName, name)
                    );
                    if (consumable == null) {
                        throw new BusinessException(errorPrefix + String.format("耗材 '%s' 不存在", name));
                    }
                    return consumable.getId();

                case LICENSE:
                    LicensesEntity license = licensesService.getOneSafe(
                            new LambdaQueryWrapper<LicensesEntity>().eq(LicensesEntity::getName, name)
                    );
                    if (license == null) {
                        throw new BusinessException(errorPrefix + String.format("软件授权 '%s' 不存在", name));
                    }
                    return license.getId();

                default:
                    throw new BusinessException(errorPrefix + String.format("不支持的资产类型: %s", assetType));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找资产ID失败: assetType={}, name={}", assetType, name, e);
            throw new BusinessException(errorPrefix + String.format("查找 %s '%s' 失败", assetType.getDesc(), name));
        }
    }

    /**
     * 根据用户名查找用户ID
     *
     * @param userName 用户名
     * @param rowNum   当前行号（用于错误提示，可选）
     * @return 用户ID
     */
    public Long findUserIdByName(String userName, Integer rowNum) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            SysUserEntity user = sysUserService.getOne(
                    new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUserName, userName)
            );
            if (user == null) {
                throw new BusinessException(errorPrefix + String.format("用户 '%s' 不存在", userName));
            }
            return user.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找用户ID失败: userName={}", userName, e);
            throw new BusinessException(errorPrefix + String.format("查找用户 '%s' 失败", userName));
        }
    }

    /**
     * 根据位置名称查找位置ID
     *
     * @param locationName 位置名称
     * @param rowNum       当前行号（用于错误提示，可选）
     * @return 位置ID
     */
    public Long findLocationIdByName(String locationName, Integer rowNum) {
        if (StrUtil.isBlank(locationName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            LocationsEntity location = locationsService.getOneSafe(
                    new LambdaQueryWrapper<LocationsEntity>().eq(LocationsEntity::getName, locationName)
            );
            if (location == null) {
                throw new BusinessException(errorPrefix + String.format("存放位置 '%s' 不存在", locationName));
            }
            return location.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找位置ID失败: locationName={}", locationName, e);
            throw new BusinessException(errorPrefix + String.format("查找存放位置 '%s' 失败", locationName));
        }
    }

    /**
     * 根据供应商名称查找供应商ID
     *
     * @param supplierName 供应商名称
     * @param rowNum       当前行号（用于错误提示，可选）
     * @return 供应商ID
     */
    public Long findSupplierIdByName(String supplierName, Integer rowNum) {
        if (StrUtil.isBlank(supplierName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            SuppliersEntity supplier = suppliersService.getOneSafe(
                    new LambdaQueryWrapper<SuppliersEntity>().eq(SuppliersEntity::getName, supplierName)
            );
            if (supplier == null) {
                throw new BusinessException(errorPrefix + String.format("供应商 '%s' 不存在", supplierName));
            }
            return supplier.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找供应商ID失败: supplierName={}", supplierName, e);
            throw new BusinessException(errorPrefix + String.format("查找供应商 '%s' 失败", supplierName));
        }
    }

    /**
     * 根据厂商名称查找厂商ID
     *
     * @param manufacturerName 厂商名称
     * @param rowNum           当前行号（用于错误提示，可选）
     * @return 厂商ID
     */
    public Long findManufacturerIdByName(String manufacturerName, Integer rowNum) {
        if (StrUtil.isBlank(manufacturerName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            ManufacturersEntity manufacturer = manufacturersService.getOneSafe(
                    new LambdaQueryWrapper<ManufacturersEntity>().eq(ManufacturersEntity::getName, manufacturerName)
            );
            if (manufacturer == null) {
                throw new BusinessException(errorPrefix + String.format("厂商 '%s' 不存在", manufacturerName));
            }
            return manufacturer.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找厂商ID失败: manufacturerName={}", manufacturerName, e);
            throw new BusinessException(errorPrefix + String.format("查找厂商 '%s' 失败", manufacturerName));
        }
    }

    /**
     * 根据分类名称查找分类ID
     *
     * @param categoryName 分类名称
     * @param rowNum       当前行号（用于错误提示，可选）
     * @return 分类ID
     */
    public Long findCategoryIdByName(String categoryName, Integer rowNum) {
        if (StrUtil.isBlank(categoryName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            CategoriesEntity category = categoriesService.getOneSafe(
                    new LambdaQueryWrapper<CategoriesEntity>().eq(CategoriesEntity::getName, categoryName)
            );
            if (category == null) {
                throw new BusinessException(errorPrefix + String.format("分类 '%s' 不存在", categoryName));
            }
            return category.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找分类ID失败: categoryName={}", categoryName, e);
            throw new BusinessException(errorPrefix + String.format("查找分类 '%s' 失败", categoryName));
        }
    }

    /**
     * 根据折旧规则名称查找折旧ID
     *
     * @param depreciationName 折旧规则名称
     * @param rowNum           当前行号（用于错误提示，可选）
     * @return 折旧ID
     */
    public Long findDepreciationIdByName(String depreciationName, Integer rowNum) {
        if (StrUtil.isBlank(depreciationName)) {
            return null;
        }

        String errorPrefix = rowNum != null ? String.format("第%d行 ", rowNum) : "";

        try {
            DepreciationsEntity depreciation = depreciationsService.getOneSafe(
                    new LambdaQueryWrapper<DepreciationsEntity>().eq(DepreciationsEntity::getName, depreciationName)
            );
            if (depreciation == null) {
                throw new BusinessException(errorPrefix + String.format("折旧规则 '%s' 不存在", depreciationName));
            }
            return depreciation.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查找折旧规则ID失败: depreciationName={}", depreciationName, e);
            throw new BusinessException(errorPrefix + String.format("查找折旧规则 '%s' 失败", depreciationName));
        }
    }
}
