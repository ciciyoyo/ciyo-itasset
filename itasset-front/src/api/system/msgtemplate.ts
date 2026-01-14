import request from '@/utils/http'

export interface MsgTemplate {
    id?: number
    createTime?: string
    updateTime?: string
    searchValue?: string | null
    createBy?: string | null
    updateBy?: string | null
    params?: Record<string, any>
    templateCode?: string
    templateName?: string
    templateContent?: string
    thirdTemplateId?: string | null
    templateType?: number
    templateTypeDesc?: string
}

export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
    orders?: any[]
    optimizeCountSql?: boolean
    searchCount?: boolean
    maxLimit?: number | null
    countId?: string | null
}

// 查询message列表
export const pageMsgTemplate = (params: any) => request.get<PageResult<MsgTemplate>>({
    url: '/sys/msg/template/page',
    params
})

export const listMsgTemplate = (params: any) => request.get<MsgTemplate[]>({url: '/sys/msg/template/list', params})

// 查询message详细
export const getMsgTemplate = (id: number) => request.get<MsgTemplate>({url: '/sys/msg/template/' + id})

// 新增message
export const addMsgTemplate = (data: Partial<MsgTemplate>) => request.post<void>({url: '/sys/msg/template', data})

// 修改message
export const updateMsgTemplate = (data: Partial<MsgTemplate>) => request.put<void>({url: '/sys/msg/template', data})

// 删除message
export const delMsgTemplate = (id: number) => request.del<void>({url: '/sys/msg/template/' + id})

export const sendTemplateMsg = (data: any) => request.post<void>({url: '/sys/msg/template/sendMsg', data})
/**
 * 同步微信模板消息
 */
export const syncWxTemplateMsg = (data: any) => request.post<void>({url: '/sys/msg/template/syncWxMpMsgTemplate', data})
