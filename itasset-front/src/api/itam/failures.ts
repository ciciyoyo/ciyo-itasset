import request from '@/utils/http'
import { AssetType, FailureStatus } from './enums'

export interface FailuresEntity extends Api.Common.BaseEntity {
  id?: number
  targetType: AssetType
  targetId: number
  failureName: string
  failureDescription?: string
  failureDate?: string
  status?: FailureStatus
  reportedBy?: string
  resolvedBy?: string
  resolvedDate?: string
  notes?: string
}

export interface FailuresVO extends FailuresEntity {
  targetTypeDesc?: string
  statusDesc?: string
  targetName?: string
  reportedByName?: string
  resolvedByName?: string
}

type FailuresList = Api.Common.PaginatedResponse<FailuresVO>

type FailuresSearchFields = {
  targetType?: string
  targetId?: string | number
  failureName?: string
  targetName?: string
}

type FailuresSearchParams = FailuresSearchFields & Api.Common.CommonSearchParams

/**
 * 获取故障列表
 */
export function pageFailures(params: FailuresSearchParams) {
  return request.get<FailuresList>({
    url: '/itam/failures/page',
    params
  })
}

/**
 * 获取故障详情
 * @param id
 */
export function getFailures(id: number) {
  return request.get<FailuresVO>({
    url: `/itam/failures/${id}`
  })
}

/**
 * 报告故障
 * @param data
 */
export function reportFailure(data: FailuresEntity) {
  return request.post({
    url: '/itam/failures/report',
    data
  })
}

/**
 * 修改故障
 * @param data
 */
export function updateFailures(data: FailuresEntity) {
  return request.post({
    url: '/itam/failures/update',
    data
  })
}

/**
 * 删除故障
 * @param id
 */
export function delFailures(id: number | number[]) {
  return request.post({
    url: `/itam/failures/delete/${id}`
  })
}

/**
 * 导出故障
 */
export function exportFailures(query: FailuresSearchParams) {
  return request.get<Blob>({
    url: '/itam/failures/export',
    responseType: 'blob',
    params: query
  })
}
/**
 * 解决故障
 * @param data
 */
export function resolveFailures(data: FailuresEntity) {
  return request.post({
    url: '/itam/failures/resolve',
    data
  })
}
