import request from '@/utils/http'

// 操作日志实体类型定义
export interface OperlogEntity {
    id?: number
    title?: string
    businessType?: number
    method?: string
    requestMethod?: string
    operatorType?: number
    operName?: string
    deptName?: string
    operUrl?: string
    operIp?: string
    operLocation?: string
    operParam?: string
    jsonResult?: string
    status?: number
    errorMsg?: string
    operTime?: string
    costTime?: number
}

// 分页响应类型定义
export interface PageResponse<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 查询操作日志列表
export function list(query: any) {
    return request.get<PageResponse<OperlogEntity>>({
        url: '/monitor/operlog/page',
        params: query
    })
}

// 删除操作日志
export function delOperlog(operId: number) {
    return request.del({
        url: '/monitor/operlog/' + operId
    })
}

// 清空操作日志
export function cleanOperlog() {
    return request.del({
        url: '/monitor/operlog/clean'
    })
}

// 导出操作日志
export function exportOperlog(query: any) {
    return request.get({
        url: '/monitor/operlog/export',
        responseType: 'blob',
        params: query
    })
}
