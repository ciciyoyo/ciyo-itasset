import request from '@/utils/http'

// 部门实体类型
export interface DeptEntity {
    /**
     * 部门ID
     */
    id?: number

    /**
     * 父部门ID
     */
    parentId?: number

    /**
     * 部门名称
     */
    deptName: string

    /**
     * 显示顺序
     */
    orderNum: number

    /**
     * 负责人
     */
    leader?: string

    /**
     * 联系电话
     */
    phone?: string

    /**
     * 邮箱
     */
    email?: string

    /**
     * 部门状态（0正常 1停用）
     */
    status: string

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    delFlag?: string

    /**
     * 父部门名称
     */
    parentName?: string

    /**
     * 创建时间
     */
    createTime?: string

    /**
     * 更新时间
     */
    updateTime?: string
}

// 部门岗位实体类型
export interface DeptPostEntity {
    /**
     * 部门岗位ID
     */
    id?: number

    /**
     * 部门ID
     */
    deptId?: number

    /**
     * 岗位ID
     */
    postId?: number

    /**
     * 岗位编码
     */
    postCode?: string

    /**
     * 岗位名称
     */
    postName?: string

    /**
     * 岗位排序
     */
    postSort?: number

    /**
     * 状态（0正常 1停用）
     */
    status?: string

    /**
     * 是否是领导岗位（0否 1是）
     */
    leaderPost?: number

    /**
     * 创建时间
     */
    createTime?: string

    /**
     * 更新时间
     */
    updateTime?: string
}

// 部门分页查询参数
export interface DeptPageParams {
    current?: number
    size?: number
    deptName?: string
    status?: string
}

// 部门岗位分页查询参数
export interface DeptPostPageParams {
    current?: number
    size?: number
    deptId?: number
}

// 部门分页响应
export interface DeptPageResponse {
    records: DeptEntity[]
    total: number
}

// 部门岗位分页响应
export interface DeptPostPageResponse {
    records: DeptPostEntity[]
    total: number
}

/**
 * 查询部门列表
 * @param query 查询参数
 * @returns 部门列表数据
 */
export function pageDept(query?: DeptPageParams) {
    return request.get<DeptEntity[]>({
        url: '/system/dept/page',
        params: query
    })
}

/**
 * 查询部门列表
 * @param query 查询参数
 * @returns 部门列表数据
 */
export function listDept(query: any) {
    return request.get<DeptEntity[]>({
        url: '/system/dept/list',
        params: query
    })
}

/**
 * 查询部门列表（排除节点）
 * @param deptId 部门ID
 * @returns 部门列表数据
 */
export function listDeptExcludeChild(deptId: number) {
    return request.get<DeptEntity[]>({
        url: '/system/dept/list/exclude/' + deptId
    })
}

/**
 * 查询部门详细
 * @param deptId 部门ID
 * @returns 部门信息
 */
export function getDept(deptId: number) {
    return request.get<DeptEntity>({
        url: '/system/dept/' + deptId
    })
}

/**
 * 查询部门下拉树结构
 * @param silent 是否静默请求
 * @returns 部门树结构数据
 */
export function treeselect(silent?: boolean) {
    return request.get<DeptEntity[]>({
        url: '/system/dept/treeselect',
        headers: {
            silent: silent
        }
    })
}

/**
 * 根据角色ID查询部门树结构
 * @param roleId 角色ID
 * @returns 部门树结构数据
 */
export function roleDeptTreeselect(roleId: number) {
    return request.get<any>({
        url: '/system/dept/roleDeptTreeselect/' + roleId
    })
}

/**
 * 新增部门
 * @param data 部门数据
 * @returns 结果
 */
export function addDept(data: DeptEntity) {
    return request.post<any>({
        url: '/system/dept',
        data
    })
}

/**
 * 修改部门
 * @param data 部门数据
 * @returns 结果
 */
export function updateDept(data: DeptEntity) {
    return request.put<any>({
        url: '/system/dept',
        data
    })
}

/**
 * 删除部门
 * @param deptId 部门ID
 * @returns 结果
 */
export function delDept(deptId: number) {
    return request.del<any>({
        url: '/system/dept/' + deptId
    })
}

/**
 * 分页查询部门岗位列表
 * @param query 查询参数
 * @returns 部门岗位分页数据
 */
export function pageDeptPost(query: DeptPostPageParams) {
    return request.get<DeptPostPageResponse>({
        url: '/system/dept/post/page',
        params: query
    })
}

/**
 * 查询部门不包含的岗位
 * @param query 查询参数
 * @returns 岗位列表数据
 */
export function queryDeptNotInPost(query: any) {
    return request.get<any>({
        url: '/system/dept/post/queryDeptNotInPost',
        params: query
    })
}

/**
 * 查询部门包含的岗位
 * @param query 查询参数
 * @returns 岗位列表数据
 */
export function queryDeptInPost(query: any) {
    return request.get<any>({
        url: '/system/dept/post/queryDeptInPost',
        params: query
    })
}

/**
 * 新增部门岗位
 * @param data 部门岗位数据
 * @returns 结果
 */
export function settingDeptPost(data: any) {
    return request.post<any>({
        url: '/system/dept/post/settingDeptPost',
        data
    })
}

/**
 * 修改部门岗位
 * @param data 部门岗位数据
 * @returns 结果
 */
export function updateDeptPost(data: any) {
    return request.post<any>({
        url: '/system/dept/post/update',
        data
    })
}

/**
 * 删除部门岗位
 * @param deptPostId 部门岗位ID
 * @returns 结果
 */
export function delDeptPost(deptPostId: number) {
    return request.del<any>({
        url: '/system/dept/post/' + deptPostId
    })
}
