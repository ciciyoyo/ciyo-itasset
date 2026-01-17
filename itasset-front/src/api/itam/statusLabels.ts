import request from '@/utils/http'

export interface StatusLabelsEntity extends Api.Common.BaseEntity {
  id: number
  name: string
  type: string
}

type StatusLabelsList = Api.Common.PaginatedResponse<StatusLabelsEntity>

type StatusLabelsSearchFields = {
  id: number
  name: string
  type: string
}

type StatusLabelsSearchParams = StatusLabelsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取状态标签列表
 */
export function listStatusLabels(params: StatusLabelsSearchParams) {
  return request.get<StatusLabelsList>({
    url: '/itam/statusLabels/page',
    params
  })
}

/**
 * 获取状态标签详情
 * @param id
 */
export function getStatusLabels(id: number): any {
  return request.get<StatusLabelsEntity>({
    url: `/itam/statusLabels/${id}`
  })
}

/**
 * 创建状态标签
 * @param data
 */
export function addStatusLabels(data: StatusLabelsEntity) {
  return request.post({
    url: '/itam/statusLabels/add',
    data
  })
}

/**
 * 修改状态标签
 * @param data
 */
export function updateStatusLabels(data: StatusLabelsEntity) {
  return request.post({
    url: '/itam/statusLabels/update',
    data
  })
}

/**
 * 删除状态标签
 * @param id
 */
export function delStatusLabels(id: number | number[]) {
  return request.post({
    url: `/itam/statusLabels/delete/${id}`
  })
}

/**
 * 导出状态标签
 */
export function exportStatusLabels(query: StatusLabelsSearchParams) {
  return request.get<Blob>({
    url: '/itam/statusLabels/export',
    responseType: 'blob',
    params: query
  })
}
