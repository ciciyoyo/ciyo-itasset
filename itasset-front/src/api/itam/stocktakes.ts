import request from '@/utils/http'

export interface StocktakesEntity extends Api.Common.BaseEntity {
    id: number
    name: string
    locationId: number
    categoryId: number
    status: string
    statusDesc: string
    managerId: number
    startDate: string
    endDate: string
    note: string
    locationName: string
    categoryName: string
    managerName: string
    totalCount: number
    surplusCount: number
    deficitCount: number
    uncheckedCount: number
    normalCount: number
    damagedCount: number
    scrappedCount: number
    createBy: string
    updateBy: string
}

type StocktakesList = Api.Common.PaginatedResponse<StocktakesEntity>

type StocktakesSearchFields = {
    name: string
    locationId: number
    categoryId: number
    startDate: string
    endDate: string
    note: string
    createBy: string
    updateBy: string
}

type StocktakesSearchParams = StocktakesSearchFields & Api.Common.CommonSearchParams

/**
 * 获取盘点任务列表
 */
export function pageStocktakes(params: StocktakesSearchParams) {
    return request.get<StocktakesList>({
        url: '/itam/stocktakes/page',
        params
    })
}

/**
 * 获取盘点任务详情
 * @param id
 */
export function getStocktakes(id: number): any {
    return request.get<StocktakesEntity>({
        url: `/itam/stocktakes/${id}`
    })
}

/**
 * 创建盘点任务
 * @param data
 */
export function addStocktakes(data: StocktakesEntity) {
    return request.post({
        url: '/itam/stocktakes/add',
        data
    })
}

/**
 * 修改盘点任务
 * @param data
 */
export function updateStocktakes(data: StocktakesEntity) {
    return request.post({
        url: '/itam/stocktakes/update',
        data
    })
}

/**
 * 删除盘点任务
 * @param id
 */
export function delStocktakes(id: number | number[]) {
    return request.post({
        url: `/itam/stocktakes/delete/${id}`
    })
}

/**
 * 导出盘点任务
 */
export function exportStocktakes(query: StocktakesSearchParams) {
    return request.get<Blob>({
        url: '/itam/stocktakes/export',
        responseType: 'blob',
        params: query
    })
}
