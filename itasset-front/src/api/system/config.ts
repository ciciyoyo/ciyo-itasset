import request from '@/utils/http'

// 配置实体类型定义
export interface ConfigEntity {
    id?: number
    createTime?: string
    updateTime?: string
    searchValue?: string | null
    createBy?: string
    updateBy?: string
    params?: Record<string, any>
    configName?: string
    configKey?: string
    configValue?: string
    configType?: string
    remark?: string
}

// 分页响应类型定义
export interface PageResponse<T> {
    records: T[]
    total: number
    size: number
    current: number
    orders?: any[]
    optimizeCountSql?: boolean
    searchCount?: boolean
    maxLimit?: number | null
    countId?: string | null
    pages: number
}

// 查询参数列表
export function listConfig(query: any) {
    return request.get<PageResponse<ConfigEntity>>({
        url: '/system/config/page',
        params: query
    })
}

// 查询参数详细
export function getConfig(configId: number) {
    return request.get<ConfigEntity>({
        url: '/system/config/' + configId
    })
}

// 根据参数键名查询参数值
export function getConfigValue(configKey: string) {
    return request.get<string>({
        url: `/system/config/${configKey}/configValue`
    })
}

// 根据参数键名查询参数值
export function getConfigByKey(configKey: string) {
    return request.get({
        url: `/system/config/${configKey}/obj`
    })
}

// 新增参数配置
export function addConfig(data: any) {
    return request.post({
        url: '/system/config',
        data: data
    })
}

// 修改参数配置
export function updateConfig(data: any) {
    return request.put({
        url: '/system/config',
        data: data
    })
}

// 删除参数配置
export function delConfig(configId: number) {
    return request.del({
        url: '/system/config/' + configId
    })
}

// 刷新参数缓存
export function refreshCache() {
    return request.del({
        url: '/system/config/refreshCache'
    })
}

// 导出参数
export function exportConfig(query: any) {
    return request.get({
        url: '/system/config/export',
        params: query,
        responseType: 'blob'
    })
}

// 获取系统配置
export function getEnvConfig(key: string) {
    return request.get({
        url: '/system/env/config/' + key
    })
}

// 获取系统配置
export function getWxMiniAppId() {
    return request.get({
        url: '/system/env/config/getWxMiniAppId'
    })
}

export function getEnvConfigValue(key: string) {
    return request.get({
        url: '/system/env/config/value/' + key
    })
}

export function getSystemInfoConfig() {
    return request.get({
        url: '/public/systemInfoConfig'
    })
}

// 保存系统配置
export function saveEnvConfig(data: any) {
    return request.post({
        url: '/system/env/config/save',
        data: data
    })
}
