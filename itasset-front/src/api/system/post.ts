import request from '@/utils/http'

// Post entity interface
export interface SysPostEntity {
  id?: number
  postCode: string
  postName: string
  level?: number
  leaderPost?: number
  status: string
  remark?: string
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  searchValue?: any
  params?: Record<string, any>
}

// Paginated response interface
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  orders?: any[]
  optimizeCountSql?: boolean
  searchCount?: boolean
  maxLimit?: any
  countId?: any
  pages: number
}

// Query params interface
export interface PostQueryParams {
  current: number
  size: number
  postCode?: string
  postName?: string
  status?: string
}

// 查询岗位列表
export function listPost(query: PostQueryParams): Promise<PageResponse<SysPostEntity>> {
  return request.get({
    url: '/system/post/page',
    params: query
  })
}

// 查询岗位详细
export function getPost(postId: number): Promise<SysPostEntity> {
  return request.get({
    url: '/system/post/' + postId
  })
}

// 新增岗位
export function addPost(data: Partial<SysPostEntity>) {
  return request.post({
    url: '/system/post',
    data: data
  })
}

// 修改岗位
export function updatePost(data: Partial<SysPostEntity>) {
  return request.put({
    url: '/system/post',
    data: data
  })
}

// 删除岗位
export function delPost(postId: number | string) {
  return request.del({
    url: '/system/post/' + postId
  })
}

// 导出岗位
export function exportPost(query: PostQueryParams) {
  return request.get({
    url: '/system/post/export',
    params: query,
    responseType: 'blob'
  })
}
