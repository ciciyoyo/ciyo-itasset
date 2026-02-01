/**
 * SSE (Server-Sent Events) 工具类
 * 简化版 - 全局单连接，根据 progressKey 分发事件
 * 智能保持策略：无订阅时延迟关闭连接
 *
 * @author codeck
 * @create 2026/01/31
 */

import {useUserStore} from '@/store/modules/user'

/** SSE 事件类型 */
export enum SseEventType {
  CONNECTED = 'connected',
  HEARTBEAT = 'heartbeat',
  COMPLETE = 'complete',
  PROGRESS = 'progress',
  ERROR = 'error'
}

/** 进度数据 */
export interface SseProgressData {
  progressKey?: string
  rate?: number
  current?: number
  total?: number
  tips?: string
  url?: string
}

/** 完成数据 */
export interface SseCompleteData {
  progressKey?: string
  rate?: number
  tips?: string
  url?: string
  result?: any
}

/** 错误数据 */
export interface SseErrorData {
  progressKey?: string
  code?: string
  message?: string
  details?: string
  suggestion?: string
  level?: 'ERROR' | 'WARN' | 'INFO'
  retryable?: boolean
}

/** 回调函数类型 */
export type ProgressCallback = (data: SseProgressData) => void
export type CompleteCallback = (data: SseCompleteData) => void
export type ErrorCallback = (data: SseErrorData) => void

/** 订阅回调 */
export interface SseSubscription {
  onProgress?: ProgressCallback
  onComplete?: CompleteCallback
  onError?: ErrorCallback
}

/**
 * SSE 全局连接管理器（单例）
 * 智能保持策略：有订阅时保持连接，无订阅一段时间后自动关闭
 */
class SseManager {
  private static instance: SseManager | null = null

  private eventSource: EventSource | null = null
  private subscriptions = new Map<string, SseSubscription>()
  private reconnectTimer: number | null = null
  private idleTimer: number | null = null // 空闲关闭定时器
  private reconnectAttempts = 0
  private baseUrl = '/api/sse'

  // 配置
  private readonly maxReconnectAttempts = 10
  private readonly reconnectInterval = 3000
  private readonly idleTimeout = 30000 // 无订阅 30 秒后关闭

  private constructor() {}

  static getInstance(): SseManager {
    if (!SseManager.instance) {
      SseManager.instance = new SseManager()
    }
    return SseManager.instance
  }

  /**
   * 确保已连接（可单独调用）
   */
  async ensureConnected(): Promise<void> {
    this.cancelIdleClose()
    if (!this.isConnected()) {
      await this.connect()
    }
  }

  /**
   * 订阅进度（自动建立连接）
   */
  async subscribe(progressKey: string, callbacks: SseSubscription): Promise<void> {
    await this.ensureConnected()
    console.log('[SSE] 订阅:', progressKey)
    this.subscriptions.set(progressKey, callbacks)
  }

  /**
   * 取消订阅（无订阅时延迟关闭）
   */
  unsubscribe(progressKey: string): void {
    console.log('[SSE] 取消订阅:', progressKey)
    this.subscriptions.delete(progressKey)

    // 检查是否需要延迟关闭
    // if (this.subscriptions.size === 0) {
    //   this.scheduleIdleClose()
    // }
  }

  /**
   * 强制关闭连接
   */
  close(): void {
    console.log('[SSE] 强制关闭')
    this.cancelIdleClose()
    this.cleanup()
    this.subscriptions.clear()
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.eventSource?.readyState === EventSource.OPEN
  }

  /**
   * 获取状态信息
   */
  getStatus(): { connected: boolean; subscriptions: number } {
    return {
      connected: this.isConnected(),
      subscriptions: this.subscriptions.size
    }
  }

  // ==================== 私有方法 ====================

