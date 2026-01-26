import request from '@/utils/http'

/** 登录参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  needChangePassword: boolean
}

/** 用户信息 */
export interface UserInfo {
  permissions: string[]
  roles: string[]
  user: {
    id: number
    deptId: number
    userName: string
    nickName: string
    email: string
    phonenumber: string
    sex: string
    avatar: string
    password: string
    status: string
    delFlag: string
    loginIp: string
    loginDate: string
    dept: any
    roles: any[]
    roleIds: any
    postIds: any
    updateTime: any
    createTime: string
    extraInfo: any
  }
}

/**
 * 登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: LoginParams) {
  return request.post<LoginResponse>({
    url: '/login',
    params
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<UserInfo>({
    url: '/getInfo'
  })
}
