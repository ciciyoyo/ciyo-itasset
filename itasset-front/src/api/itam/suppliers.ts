import request from '@/utils/http'

export interface SuppliersEntity extends Api.Common.BaseEntity {
    id?: number
    name: string
    contactName?: string
    phone?: string
    remark?: string
}

type SuppliersList = Api.Common.PaginatedResponse<SuppliersEntity>

type SuppliersSearchFields = {
    id?: number
    name?: string
    contactName?: string
    phone?: string
    remark?: string
}

type SuppliersSearchParams = SuppliersSearchFields & Api.Common.CommonSearchParams

/**
 * 获取供应商列表
 */
export function pageSuppliers(params: SuppliersSearchParams) {
    return request.get<SuppliersList>({
        url: '/itam/suppliers/page',
        params
    })
}

/**
 * 获取供应商列表（不分页）
 */
export function listSuppliers(params?: SuppliersSearchFields) {
    return request.get<SuppliersEntity[]>({
        url: '/itam/suppliers/list',
        params
    })
}

/**
 * 获取供应商详情
 * @param id
 */
export function getSuppliers(id: number): any {
    return request.get<SuppliersEntity>({
        url: `/itam/suppliers/${id}`
    })
}

/**
 * 创建供应商
 * @param data
 */
export function addSuppliers(data: SuppliersEntity) {
    return request.post({
        url: '/itam/suppliers/add',
        data
    })
}

/**
 * 修改供应商
 * @param data
 */
export function updateSuppliers(data: SuppliersEntity) {
    return request.post({
        url: '/itam/suppliers/update',
        data
    })
}

/**
 * 删除供应商
 * @param id
 */
export function delSuppliers(id: number | number[]) {
    return request.post({
        url: `/itam/suppliers/delete/${id}`
    })
}

/**
 * 导出供应商
 */
export function exportSuppliers(query: SuppliersSearchParams) {
    return request.get<Blob>({
        url: '/itam/suppliers/export',
        responseType: 'blob',
        params: query
    })
}