  private connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnected()) {
        resolve()
        return
      }

      this.cleanup()

      const url = this.buildUrl(`${this.baseUrl}/connect`)
      console.log('[SSE] 建立连接')

      try {
        this.eventSource = new EventSource(url)

        this.eventSource.onopen = () => {
          console.log('[SSE] 连接成功')
          this.reconnectAttempts = 0
          resolve()
        }

        this.eventSource.onerror = () => {
          if (this.reconnectAttempts === 0) {
            reject(new Error('SSE 连接失败'))
          }
          this.handleReconnect()
        }

        this.setupListeners()
      } catch (error) {
        reject(error)
      }
    })
  }

  private buildUrl(baseUrl: string): string {
    const { accessToken } = useUserStore()
    const url = new URL(baseUrl, window.location.origin)
    if (accessToken) {
      url.searchParams.set('Authorization', accessToken)
    }
    return url.toString()
  }

  private setupListeners(): void {
    if (!this.eventSource) return

    // 进度事件
    this.eventSource.addEventListener(SseEventType.PROGRESS, (e: MessageEvent) => {
      console.log('[SSE] 收到进度消息 原始数据:', e.data)
      const { progressKey, data } = this.parseData(e.data)
      console.log('[SSE] 进度消息解析:', { progressKey, data })
      if (progressKey) this.dispatch(progressKey, 'onProgress', data)
    })

    // 完成事件
    this.eventSource.addEventListener(SseEventType.COMPLETE, (e: MessageEvent) => {
      console.log('[SSE] 收到完成消息 原始数据:', e.data)
      const { progressKey, data } = this.parseData(e.data)
      console.log('[SSE] 完成消息解析:', { progressKey, data })
      if (progressKey) {
        this.dispatch(progressKey, 'onComplete', data)
        this.unsubscribe(progressKey) // 完成后自动取消
      }
    })

    // 错误事件
    this.eventSource.addEventListener(SseEventType.ERROR, (e: MessageEvent) => {
      console.log('[SSE] 收到错误消息 原始数据:', e.data)
      const { progressKey, data } = this.parseData(e.data)
      console.log('[SSE] 错误消息解析:', { progressKey, data })
      if (progressKey) this.dispatch(progressKey, 'onError', data)
    })

    // 心跳（保持连接）
    this.eventSource.addEventListener(SseEventType.HEARTBEAT, (e: MessageEvent) => {
      console.log('[SSE] 收到心跳消息:', e.data)
    })
  }

  private parseData(data: string): { progressKey: string; data: any } {
    try {
      // 后端返回的是 SseEvent 对象，包含 progressKey 和 data 字段
      const sseEvent = JSON.parse(data)
      return {
        progressKey: sseEvent.progressKey || '',
        data: sseEvent.data || sseEvent // 如果 data 不存在则使用整个对象
      }
    } catch {
      return { progressKey: '', data: null }
    }
  }

  private dispatch(progressKey: string, type: keyof SseSubscription, data: any): void {
    console.log('[SSE] 分发消息:', { progressKey, type, data })
    const sub = this.subscriptions.get(progressKey)
    const callback = sub?.[type]
    if (callback) {
      console.log('[SSE] 执行回调:', type)
      ;(callback as Function)(data)
    } else {
      console.warn('[SSE] 未找到回调:', { progressKey, type })
    }
  }

  private handleReconnect(): void {
    if (this.subscriptions.size === 0) return // 无订阅不重连

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectTimer = window.setTimeout(() => {
        this.reconnectAttempts++
        console.log(`[SSE] 重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
        this.connect().catch(() => {})
      }, this.reconnectInterval)
    } else {
      console.error('[SSE] 重连失败，已达上限')
    }
  }

  private scheduleIdleClose(): void {
    this.cancelIdleClose()
    console.log(`[SSE] ${this.idleTimeout / 1000}秒后无订阅将关闭连接`)
    this.idleTimer = window.setTimeout(() => {
      if (this.subscriptions.size === 0) {
        console.log('[SSE] 空闲超时，关闭连接')
        this.cleanup()
      }
    }, this.idleTimeout)
  }

  private cancelIdleClose(): void {
    if (this.idleTimer) {
      clearTimeout(this.idleTimer)
      this.idleTimer = null
    }
  }

  private cleanup(): void {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.cancelIdleClose()
  }
}

// 导出单例
export const sseManager = SseManager.getInstance()

/**
 * 便捷方法：监听进度
 *
 * @example
 * const unsubscribe = await listenProgress('import_123', {
 *   onProgress: (data) => console.log('进度:', data.rate),
 *   onComplete: (data) => console.log('完成:', data),
 *   onError: (data) => console.error('错误:', data)
 * })
 *
 * // 取消监听
 * unsubscribe()
 */
export async function listenProgress(progressKey: string, callbacks: SseSubscription): Promise<() => void> {
  await sseManager.subscribe(progressKey, callbacks)
  return () => sseManager.unsubscribe(progressKey)
}
