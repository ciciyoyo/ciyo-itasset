import request from '@/utils/http'

/** 菜单实体 */
export interface MenuEntity {
  id?: number
  createTime?: string
  updateTime?: string | null
  searchValue?: any
  createBy?: string
  updateBy?: string
  params?: Record<string, any>
  menuName?: string
  langKey?: string | null
  parentName?: string | null
  parentId: number
  orderNum?: number
  path?: string
  component?: string
  isFrame: number // 是否为外链 (0: 否, 1: 是)
  isCache: number // 是否缓存 (0: 缓存, 1: 不缓存)
  menuType: 'M' | 'C' | 'F' // M: 目录, C: 菜单, F: 按钮
  visible: string // 显示状态 ("0": 显示, "1": 隐藏)
  status: string // 菜单状态 ("0": 正常, "1": 停用)
  location?: string | null
  perms?: string
  icon?: string
  children?: MenuEntity[]
}

/** 菜单查询参数 */
export interface MenuQueryParams {
  menuName?: string
  visible?: string
  status?: string
}

/** 菜单列表响应 */
export interface MenuListResponse {
  data: MenuEntity[]
}

/** 菜单树选择响应 */
export interface MenuTreeResponse {
  data: MenuEntity[]
}

/** 角色菜单树响应 */
export interface RoleMenuTreeResponse {
  checkedKeys: number[]
  menus: MenuEntity[]
}

/** 通用响应 */
export interface CommonResponse {
  code: number
  msg: string
  data?: any
}

/**
 * 查询菜单列表
 * @param query 查询参数
 * @returns 菜单列表
 */
export function listMenu(query?: MenuQueryParams) {
  return request.get<MenuEntity[]>({
    url: '/system/menu/list',
    params: query
  })
}

/**
 * 查询菜单详细
 * @param menuId 菜单ID
 * @returns 菜单详情
 */
export function getMenu(menuId: number) {
  return request.get<MenuEntity>({
    url: `/system/menu/${menuId}`
  })
}

/**
 * 查询菜单下拉树结构
 * @returns 菜单树
 */
export function treeselect() {
  return request.get<MenuTreeResponse>({
    url: '/system/menu/treeselect'
  })
}

/**
 * 根据角色ID查询菜单下拉树结构
 * @param roleId 角色ID
 * @returns 角色菜单树
 */
export function roleMenuTreeselect(roleId: number) {
  return request.get<RoleMenuTreeResponse>({
    url: `/system/menu/roleMenuTreeselect/${roleId}`
  })
}

/**
 * 新增菜单
 * @param data 菜单数据
 * @returns 操作结果
 */
export function addMenu(data: Omit<MenuEntity, 'id'>) {
  return request.post<CommonResponse>({
    url: '/system/menu',
    data
  })
}

/**
 * 修改菜单
 * @param data 菜单数据
 * @returns 操作结果
 */
export function updateMenu(data: MenuEntity) {
  return request.put<CommonResponse>({
    url: '/system/menu',
    data
  })
}

/**
 * 删除菜单
 * @param menuId 菜单ID
 * @returns 操作结果
 */
export function delMenu(menuId: number) {
  return request.del<CommonResponse>({
    url: `/system/menu/${menuId}`
  })
}
