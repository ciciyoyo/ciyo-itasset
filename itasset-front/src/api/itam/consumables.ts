import request from '@/utils/http'

export interface ConsumablesEntity extends Api.Common.BaseEntity {
  id: number
  name: string
  categoryId: string
  manufacturerId: string
  itemNo: string
  orderNumber: string
  purchaseDate: string
  purchaseCost: string
  quantity: string
  minQuantity: string
  locationId: string
  deleted: string
  createBy: string
  updateBy: string
}

type ConsumablesList = Api.Common.PaginatedResponse<ConsumablesEntity>

type ConsumablesSearchFields = {
  id: number
  name: string
  categoryId: string
  manufacturerId: string
  itemNo: string
  orderNumber: string
  purchaseDate: string
  purchaseCost: string
  quantity: string
  minQuantity: string
  locationId: string
  deleted: string
  createBy: string
  updateBy: string
}

type ConsumablesSearchParams = ConsumablesSearchFields & Api.Common.CommonSearchParams

/**
 * 获取耗材列表
 */
export function pageConsumables(params: ConsumablesSearchParams) {
  return request.get<ConsumablesList>({
    url: '/itam/consumables/page',
    params
  })
}

/**
 * 获取耗材详情
 * @param id
 */
export function getConsumables(id: number): any {
  return request.get<ConsumablesEntity>({
    url: `/itam/consumables/${id}`
  })
}

/**
 * 创建耗材
 * @param data
 */
export function addConsumables(data: ConsumablesEntity) {
  return request.post({
    url: '/itam/consumables/add',
    data
  })
}

/**
 * 修改耗材
 * @param data
 */
export function updateConsumables(data: ConsumablesEntity) {
  return request.post({
    url: '/itam/consumables/update',
    data
  })
}

/**
 * 删除耗材
 * @param id
 */
export function delConsumables(id: number | number[]) {
  return request.post({
    url: `/itam/consumables/delete/${id}`
  })
}

/**
 * 导出耗材
 */
export function exportConsumables(query: ConsumablesSearchParams) {
  return request.get<Blob>({
    url: '/itam/consumables/export',
    responseType: 'blob',
    params: query
  })
}

/**
 * 耗材领取
 * @param data
 */
export function collectConsumables(data: {
  consumableId: number
  actionType: string
  quantity: number
  remainingQuantity: number
  note: string
  targetType: string
  targetId: number
}) {
  return request.post({
    url: '/itam/consumables/collect',
    data
  })
}

/**
 * 耗材入库
 * @param data
 */
export function stockInConsumables(data: {
  consumableId: number
  actionType: string
  quantity: number
  remainingQuantity: number
  note: string
}) {
  return request.post({
    url: '/itam/consumables/stockIn',
    data
  })
}

/**
 * 获取耗材分类统计
 */
export function getConsumablesCategory() {
  return request.get<ConsumablesCategoryVO[]>({
    url: '/itam/consumables/stats/category'
  })
}

/**
 * 获取耗材月度统计
 */
export function getConsumablesMonthly() {
  return request.get<ConsumablesMonthlyVO[]>({
    url: '/itam/consumables/stats/monthly'
  })
}

/**
 * 获取耗材概览统计
 */
export function getConsumablesOverview() {
  return request.get<ConsumablesOverviewVO>({
    url: '/itam/consumables/stats/overview'
  })
}

// ============ 统计相关类型定义 ============

/**
 * 耗材分类统计
 */
export interface ConsumablesCategoryVO {
  value: number // 数量
  name: string // 分类名称
}

/**
 * 耗材月度统计
 */
export interface ConsumablesMonthlyVO {
  stockOut: number // 出库数量
  stockIn: number // 入库数量
  month: string // 月份（格式：YYYY-MM）
}

/**
 * 耗材概览统计
 */
export interface ConsumablesOverviewVO {
  totalAmount: number // 总金额
  monthStockIn: number // 本月入库
  totalQuantity: number // 库存总量
  monthStockOut: number // 本月出库
  lowStockCount: number // 低库存预警数
  skuCount: number // 耗材种类数 (SKU数)
}

// ============ 出入库明细相关 ============

/**
 * 出入库明细实体
 */
export interface ConsumableTransactionEntity extends Api.Common.BaseEntity {
  id: number
  consumableId: number
  consumableName?: string
  operatorId: number
  operatorName?: string
  actionType: string
  quantity: number
  remainingQuantity: number
  targetType?: string
  targetId?: number
  targetName?: string
  note?: string
}

type ConsumableTransactionList = Api.Common.PaginatedResponse<ConsumableTransactionEntity>

type ConsumableTransactionSearchParams = {
  consumableId?: number
  actionType?: string
  targetType?: string
} & Api.Common.CommonSearchParams

/**
 * 获取出入库明细列表
 */
export function pageConsumableTransactions(params: ConsumableTransactionSearchParams) {
  return request.get<ConsumableTransactionList>({
    url: '/itam/consumables/transactions/page',
    params
  })
}
