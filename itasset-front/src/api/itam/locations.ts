import request from '@/utils/http'

export interface LocationsEntity extends Api.Common.BaseEntity {
    id?: number
    name: string
    parentId?: number | null
    managerId?: number
    children?: LocationsEntity[]
}

type LocationsList = Api.Common.PaginatedResponse<LocationsEntity>

type LocationsSearchFields = {
    id?: number
    name?: string
    parentId?: number
    managerId?: number
}

type LocationsSearchParams = LocationsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取物理位置树形列表
 */
export function listLocations(params?: LocationsSearchFields) {
    return request.get<LocationsEntity[]>({
        url: '/itam/locations/tree',
        params
    })
}

/**
 * 获取物理位置详情
 * @param id
 */
export function getLocations(id: number): any {
    return request.get<LocationsEntity>({
        url: `/itam/locations/${id}`
    })
}

/**
 * 创建物理位置
 * @param data
 */
export function addLocations(data: LocationsEntity) {
    return request.post({
        url: '/itam/locations/add',
        data
    })
}

/**
 * 修改物理位置
 * @param data
 */
export function updateLocations(data: LocationsEntity) {
    return request.post({
        url: '/itam/locations/update',
        data
    })
}

/**
 * 删除物理位置
 * @param id
 */
export function delLocations(id: number | number[]) {
    return request.post({
        url: `/itam/locations/delete/${id}`
    })
}

/**
 * 导出物理位置
 */
export function exportLocations(query: LocationsSearchParams) {
    return request.get<Blob>({
        url: '/itam/locations/export',
        responseType: 'blob',
        params: query
    })
}
