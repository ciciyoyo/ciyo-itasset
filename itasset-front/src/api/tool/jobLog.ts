import request from '@/utils/http'

// 调度日志详情类型
export interface JobLogItem {
  id: number
  createTime: string
  updateTime: string
  jobName: string
  jobGroup: string
  invokeTarget: string
  jobMessage: string
  status: number
  exceptionInfo: string
  startTime: string
  stopTime: string
}

// 分页结果类型
export interface JobLogPageResult {
  records: JobLogItem[]
  total: number
  size: number
  current: number
  pages: number
}

// 查询调度日志列表
export function listJobLog(query: any): Promise<JobLogPageResult> {
  return request.get<JobLogPageResult>({
    url: '/system/jobLog/page',
    params: query
  })
}

// 删除调度日志
export function delJobLog(jobLogId: number) {
  return request.post({
    url: '/system/jobLog/delete' + jobLogId
  })
}

// 清空调度日志
export function cleanJobLog() {
  return request.post({
    url: '/system/jobLog/clean'
  })
}
