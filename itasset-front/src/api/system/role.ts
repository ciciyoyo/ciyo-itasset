import request from '@/utils/http'

// 查询角色列表
export function pageRole(query: any, silent?: boolean) {
    return request.get({
        url: '/system/role/page',
        params: query,
        headers: {
            silent: silent
        }
    })
}

// 查询角色列表
export function listRole(query: any, silent?: boolean) {
    return request.get({
        url: '/system/role/list',
        params: query,
        headers: {
            silent: silent
        }
    })
}

// 查询角色详细
export function getRole(roleId: number) {
    return request.get({
        url: '/system/role/' + roleId
    })
}

// 新增角色
export function addRole(data: any) {
    return request.post({
        url: '/system/role',
        data: data
    })
}

// 修改角色
export function updateRole(data: any) {
    return request.put({
        url: '/system/role',
        data: data
    })
}

// 角色数据权限
export function dataScope(data: any) {
    return request.put({
        url: '/system/role/dataScope',
        data: data
    })
}

// 角色状态修改
export function changeRoleStatus(id: number, status: any) {
    const data = {
        id,
        status
    }
    return request.put({
        url: '/system/role/changeStatus',
        data: data
    })
}

// 删除角色
export function delRole(roleId: number) {
    return request.del({
        url: '/system/role/' + roleId
    })
}

// 导出角色
export function exportRole(query: any) {
    return request.get({
        url: '/system/role/export',
        params: query,
        responseType: 'blob'
    })
}

export interface SysRoleEntity {
    id: number
    roleName: string
    roleKey: string
    roleSort: string
    dataScope: string
    menuCheckStrictly?: boolean
    deptCheckStrictly?: boolean
    status: string
    delFlag: string
    flag?: boolean
    menuIds?: Array<string>
    deptIds?: Array<string>
    remark?: string
}
