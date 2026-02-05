import request from '@/utils/http'

export interface CategoriesEntity extends Api.Common.BaseEntity {
  id: number
  name: string
  code: string
  parentId: string
  categoryType: string
  deleted: string
  createBy: string
  updateBy: string
}

type CategoriesList = Api.Common.PaginatedResponse<CategoriesEntity>

type CategoriesSearchFields = {
  id: number
  name: string
  code: string
  parentId: string
  categoryType: string
  deleted: string
  createBy: string
  updateBy: string
}

type CategoriesSearchParams = CategoriesSearchFields & Api.Common.CommonSearchParams

/**
 * 获取分类列表
 */
export function pageCategories(params: CategoriesSearchParams) {
  return request.get<CategoriesList>({
    url: '/itam/categories/page',
    params
  })
}

/**
 * 获取分类详情
 * @param id
 */
export function getCategories(id: number): any {
  return request.get<CategoriesEntity>({
    url: `/itam/categories/${id}`
  })
}

/**
 * 创建分类
 * @param data
 */
export function addCategories(data: CategoriesEntity) {
  return request.post({
    url: '/itam/categories/add',
    data
  })
}

/**
 * 修改分类
 * @param data
 */
export function updateCategories(data: CategoriesEntity) {
  return request.post({
    url: '/itam/categories/update',
    data
  })
}

/**
 * 删除分类
 * @param id
 */
export function delCategories(id: number | number[]) {
  return request.post({
    url: `/itam/categories/delete/${id}`
  })
}

/**
 * 导出分类
 */
export function exportCategories(query: CategoriesSearchParams) {
  return request.get<Blob>({
    url: '/itam/categories/export',
    responseType: 'blob',
    params: query
  })
}

/**
 * 获取分类树形结构
 * @param categoryType 分类类型
 */
export function getCategoriesTree(categoryType: string) {
  return request.get<CategoriesEntity[]>({
    url: '/itam/categories/tree',
    params: { categoryType }
  })
}

/**
 * 导入分类
 * @param file 文件
 * @param categoryType 分类类型
 */
export function importCategories(file: File, categoryType: string, progressKey?: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('categoryType', categoryType)
  if (progressKey) {
    formData.append('progressKey', progressKey)
  }
  return request.post({
    url: '/itam/categories/import',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
