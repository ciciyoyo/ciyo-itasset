import request from '@/utils/http'

export interface LicensesEntity extends Api.Common.BaseEntity {
  id: number
  deleted: string
  name: string
  licenseKey: string
  totalSeats: string
  manufacturerId: string
  categoryId: string
  minQty: string
  licensedToName: string
  licensedToEmail: string
  supplierId: string
  orderNumber: string
  purchaseCost: string
  purchaseDate: string
  expirationDate: string
  terminationDate: string
  purchaseOrderNumber: string
  notes: string
  createBy: string
  updateBy: string
}

type LicensesList = Api.Common.PaginatedResponse<LicensesEntity>

type LicensesSearchFields = {
  id: number
  deleted: string
  name: string
  licenseKey: string
  totalSeats: string
  manufacturerId: string
  categoryId: string
  minQty: string
  licensedToName: string
  licensedToEmail: string
  supplierId: string
  orderNumber: string
  purchaseCost: string
  purchaseDate: string
  expirationDate: string
  terminationDate: string
  purchaseOrderNumber: string
  notes: string
  createBy: string
  updateBy: string
}

type LicensesSearchParams = LicensesSearchFields & Api.Common.CommonSearchParams

/**
 * 获取软件授权列表
 */
export function pageLicenses(params: LicensesSearchParams) {
  return request.get<LicensesList>({
    url: '/itam/licenses/page',
    params
  })
}

/**
 * 获取软件授权详情
 * @param id
 */
export function getLicenses(id: number): any {
  return request.get<LicensesEntity>({
    url: `/itam/licenses/${id}`
  })
}

/**
 * 创建软件授权
 * @param data
 */
export function addLicenses(data: LicensesEntity) {
  return request.post({
    url: '/itam/licenses/add',
    data
  })
}

/**
 * 修改软件授权
 * @param data
 */
export function updateLicenses(data: LicensesEntity) {
  return request.post({
    url: '/itam/licenses/update',
    data
  })
}

/**
 * 删除软件授权
 * @param id
 */
export function delLicenses(id: number | number[]) {
  return request.post({
    url: `/itam/licenses/delete/${id}`
  })
}

/**
 * 导出软件授权
 */
export function exportLicenses(query: LicensesSearchParams) {
  return request.get<Blob>({
    url: '/itam/licenses/export',
    responseType: 'blob',
    params: query
  })
}

/**
 * 分配软件到设备
 * @param data
 */
export function allocateLicense(data: { licenseId: number; ownerId: number; ownerType: string }) {
  return request.post({
    url: '/itam/licenses/allocate',
    data
  })
}

// 统计相关类型定义
export interface LicenseIndicatorsVO {
  totalCount: number
  insufficientCount: number
  expiringSoonCount: number
  expiredCount: number
}

export interface LicenseMonthlyValueVO {
  statsMonth: string
  assetsType: string
  assetsTypeDesc: string
  totalValue: number
}

export interface LicenseCategoryVO {
  count: number
  categoryName: string
}

/**
 * 解除软件关联
 * @param allocationId 分配记录ID
 */
export function deallocateLicense(allocationId: number) {
  return request.post({
    url: `/itam/licenses/deallocate/${allocationId}`
  })
}

/**
 * 获取软件统计指标
 */
export function getLicenseIndicators() {
  return request.get<LicenseIndicatorsVO>({
    url: '/itam/licenses/stats/indicators'
  })
}

/**
 * 获取软件月度价值统计
 */
export function getLicenseMonthlyValue() {
  return request.get<LicenseMonthlyValueVO[]>({
    url: '/itam/licenses/stats/monthly-value'
  })
}

/**
 * 获取软件分类统计
 */
export function getLicenseCategory() {
  return request.get<LicenseCategoryVO[]>({
    url: '/itam/licenses/stats/category'
  })
}
