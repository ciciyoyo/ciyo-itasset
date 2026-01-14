export enum AssetType {
    /**
     * 设备
     */
    DEVICE = 'device',

    /**
     * 配件
     */
    ACCESSORY = 'accessory',

    /**
     * 耗材
     */
    CONSUMABLE = 'consumable',

    /**
     * 软件授权
     */
    LICENSE = 'license',

    /**
     * 服务
     */
    SERVICE = 'service',

    /**
     * 其他
     */
    OTHER = 'other'
}

export const AssetTypeMap: Record<AssetType, string> = {
    [AssetType.DEVICE]: '设备',
    [AssetType.ACCESSORY]: '配件',
    [AssetType.CONSUMABLE]: '耗材',
    [AssetType.LICENSE]: '软件授权',
    [AssetType.SERVICE]: '服务',
    [AssetType.OTHER]: '其他'
}

export const AssetTypeOptions = Object.values(AssetType).map((value) => ({
    value,
    label: AssetTypeMap[value]
}))

export enum FailureStatus {
    /**
     * 待处理
     */
    PENDING = 'pending',

    /**
     * 处理中
     */
    PROCESSING = 'processing',

    /**
     * 已修复
     */
    RESOLVED = 'resolved',

    /**
     * 已报废
     */
    SCRAPPED = 'scrapped'
}

export const FailureStatusMap: Record<FailureStatus, string> = {
    [FailureStatus.PENDING]: '待处理',
    [FailureStatus.PROCESSING]: '处理中',
    [FailureStatus.RESOLVED]: '已修复',
    [FailureStatus.SCRAPPED]: '已报废'
}

export const FailureStatusOptions = Object.values(FailureStatus).map((value) => ({
    value,
    label: FailureStatusMap[value]
}))
