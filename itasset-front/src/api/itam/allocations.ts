import request from '@/utils/http'

export interface AllocationsEntity extends Api.Common.BaseEntity {
  id: number
  itemType: string
  itemId: string
  ownerType: string
  ownerId: string
  quantity: string
  status: string
  assignDate: string
  returnDate: string
  note: string
}

export interface AllocationsVO {
  id: number
  createTime: string
  updateTime: string | null
  createBy: number
  updateBy: number | null
  itemType: string
  itemTypeDesc: string
  itemId: number
  ownerType: string
  ownerTypeDesc: string
  ownerId: number
  quantity: number
  status: string
  statusDesc: string
  assignDate: string
  returnDate: string | null
  note: string | null
  itemName: string
  ownerName: string
}

type AllocationsList = Api.Common.PaginatedResponse<AllocationsVO>

type AllocationsSearchFields = {
  id: number
  itemType: string
  itemId: string
  ownerType: string
  ownerId: string
  quantity: string
  status: string
  assignDate: string
  returnDate: string
  note: string
}

type AllocationsSearchParams = AllocationsSearchFields & Api.Common.CommonSearchParams

/**
 * 获取资源分配/领用明细列表
 */
export function pageAllocations(params: AllocationsSearchParams) {
  return request.get<AllocationsList>({
    url: '/itam/allocations/page',
    params
  })
}

/**
 * 获取资源分配/领用明细详情
 * @param id
 */
export function getAllocations(id: number): any {
  return request.get<AllocationsEntity>({
    url: `/itam/allocations/${id}`
  })
}

/**
 * 创建资源分配/领用明细
 * @param data
 */
export function addAllocations(data: AllocationsEntity) {
  return request.post({
    url: '/itam/allocations/add',
    data
  })
}

/**
 * 修改资源分配/领用明细
 * @param data
 */
export function updateAllocations(data: AllocationsEntity) {
  return request.post({
    url: '/itam/allocations/update',
    data
  })
}

/**
 * 删除资源分配/领用明细
 * @param id
 */
export function delAllocations(id: number | number[]) {
  return request.post({
    url: `/itam/allocations/delete/${id}`
  })
}

/**
 * 导出资源分配/领用明细
 */
export function exportAllocations(query: AllocationsSearchParams) {
  return request.get<Blob>({
    url: '/itam/allocations/export',
    responseType: 'blob',
    params: query
  })
}
