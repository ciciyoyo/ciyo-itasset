import request from '@/utils/http'

export interface ManufacturersEntity extends Api.Common.BaseEntity {
  id?: number
  name: string
  officialUrl?: string
  supportUrl?: string
  warrantyUrl?: string
  supportPhone?: string
  supportEmail?: string
  logoUrl?: string
  remark?: string
}

type ManufacturersList = Api.Common.PaginatedResponse<ManufacturersEntity>

type ManufacturersSearchFields = {
  id?: number
  name?: string
  officialUrl?: string
  supportUrl?: string
  warrantyUrl?: string
  supportPhone?: string
  supportEmail?: string
  logoUrl?: string
  remark?: string
}

type ManufacturersSearchParams = ManufacturersSearchFields & Api.Common.CommonSearchParams

/**
 * 获取制造商列表（分页）
 */
export function pageManufacturers(params: ManufacturersSearchParams) {
  return request.get<ManufacturersList>({
    url: '/itam/manufacturers/page',
    params
  })
}

/**
 * 获取制造商列表（不分页）
 */
export function listManufacturers(params?: ManufacturersSearchFields) {
  return request.get<ManufacturersEntity[]>({
    url: '/itam/manufacturers/list',
    params
  })
}

/**
 * 获取制造商详情
 * @param id
 */
export function getManufacturers(id: number): any {
  return request.get<ManufacturersEntity>({
    url: `/itam/manufacturers/${id}`
  })
}

/**
 * 创建制造商
 * @param data
 */
export function addManufacturers(data: ManufacturersEntity) {
  return request.post({
    url: '/itam/manufacturers/add',
    data
  })
}

/**
 * 修改制造商
 * @param data
 */
export function updateManufacturers(data: ManufacturersEntity) {
  return request.post({
    url: '/itam/manufacturers/update',
    data
  })
}

/**
 * 删除制造商
 * @param id
 */
export function delManufacturers(id: number | number[]) {
  return request.post({
    url: `/itam/manufacturers/delete/${id}`
  })
}

/**
 * 导出制造商
 */
export function exportManufacturers(query: ManufacturersSearchParams) {
  return request.get<Blob>({
    url: '/itam/manufacturers/export',
    responseType: 'blob',
    params: query
  })
}

/**
 * 导入制造商
 * @param file 文件
 * @param updateSupport 是否支持更新
 * @param progressKey 进度标识key（可选）
 * @param sessionId 会话ID（可选）
 */
export function importManufacturers(file: FormData, updateSupport: boolean = false, progressKey?: string, sessionId?: string) {
  // 添加额外参数
  if (updateSupport !== undefined) {
    file.append('updateSupport', updateSupport.toString())
  }
  if (progressKey) {
    file.append('progressKey', progressKey)
  }
  if (sessionId) {
    file.append('sessionId', sessionId)
  }

  return request.post<string>({
    url: '/itam/manufacturers/importData',
    data: file,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取制造商导入模板
 */
export function downloadManufacturersTemplate() {
  return request.get<Blob>({
    url: '/itam/manufacturers/importTemplate',
    responseType: 'blob'
  })
}
