import request from '@/utils/http'

export interface StocktakeItemsEntity extends Api.Common.BaseEntity {
    id: number
    stocktakeId: string
    assetId: string
    status: string
    scannedBy: string
    scannedAt: string
    expectedLocationId: string
    actualLocationId: string
    note: string
    createBy: string
    updateBy: string
}

type StocktakeItemsList = Api.Common.PaginatedResponse<StocktakeItemsEntity>

type StocktakeItemsSearchFields = {
    id: number
    stocktakeId: string
    assetId: string
    status: string
    scannedBy: string
    scannedAt: string
    expectedLocationId: string
    actualLocationId: string
    note: string
    createBy: string
    updateBy: string
}

type StocktakeItemsSearchParams = StocktakeItemsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取盘点明细列表
 */
export function pageStocktakeItems(params: StocktakeItemsSearchParams) {
    return request.get<StocktakeItemsList>({
        url: '/itam/stocktakeItems/page',
        params
    })
}

/**
 * 获取盘点明细详情
 * @param id
 */
export function getStocktakeItems(id: number): any {
    return request.get<StocktakeItemsEntity>({
        url: `/itam/stocktakeItems/${id}`
    })
}

/**
 * 创建盘点明细
 * @param data
 */
export function addStocktakeItems(data: StocktakeItemsEntity) {
    return request.post({
        url: '/itam/stocktakeItems/add',
        data
    })
}

/**
 * 修改盘点明细
 * @param data
 */
export function updateStocktakeItems(data: StocktakeItemsEntity) {
    return request.post({
        url: '/itam/stocktakeItems/update',
        data
    })
}

/**
 * 删除盘点明细
 * @param id
 */
export function delStocktakeItems(id: number | number[]) {
    return request.post({
        url: `/itam/stocktakeItems/delete/${id}`
    })
}

/**
 * 导出盘点明细
 */
export function exportStocktakeItems(query: StocktakeItemsSearchParams) {
    return request.get<Blob>({
        url: '/itam/stocktakeItems/export',
        responseType: 'blob',
        params: query
    })
}
