import request from '@/utils/http'

// 任务详情类型
export interface JobItem {
    id: number
    jobName: string
    jobGroup: string
    invokeTarget: string
    cronExpression: string
    misfirePolicy: string
    concurrent: string
    status: string | number
    createBy?: string
    createTime?: string
    updateBy?: string
    updateTime?: string
    remark?: string
}

// 查询定时任务调度列表
export function listJob(query: any) {
    return request.get({
        url: '/system/job/page',
        params: query
    })
}

// 查询定时任务调度详细
export function getJob(jobId: number): Promise<JobItem> {
    return request.get<JobItem>({
        url: '/system/job/' + jobId
    })
}

// 新增定时任务调度
export function addJob(data: any) {
    return request.post({
        url: '/system/job/add',
        data: data
    })
}

// 修改定时任务调度
export function updateJob(data: any) {
    return request.post({
        url: '/system/job/update',
        data: data
    })
}

// 删除定时任务调度
export function delJob(jobId: number) {
    return request.post({
        url: '/system/job/delete/' + `${jobId}`
    })
}

// 任务状态修改
export function changeJobStatus(id: number, status: any) {
    const data = {
        id,
        status
    }
    return request.post({
        url: '/system/job/changeStatus',
        data: data
    })
}

// 定时任务立即执行一次
export function runJob(id: number, jobGroup: any) {
    const data = {
        id,
        jobGroup
    }
    return request.post({
        url: '/system/job/run',
        data: data
    })
}
