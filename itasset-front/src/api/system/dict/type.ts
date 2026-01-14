import request from '@/utils/http'

/** 字典类型实体 */
export interface DictTypeEntity {
    /**
     * 字典ID
     */
    dictId?: number

    /**
     * 字典名称
     */
    dictName: string

    /**
     * 字典类型
     */
    dictType: string

    /**
     * 状态（0正常 1停用）
     */
    status: string

    /**
     * 备注
     */
    remark: string

    /**
     * 创建时间
     */
    createTime?: string

    /**
     * 更新时间
     */
    updateTime?: string
}

/** 字典类型分页查询参数 */
export interface DictTypePageParams {
    pageNum?: number
    pageSize?: number
    dictName?: string
    dictType?: string
    status?: string
}

/** 字典类型分页响应 */
export interface DictTypePageResponse {
    records: DictTypeEntity[]
    total: number
}

/**
 * 查询字典类型列表
 * @param query 查询参数
 * @returns 字典类型分页数据
 */
export function listType(query?: DictTypePageParams) {
    return request.get<DictTypePageResponse>({
        url: '/system/dict/type/page',
        params: query
    }) as Promise<DictTypePageResponse>
}

/**
 * 查询字典类型详细
 * @param dictId 字典ID
 * @returns 字典类型信息
 */
export function getType(dictId: string | number) {
    return request.get<DictTypeEntity>({
        url: '/system/dict/type/' + dictId
    }) as Promise<DictTypeEntity>
}

/**
 * 新增字典类型
 * @param data 字典类型数据
 * @returns 操作结果
 */
export function addType(data: DictTypeEntity) {
    return request.post({
        url: '/system/dict/type',
        data
    })
}

/**
 * 修改字典类型
 * @param data 字典类型数据
 * @returns 操作结果
 */
export function updateType(data: DictTypeEntity) {
    return request.put({
        url: '/system/dict/type',
        data
    })
}

/**
 * 删除字典类型
 * @param dictId 字典ID
 * @returns 操作结果
 */
export function delType(dictId: number) {
    return request.del({
        url: '/system/dict/type/' + dictId
    })
}

/**
 * 刷新字典缓存
 * @returns 操作结果
 */
export function refreshCache() {
    return request.del({
        url: '/system/dict/type/refreshCache'
    })
}

/**
 * 导出字典类型
 * @param query 查询参数
 * @returns Blob数据
 */
export function exportType(query: DictTypePageParams) {
    return request.get<Blob>({
        url: '/system/dict/type/export',
        responseType: 'blob',
        params: query
    })
}

/**
 * 获取字典选择框列表
 * @returns 字典类型列表
 */
export function optionselect() {
    return request.get<DictTypeEntity[]>({
        url: '/system/dict/type/optionselect'
    }) as Promise<DictTypeEntity[]>
}
