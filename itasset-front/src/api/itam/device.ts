import request from '@/utils/http'

export interface DeviceEntity extends Api.Common.BaseEntity {
  id?: number
  modelId: string
  categoryId?: number
  serial: string
  name: string
  imageUrl?: string
  purchaseDate?: string
  purchaseCost?: string
  warrantyDate?: string
  locationId?: string
  supplierId?: string
  depreciationId?: string
  description?: string
  deviceNo: string
}

/**
 * 配件信息
 */
export interface AccessoryVO extends Api.Common.BaseEntity {
  id?: number
  name?: string
  assetNumber?: string
  categoryId?: number
  supplierId?: number
  manufacturerId?: number
  specifications?: string
  locationId?: number
  depreciationId?: number
  quantity?: number
  minQuantity?: number
  purchaseDate?: string
  warrantyExpirationDate?: string
  purchaseCost?: number
  orderNumber?: string
  serialNumber?: string
  description?: string
  categoryName?: string
  supplierName?: string
  manufacturerName?: string
  locationName?: string
  depreciationName?: string
}

/**
 * 许可证信息
 */
export interface LicenseVO extends Api.Common.BaseEntity {
  id?: number
  name?: string
  licenseKey?: string
  totalSeats?: number
  manufacturerId?: number
  categoryId?: number
  minQty?: number
  licensedToName?: string
  licensedToEmail?: string
  supplierId?: number
  orderNumber?: string
  purchaseCost?: number
  purchaseDate?: string
  expirationDate?: string
  terminationDate?: string
  purchaseOrderNumber?: string
  notes?: string
  categoryName?: string
  manufacturerName?: string
  supplierName?: string
}

/**
 * 服务信息
 */
export interface ServiceVO extends Api.Common.BaseEntity {
  id?: number
  name?: string
  supplierId?: number
  serviceNumber?: string
  startDate?: string
  endDate?: string
  cost?: number
  notes?: string
  targetType?: string
  targetTypeDesc?: string
  targetId?: number
  offeringStatus?: string
  offeringStatusDesc?: string
  supplierName?: string
  targetName?: string
  assignDate?: string
}

/**
 * 耗材信息
 */
export interface ConsumableVO extends Api.Common.BaseEntity {
  id?: number
  name?: string
  quantity?: number
  categoryName?: string
  supplierName?: string
}

/**
 * 型号信息
 */
export interface ModelVO extends Api.Common.BaseEntity {
  id?: number
  name?: string
  imageUrl?: string
  manufacturerId?: number
  categoryId?: number
  depreciationId?: number
  modelNumber?: string
  eol?: number
  categoryName?: string
  manufacturerName?: string
  depreciationName?: string
}

/**
 * 设备详情VO（包含关联信息）
 */
export interface DeviceDetailVO extends DeviceEntity {
  assetsStatus?: number
  assetsStatusDesc?: string
  assignedTo?: number
  assignedToName?: string
  modelName?: string
  locationName?: string
  supplierName?: string
  depreciationName?: string
  deleted?: number
  accessories?: AccessoryVO[]
  licenses?: LicenseVO[]
  services?: ServiceVO[]
  consumables?: ConsumableVO[]
  model?: ModelVO
}

type DeviceList = Api.Common.PaginatedResponse<DeviceEntity>

type DeviceSearchFields = {
  serial?: string
  name?: string
  deviceNo?: string
  categoryId?: number
  status?: string
}

type DeviceSearchParams = DeviceSearchFields & Api.Common.CommonSearchParams

/**
 * 获取设备列表
 */
export function pageDevice(params: DeviceSearchParams) {
  return request.get<DeviceList>({
    url: '/itam/device/page',
    params
  })
}

/**
 * 获取设备列表
 */
export function listDevice(params: DeviceSearchFields) {
  return request.get<DeviceList>({
    url: '/itam/device/list',
    params
  })
}

/**
 * 获取设备详情
 * @param id
 */
export function getDevice(id: number): any {
  return request.get<DeviceEntity>({
    url: `/itam/device/${id}`
  })
}

/**
 * 获取设备详情（包含关联信息）
 * @param id
 */
export function getDeviceDetail(id: number) {
  return request.get<DeviceDetailVO>({
    url: `/itam/device/detail/${id}`
  })
}

/**
 * 创建设备
 * @param data
 */
export function addDevice(data: DeviceEntity) {
  return request.post({
    url: '/itam/device/add',
    data
  })
}

/**
 * 修改设备
 * @param data
 */
export function updateDevice(data: DeviceEntity) {
  return request.post({
    url: '/itam/device/update',
    data
  })
}

/**
 * 删除设备
 * @param id
 */
export function delDevice(id: number | number[]) {
  return request.post({
    url: `/itam/device/delete/${id}`
  })
}

/**
 * 导出设备
 */
export function exportDevice(query: DeviceSearchParams) {
  return request.get<Blob>({
    url: '/itam/device/export',
    responseType: 'blob',
    params: query
  })
}

export interface AllocateData {
  itemType: string
  itemId: number
  ownerType: string
  ownerId: number
  note?: string
}

/**
 * 分配设备
 */
export function allocateDevice(data: AllocateData) {
  return request.post({
    url: '/itam/device/allocate',
    data
  })
}

export interface AllocationEntity extends Api.Common.BaseEntity {
  id: number
  itemId: number
  itemType: string
  itemTypeDesc: string
  itemName: string
  ownerId: number
  ownerType: string
  ownerTypeDesc: string
  ownerName: string
  quantity: number
  status: string
  statusDesc: string
  assignDate: string
  returnDate: string | null
  note: string
}

/**
 * 获取分配列表
 */
export function pageAllocation(params: any) {
  return request.get<Api.Common.PaginatedResponse<AllocationEntity>>({
    url: '/itam/device/allocation/page',
    params
  })
}

/**
 * 报废设备
 */
export function scrappedDevice(ids: number[]) {
  return request.post({
    url: '/itam/device/scrap',
    data: { ids }
  })
}

/**
 * 取消分配
 */
export function deallocateDevice(id: number) {
  return request.post({
    url: `/itam/device/deallocate/${id}`
  })
}

export interface DeviceSummaryVO {
  total: number
  expired: number
  scrapped: number
  soonToExpire: number
}

export interface DeviceMonthlyValueVO {
  statsMonth: string
  totalValue: number
}

/**
 * 获取设备统计概要
 */
export function getDeviceSummary() {
  return request.get<DeviceSummaryVO>({
    url: '/itam/device/stats/summary'
  })
}

/**
 * 获取设备月度价值统计
 */
export function getDeviceMonthlyValue(year?: number) {
  return request.get<DeviceMonthlyValueVO[]>({
    url: '/itam/device/stats/monthly-value',
    params: { year }
  })
}
