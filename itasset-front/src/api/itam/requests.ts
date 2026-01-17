import request from '@/utils/http'

export interface AssetRequestEntity {
  id: number
  requestNo: string
  userId: number
  userName: string
  itemType: string
  itemTypeDesc: string
  categoryId: number
  categoryName: string
  itemId: number
  itemName: string
  quantity: number
  reason: string
  status: string // e.g. 'pending', 'approved', 'rejected'
  approverId: number
  approverName: string
  approvalTime: string
  approvalNote: string
  createTime: string
}

export type AssetRequestVO = AssetRequestEntity

type AssetRequestsList = Api.Common.PaginatedResponse<AssetRequestVO>

export type AssetRequestSearchFields = {
  requestNo?: string
  userId?: number
  itemType?: string
  status?: string
  startDate?: string
  endDate?: string
}

type AssetRequestSearchParams = AssetRequestSearchFields & Api.Common.CommonSearchParams

/**
 * 分页查询资产申请
 */
export function pageAssetRequests(params: AssetRequestSearchParams) {
  return request.get<AssetRequestsList>({
    url: '/itam/asset-requests/manage/page',
    params
  })
}

/**
 * 审批申请 (通过或驳回)
 */
export interface AssetRequestApprovalParams {
  id: number
  status: 'approved' | 'rejected'
  allocatedItemId?: number
  approvalNote?: string
}

export function approveRequest(data: AssetRequestApprovalParams) {
  return request.post<void>({
    url: '/itam/asset-requests/approve',
    data
  })
}

/**
 * 审批通过并分配资产
 */
export function approveAndAssignRequest(params: { id: number; deviceId: number; note?: string }) {
  return approveRequest({
    id: params.id,
    status: 'approved',
    allocatedItemId: params.deviceId,
    approvalNote: params.note
  })
}

/**
 * 驳回申请
 */
export function rejectRequest(params: { id: number; reason: string }) {
  return approveRequest({
    id: params.id,
    status: 'rejected',
    approvalNote: params.reason
  })
}
