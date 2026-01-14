import request from '@/utils/http'

// 生成表项类型
export interface GenTableItem {
    id: number
    tableName: string
    tableComment: string
    className: string
    packageName: string
    moduleName: string
    businessName: string
    functionName: string
    functionAuthor: string
    genType: string
    genPath: string
    createTime: string
    updateTime: string
}

// 分页结果类型
export interface GenTablePageResult {
    records: GenTableItem[]
    total: number
    size: number
    current: number
    pages: number
}

// 查询生成表数据
export function listTable(query: any): Promise<GenTablePageResult> {
    return request.get<GenTablePageResult>({
        url: '/tool/gen/list',
        params: query
    })
}

// 查询db数据库列表
export function listDbTable(query: any) {
    return request.get({
        url: '/tool/gen/db/list',
        params: query
    })
}

// 查询表详细信息
export function getGenTable(tableId: number) {
    return request.get({
        url: '/tool/gen/' + tableId
    })
}

// 修改代码生成信息
export function updateGenTable(data: any) {
    return request.put({
        url: '/tool/gen',
        data: data
    })
}

// 导入表
export function importTable(params: any) {
    return request.post({
        url: '/tool/gen/importTable',
        params: params,
        data: {}
    })
}

// 预览生成代码
export function previewTable(tableId: number) {
    return request.get({
        url: '/tool/gen/preview/' + tableId
    })
}

// 删除表数据
export function delTable(tableId: number | number[]) {
    return request.del({
        url: '/tool/gen/' + tableId
    })
}

// 生成代码（自定义路径）
export function genCode(tableName: string) {
    return request.get({
        url: '/tool/gen/genCode/' + tableName
    })
}

// 同步数据库
export function synchDb(tableName: string) {
    return request.get({
        url: '/tool/gen/synchDb/' + tableName
    })
}
