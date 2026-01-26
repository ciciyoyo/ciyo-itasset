import request from '@/utils/http'

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
  orders?: any[]
  searchCount?: boolean
}

// 查询系统通告列表
export const listAnnouncement = (params: any) =>
  request.get<PageResult<AnnouncementItem>>({
    url: '/sys/announce/page',
    params
  })

// 查询系统通告详细
export const getAnnouncement = (id: number | string | (number | string)[]) =>
  request.get<{ data: AnnouncementForm & { userList?: User[] } }>({ url: '/sys/announce/' + id })

// 新增系统通告
export const addAnnouncement = (data: Partial<AnnouncementForm>) => request.post<void>({ url: '/sys/announce', data })

// 修改系统通告
export const updateAnnouncement = (data: Partial<AnnouncementForm>) => request.put<void>({ url: '/sys/announce', data })

// 删除系统通告
export const delAnnouncement = (id: number | string | (number | string)[]) => request.del<void>({ url: '/sys/announce/' + id })

// 发布系统公告
export const releaseAnnouncement = (id: number | string) => request.post<void>({ url: `/sys/announce/release/${id}` })

// 撤销系统公告
export const revokeAnnouncement = (id: number | string) => request.post<void>({ url: `/sys/announce/revoke/${id}` })

interface MyAnnouncementSendReq {
  current?: number
  size?: number
  msgCategory?: string
  delFlag?: boolean
  title?: string | null
}

/**
 * 查询我的消息列表
 * @param params
 */
export const getMyAnnouncementSend = (params: MyAnnouncementSendReq) =>
  request.get<MessagePageRes>({ url: '/sys/announce/getMyAnnouncementSend', params })

/**
 * 首页通知列表
 * @param params
 */
export const getMyUnReadAnnouncementSend = (params: any) => request.get({ url: '/sys/announce/unread/list', params })

/**
 * 首页通知列表
 * @param params
 */
export const getMyAnnouncementNotice = (params: any) => request.get({ url: '/sys/announce/notice', params })

/**
 * 标记消息已读
 */
export const readAnnouncementSend = (id: number) => request.put<Message>({ url: '/sys/announce/read/' + id })

/**
 * 标记全部已读
 */
export const readAllAnnouncementSend = () => request.put<Message>({ url: '/sys/announce/readAll/' })

export interface Message {
  anntId?: number
  openType?: string
  openPage?: string
  msgContent: string
  priorityDesc?: string
  readFlag?: boolean
  sendTime?: string
  sender?: string
  title?: string
  priority?: 'H' | 'L' | 'M'
  id?: number
}

export interface MessagePageRes {
  current: number
  pages: number
  records: Message[]
  size: number
  total: number
}

// 公告查询参数接口
export interface AnnouncementQueryParams {
  current: number
  size: number
  title: string | null
}

// 用户接口
export interface User {
  id: string | number
  nickName: string
}

// 公告表单接口
export interface AnnouncementForm {
  id?: string | number | null
  title?: string | null
  msgContent?: string | null
  startTime?: string | null
  endTime?: string | null
  priority: string
  msgCategory: string
  msgType: string
  sendTime?: string | null
  userIds?: string | null
  msgAbstract?: string | null
  userIdList: (string | number)[]
  userSelectList: User[]
}

// 公告列表项接口
export interface AnnouncementItem {
  id: string | number
  title: string
  sender: string
  priorityDesc: string
  msgTypeDesc: string
  sendStatus: string
  sendTime: string
  cancelTime: string
  msgAbstract: string
  userIds?: string
}
