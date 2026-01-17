import request from '@/utils/http'

export interface DeviceVO {
  id: number
  name: string
  assetNo: string
  model: string
  status: string
  assignedTo: number
  assignDate: string
}

/**
 * 获取我的设备列表
 */
export function getMyDevices(params?: Api.Common.PaginationParams) {
  return request.get<Api.Common.PaginatedResponse<DeviceVO>>({
    url: '/itam/personal/devices',
    params
  })
}

export interface PersonalStatsVO {
  deviceCount: number
  deviceDetail: string
  pendingRequestCount: number
  pendingRequestDetail: string
  daysInUse: number
  daysInUseDetail: string
  expiringCount: number
  expiringDetail: string
}

/**
 * 获取个人首页统计数据
 */
export function getPersonalStats() {
  return request.get<PersonalStatsVO>({
    url: '/itam/personal/stats'
  })
}

export interface AssetRequestsVO {
  id: number
  requestNo: string
  userId: number
  userName: string
  itemType: string // Enum code likely
  categoryId: number
  categoryName: string
  itemId: number
  itemName: string
  quantity: number
  reason: string
  status: string // Enum code likely
  approverId: number
  approverName: string
  approvalTime: string
  approvalNote: string
  allocatedItemId?: number
  allocatedItemName?: string
  createTime: string
}

type AssetRequestsList = Api.Common.PaginatedResponse<AssetRequestsVO>

/**
 * 获取我的申请列表
 */
export function getMyRequests(params?: Api.Common.CommonSearchParams) {
  return request.get<AssetRequestsList>({
    url: '/itam/personal/requests',
    params
  })
}

export interface SubmitRequestParams {
  itemType: string
  categoryId?: number
  itemId?: number
  quantity: number
  reason?: string
  isLongTerm?: boolean
  expectedReturnTime?: string
}

/**
 * 提交资产申请
 */
export function submitAssetRequest(data: SubmitRequestParams) {
  return request.post<void>({
    url: '/itam/asset-requests/submit',
    data
  })
}

export interface ReturnDeviceParams {
  deviceId: number
  returnDate: string
  reason?: string
}

/**
 * 提交设备归还申请
 */
export function submitDeviceReturn(data: ReturnDeviceParams) {
  return request.post<void>({
    url: '/itam/personal/return',
    data
  })
}
