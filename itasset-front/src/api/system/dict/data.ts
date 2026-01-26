import request from '@/utils/http'

/** 字典数据实体 */
export interface DictDataEntity {
  dictCode?: number
  dictSort?: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 字典数据分页查询参数 */
export interface DictDataPageParams {
  pageNum?: number
  pageSize?: number
  dictType?: string
  dictLabel?: string
  status?: string
}

/** 字典数据分页响应 */
export interface DictDataPageResponse {
  records: DictDataEntity[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 查询字典数据列表
 * @param query 查询参数
 * @returns 字典数据分页数据
 */
export function listData(query: DictDataPageParams) {
  return request.get<DictDataPageResponse>({
    url: '/system/dict/data/page',
    params: query
  }) as Promise<DictDataPageResponse>
}

/**
 * 查询字典数据详细
 * @param dictCode 字典编码
 * @returns 字典数据信息
 */
export function getData(dictCode: string | number) {
  return request.get<DictDataEntity>({
    url: '/system/dict/data/' + dictCode
  }) as Promise<DictDataEntity>
}

/**
 * 根据字典类型查询字典数据信息
 * @param dictType 字典类型
 * @returns 字典数据列表
 */
export function getDicts(dictType: string) {
  return request.get<DictDataEntity[]>({
    url: '/system/dict/data/type/' + dictType
  }) as Promise<DictDataEntity[]>
}

/**
 * 新增字典数据
 * @param data 字典数据
 * @returns 操作结果
 */
export function addData(data: DictDataEntity) {
  return request.post({
    url: '/system/dict/data',
    data
  })
}

/**
 * 修改字典数据
 * @param data 字典数据
 * @returns 操作结果
 */
export function updateData(data: DictDataEntity) {
  return request.put({
    url: '/system/dict/data',
    data
  })
}

/**
 * 删除字典数据
 * @param dictCode 字典编码
 * @returns 操作结果
 */
export function delData(dictCode: string | number) {
  return request.del({
    url: '/system/dict/data/' + dictCode
  })
}

/**
 * 导出字典数据
 * @param query 查询参数
 * @returns Blob数据
 */
export function exportData(query: DictDataPageParams) {
  return request.get<Blob>({
    url: '/system/dict/data/export',
    responseType: 'blob',
    params: query
  })
}
