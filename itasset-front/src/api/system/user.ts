import request from '@/utils/http'
import {SysRoleEntity} from '@/api/system/role'
import {DeptEntity} from '@/api/system/dept'

/** 用户实体 */
export interface UserEntity {
  id: number
  deptId: string
  userName: string
  nickName?: string
  email?: string
  phonenumber?: string
  sex?: string
  avatar?: string
  password?: string
  salt?: string
  status?: string
  delFlag?: string
  loginIp?: string
  loginDate?: string
  roleIds?: string[]
  postIds?: string[]
  remark?: string
  createTime?: string
  /** 所属部门 */
  dept?: DeptEntity
}

/** 用户分页查询参数 */
export type UserPageParams = {
  userName?: string
  phonenumber?: string
  status?: string
  deptId?: string
} & Api.Common.CommonSearchParams

/** 重置密码参数 */
export interface ResetPwdParams {
  id: number
  password: string
}

/** 修改用户状态参数 */
export interface ChangeStatusParams {
  id: number
  status: string | number
}

/** 更新密码参数 */
export interface UpdatePwdParams {
  oldPassword: string
  newPassword: string
}

/** 授权角色参数 */
export interface AuthRoleParams {
  userId: number
  roleIds: string[]
}

/** 用户列表 */
type UserList = Api.Common.PaginatedResponse<UserEntity>

/**
 * 查询用户列表
 * @param query 查询参数
 * @param silent 是否静默请求
 * @returns 用户分页数据
 */
export function pageUser(query: UserPageParams, silent?: boolean) {
  return request.get<UserList>({
    url: '/system/user/page',
    params: query,
    headers: {
      silent: silent
    }
  })
}

export interface UserInfo {
  roles: SysRoleEntity[]
  user: UserEntity
  postIds: number[]
  roleIds: number[]
}

/**
 * 查询用户详细
 * @param userId 用户ID
 * @returns 用户信息
 */
export function getUser(userId?: number | string) {
  return request.get<UserInfo>({
    url: '/system/user/' + userId
  })
}

/**
 * 新增用户
 * @param data 用户数据
 * @returns 操作结果
 */
export function addUser(data: UserEntity) {
  return request.post({
    url: '/system/user',
    data
  })
}

/**
 * 修改用户
 * @param data 用户数据
 * @returns 操作结果
 */
export function updateUser(data: UserEntity) {
  return request.put({
    url: '/system/user',
    data
  })
}

/**
 * 删除用户
 * @param userId 用户ID
 * @returns 操作结果
 */
export function delUser(userId: number) {
  return request.del({
    url: '/system/user/' + userId
  })
}

/**
 * 导出用户
 * @param query 查询参数
 * @returns Blob数据
 */
export function exportUser(query: UserPageParams) {
  return request.get<Blob>({
    url: '/system/user/export',
    responseType: 'blob',
    params: query
  })
}

/**
 * 用户密码重置
 * @param id 用户ID
 * @param password 新密码
 * @returns 操作结果
 */
export function resetUserPwd(id: number, password: string) {
  const data: ResetPwdParams = {
    id,
    password
  }
  return request.put({
    url: '/system/user/resetPwd',
    data
  })
}

/**
 * 用户状态修改
 * @param id 用户ID
 * @param status 状态
 * @returns 操作结果
 */
export function changeUserStatus(id: number, status: string | number) {
  const data: ChangeStatusParams = {
    id,
    status
  }
  return request.put({
    url: '/system/user/changeStatus',
    data
  })
}

/** 用户个人资料响应 */
export interface UserProfileResponse {
  user: UserEntity & {
    roles?: SysRoleEntity[]
    dept?: DeptEntity
  }
  postGroup: string
}

/**
 * 查询用户个人信息
 * @returns 用户信息
 */
export function getUserProfile() {
  return request.get<UserProfileResponse>({
    url: '/system/user/profile'
  })
}

/**
 * 修改用户个人信息
 * @param data 用户信息
 * @returns 操作结果
 */
export function updateUserProfile(data: Partial<UserEntity>) {
  return request.put({
    url: '/system/user/profile',
    data
  })
}

/**
 * 用户密码重置
 * @param oldPassword 旧密码
 * @param newPassword 新密码
 * @param token 授权token
 * @returns 操作结果
 */
export function updateUserPwd(oldPassword: string, newPassword: string, token?: string) {
  const data: UpdatePwdParams = {
    oldPassword,
    newPassword
  }
  let url = '/system/user/profile/updatePwd'
  if (token) {
    url = url + '?Authorization=' + token
  }
  return request.post({
    url,
    data
  })
}

/**
 * 用户头像上传
 * @param data 表单数据
 * @returns 操作结果
 */
export function uploadAvatar(data: FormData) {
  return request.post({
    url: '/system/user/profile/avatar',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载用户导入模板
 * @returns Blob数据
 */
export function importTemplate() {
  return request.get<Blob>({
    url: '/system/user/importTemplate',
    responseType: 'blob'
  })
}

/**
 * 查询授权角色
 * @param userId 用户ID
 * @returns 授权角色信息
 */
export function getAuthRole(userId: number) {
  return request.get({
    url: '/system/user/authRole/' + userId
  })
}

/**
 * 保存授权角色
 * @param data 授权角色数据
 * @returns 操作结果
 */
export function updateAuthRole(data: AuthRoleParams) {
  return request.put({
    url: '/system/user/authRole',
    params: data
  })
}

/** 重置密码参数 */
export interface ResetPwdParams {
  id: number
  password: string
}

/** 修改用户状态参数 */
export interface ChangeStatusParams {
  id: number
  status: string | number
}

/** 更新密码参数 */
export interface UpdatePwdParams {
  oldPassword: string
  newPassword: string
}

/** 授权角色参数 */
export interface AuthRoleParams {
  userId: number
  roleIds: string[]
}
