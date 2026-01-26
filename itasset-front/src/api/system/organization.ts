import request from '@/utils/http'
// 同步企微
export const syncWechatCpRequest = () => request.get({ url: '/organization/syncCpWx' })

export function pageUser(query: any, silent?: boolean) {
  return request.get({
    url: '/organization/user/page',
    params: query,
    headers: {
      silent: silent
    }
  })
}

// 导出一个名为 pageRole 的函数，该函数用于获取角色分页数据
export function pageRole(query: any) {
  return request.get({
    url: '/organization/role/page',
    params: query
  })
}

// 导出一个名为 pageRole 的函数，该函数用于获取角色分页数据
export function pagePost(query: any) {
  return request.get({
    url: '/organization/post/page',
    params: query
  })
}

export const getRoleList = (query: any) => request.get({ url: '/organization/role/list', params: query })

export const getDeptTree = (query: any) => request.get({ url: '/organization/dept/tree', params: query })
