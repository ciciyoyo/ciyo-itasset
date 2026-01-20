import request from '@/utils/http'

/**
 * 资产类型枚举
 */
export enum AssetsType {
    /** 设备 */
    DEVICE = 'DEVICE',
    /** 配件 */
    ACCESSORY = 'ACCESSORY',
    /** 耗材 */
    CONSUMABLE = 'CONSUMABLE',
    /** 软件授权 */
    LICENSE = 'LICENSE',
    /** 服务 */
    SERVICE = 'SERVICE',
    /** 其他 */
    OTHER = 'OTHER'
}

/**
 * 摘要统计数据
 */
export interface SummaryData {
    totalAssetAmount: number
    totalAssetCount: number
    totalDevices: number
    totalSoftware: number
    totalAccessories: number
    totalServices: number
    totalConsumables: number
}

/**
 * 年度资产价值数据项
 */
export interface YearlyAssetsValueItem {
    statsMonth: string
    assetsType: string
    assetsTypeDesc: string
    totalValue: number
}

/**
 * 年度资产价值数据
 */
export type YearlyAssetsValueData = YearlyAssetsValueItem[]

/**
 * 月度趋势数据项（与年度数据结构相同）
 */
export interface MonthlyTrendItem {
    statsMonth: string
    assetsType: string
    assetsTypeDesc: string
    totalValue: number
}

/**
 * 月度趋势数据
 */
export type MonthlyTrendData = MonthlyTrendItem[]

/**
 * 分布数据项（与年度/月度数据结构相同）
 */
export interface DistributionItem {
    statsMonth: string
    assetsType: string
    assetsTypeDesc: string
    totalValue: number
}

/**
 * 分布数据
 */
export type DistributionData = DistributionItem[]

/**
 * 获取年度资产价值统计
 */
export function getYearlyAssetsValue() {
    return request.get<YearlyAssetsValueData>({
        url: '/itam/reports/yearly-assets-value'
    })
}

/**
 * 获取月度趋势统计
 * @param assetsType 资产类型（可选）
 */
export function getMonthlyTrend(assetsType?: AssetsType | string) {
    return request.get<MonthlyTrendData>({
        url: '/itam/reports/monthly-trend',
        params: assetsType ? { assetsType } : undefined
    })
}

/**
 * 获取资产分布统计
 */
export function getDistribution() {
    return request.get<DistributionData>({
        url: '/itam/reports/distribution'
    })
}

/**
 * 获取摘要统计
 */
export function getSummary() {
    return request.get<SummaryData>({
        url: '/itam/reports/summary'
    })
}
