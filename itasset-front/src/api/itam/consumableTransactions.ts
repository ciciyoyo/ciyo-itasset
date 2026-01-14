import request from '@/utils/http'

export interface ConsumableTransactionsEntity extends Api.Common.BaseEntity {
    id: number
    consumableId: string
    operatorId: string
    actionType: string
    quantity: string
    remainingQuantity: string
    targetType: string
    targetId: string
    note: string
    createBy: string
    updateBy: string
}

type ConsumableTransactionsList = Api.Common.PaginatedResponse<ConsumableTransactionsEntity>

type ConsumableTransactionsSearchFields = {
    id: number
    consumableId: string
    operatorId: string
    actionType: string
    quantity: string
    remainingQuantity: string
    targetType: string
    targetId: string
    note: string
    createBy: string
    updateBy: string
}

type ConsumableTransactionsSearchParams = ConsumableTransactionsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取耗材出入库明细列表
 */
export function pageConsumableTransactions(params: ConsumableTransactionsSearchParams) {
    return request.get<ConsumableTransactionsList>({
        url: '/itam/consumableTransactions/page',
        params
    })
}

/**
 * 获取耗材出入库明细详情
 * @param id
 */
export function getConsumableTransactions(id: number): any {
    return request.get<ConsumableTransactionsEntity>({
        url: `/itam/consumableTransactions/${id}`
    })
}

/**
 * 创建耗材出入库明细
 * @param data
 */
export function addConsumableTransactions(data: ConsumableTransactionsEntity) {
    return request.post({
        url: '/itam/consumableTransactions/add',
        data
    })
}

/**
 * 修改耗材出入库明细
 * @param data
 */
export function updateConsumableTransactions(data: ConsumableTransactionsEntity) {
    return request.post({
        url: '/itam/consumableTransactions/update',
        data
    })
}

/**
 * 删除耗材出入库明细
 * @param id
 */
export function delConsumableTransactions(id: number | number[]) {
    return request.post({
        url: `/itam/consumableTransactions/delete/${id}`
    })
}

/**
 * 导出耗材出入库明细
 */
export function exportConsumableTransactions(query: ConsumableTransactionsSearchParams) {
    return request.get<Blob>({
        url: '/itam/consumableTransactions/export',
        responseType: 'blob',
        params: query
    })
}
