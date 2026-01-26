import request from '@/utils/http'

export interface ModelsEntity extends Api.Common.BaseEntity {
  id?: number
  createTime?: string
  updateTime?: string
  createBy?: number
  updateBy?: number
  name: string
  imageUrl?: string | null
  manufacturerId?: number
  categoryId?: number
  depreciationId?: number
  modelNumber: string
  eol?: number // 报废年限(月)
  deleted?: number
  // 以下字段为API返回的关联名称字段，用于列表显示
  manufacturerName?: string
  depreciationName?: string
}

type ModelsList = Api.Common.PaginatedResponse<ModelsEntity>

type ModelsSearchFields = {
  name?: string
  modelNumber?: string
  manufacturerId?: number
  depreciationId?: number
}

type ModelsSearchParams = ModelsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取型号列表
 */
export function pageModels(params: ModelsSearchParams) {
  return request.get<ModelsList>({
    url: '/itam/models/page',
    params
  })
}

/**
 * 获取型号列表（不分页）
 */
export function listModels(params?: ModelsSearchFields) {
  return request.get<ModelsEntity[]>({
    url: '/itam/models/list',
    params
  })
}

/**
 * 获取型号详情
 * @param id
 */
export function getModels(id: number): any {
  return request.get<ModelsEntity>({
    url: `/itam/models/${id}`
  })
}

/**
 * 创建型号
 * @param data
 */
export function addModels(data: Partial<ModelsEntity>) {
  return request.post({
    url: '/itam/models/add',
    data
  })
}

/**
 * 修改型号
 * @param data
 */
export function updateModels(data: Partial<ModelsEntity>) {
  return request.post({
    url: '/itam/models/update',
    data
  })
}

/**
 * 删除型号
 * @param id
 */
export function delModels(id: number | number[]) {
  return request.post({
    url: `/itam/models/delete/${id}`
  })
}

/**
 * 导出型号
 */
export function exportModels(query: ModelsSearchParams) {
  return request.get<Blob>({
    url: '/itam/models/export',
    responseType: 'blob',
    params: query
  })
}
