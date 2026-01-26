import request from '@/utils/http'

export interface AssetsMonthlyStatsEntity extends Api.Common.BaseEntity {
  id: number
  assetsType: string
  assetsId: string
  categoryId: string
  initialValue: string
  currentValue: string
  accumulatedDepreciation: string
  statsMonth: string
  statsDate: string
}

type AssetsMonthlyStatsList = Api.Common.PaginatedResponse<AssetsMonthlyStatsEntity>

type AssetsMonthlyStatsSearchFields = {
  id: number
  assetsType: string
  assetsId: string
  categoryId: string
  initialValue: string
  currentValue: string
  accumulatedDepreciation: string
  statsMonth: string
  statsDate: string
}

type AssetsMonthlyStatsSearchParams = AssetsMonthlyStatsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取资产月度价值统计列表
 */
export function pageAssetsMonthlyStats(params: AssetsMonthlyStatsSearchParams) {
  return request.get<AssetsMonthlyStatsList>({
    url: '/itam/assetsMonthlyStats/page',
    params
  })
}

/**
 * 获取资产月度价值统计详情
 * @param id
 */
export function getAssetsMonthlyStats(id: number): any {
  return request.get<AssetsMonthlyStatsEntity>({
    url: `/itam/assetsMonthlyStats/${id}`
  })
}

/**
 * 创建资产月度价值统计
 * @param data
 */
export function addAssetsMonthlyStats(data: AssetsMonthlyStatsEntity) {
  return request.post({
    url: '/itam/assetsMonthlyStats/add',
    data
  })
}

/**
 * 修改资产月度价值统计
 * @param data
 */
export function updateAssetsMonthlyStats(data: AssetsMonthlyStatsEntity) {
  return request.post({
    url: '/itam/assetsMonthlyStats/update',
    data
  })
}

/**
 * 删除资产月度价值统计
 * @param id
 */
export function delAssetsMonthlyStats(id: number | number[]) {
  return request.post({
    url: `/itam/assetsMonthlyStats/delete/${id}`
  })
}

/**
 * 导出资产月度价值统计
 */
export function exportAssetsMonthlyStats(query: AssetsMonthlyStatsSearchParams) {
  return request.get<Blob>({
    url: '/itam/assetsMonthlyStats/export',
    responseType: 'blob',
    params: query
  })
}
