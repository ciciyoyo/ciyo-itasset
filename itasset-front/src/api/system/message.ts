import request from '@/utils/http'

export interface ReqPage {
    current?: number
    size?: number
}

export interface MessageLog {
    id?: number
    title?: string
    content?: string
    msgParams?: string
    receiver?: string
    sendTime?: string
    sendStatus?: string
    sendStatusDesc?: string
    sendNum?: number
    result?: string
    remark?: string
    type?: number
    typeDesc?: string
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

// 查询msg列表
export const listMessage = (params: any) => request.get<PageResult<MessageLog>>({url: '/sys/msg/page', params})

// 查询msg详细
export const getMessage = (id: number) => request.get<MessageLog>({url: '/sys/msg/' + id})

// 新增msg
export const addMessage = (data: Partial<MessageLog>) => request.post<void>({url: '/sys/msg', data})

// 修改msg
export const updateMessage = (data: Partial<MessageLog>) => request.put<void>({url: '/sys/msg', data})

// 删除msg
export const delMessage = (id: number | string) => request.del<void>({url: '/sys/msg/' + id})

// 查询消息发送详情
export const listMessageSendDetails = (params: MessageSendDetailsParam) =>
    request.get<PageResult<MessageSendDetail>>({url: 'sys/msg/sendDetail/page', params})

export interface MessageSendDetailsParam extends ReqPage {
    sourceType: string
    sourceId: string
}

export interface MessageSendDetail {
    /**
     * ID
     */
    id: number
    /**
     * 来源类型
     */
    sourceType: string
    /**
     * 来源Id
     */
    sourceId: string
    /**
     * 消息iD
     */
    templateId: number
    /**
     * 发送条数
     */
    sendCount: number
    /**
     * 消息类型
     */
    msgType: string
    /**
     * 消息内容
     */
    msgContent: string
}

// 查询消息发送操作中的详情
export const listSendDetailsMsgList = (sendId: number, param: SendDetailsMsgListParam) =>
    request.get<PageResult<PushMessage>>({url: `/sys/msg/sendDetail/${sendId}/page`, params: param})

export interface SendDetailsMsgListParam extends ReqPage {
    receiver: string
}

export interface PushMessage {
    /**
     * 消息Id
     */
    msgId: string

    /**
     * 推送内容
     */
    content: string

    /**
     * 推送所需参数Json格式
     */
    msgParams: string

    /**
     * 接收人
     */
    receiver: string

    /**
     * 推送失败原因
     */
    result: string

    /**
     * 发送次数
     */
    sendNum: number

    /**
     * 推送状态 0未推送 1推送成功 2推送失败
     */
    sendStatus: number

    /**
     * 推送时间
     */
    sendTime: Date

    /**
     * 消息标题
     */
    title: string

    /**
     * 推送方式：1短信 2邮件 3微信
     */
    type: number

    /**
     * 备注
     */
    remark: string

    /**
     * 发送详情Id
     */
    sendDetailId: number

    /**
     * 扩展状态
     */
    extStatus: number
}
