/**
 * SSE (Server-Sent Events) 工具类
 * 封装 EventSource 连接和自动重连机制
 *
 * @author codeck
 * @create 2026/01/28
 */

import {useUserStore} from '@/store/modules/user'

/**
 * SSE 事件类型枚举
 */
export enum SseEventType {
  /** 连接成功 */
  CONNECTED = 'connected',
  /** 心跳检测 */
  HEARTBEAT = 'heartbeat',
  /** 任务完成 */
  COMPLETE = 'complete',
  /** 进度更新 */
  PROGRESS = 'progress',
  /** 错误事件 */
  ERROR = 'error'
}

/**
 * SSE 事件数据类型定义
 */
export interface SseProgressData {
  rate?: number
  current?: number
  total?: number
  tips?: string
}

export interface SseCompleteData {
  result?: any
  message?: string
  url?: string
  tips?: string
}

export interface SseErrorData {
  error?: string
  message?: string
  code?: string
  details?: string
  suggestion?: string
  level?: 'ERROR' | 'WARN' | 'INFO'
  retryable?: boolean
  extra?: any
}

export interface SseEventData {
  id?: string
  event?: string
  data?: any
  timestamp?: string
}

export interface SseOptions {
  /** 自动重连 */
  autoReconnect?: boolean
  /** 重连间隔(ms) */
  reconnectInterval?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
  /** 连接超时时间(ms) */
  timeout?: number
  /** 心跳检测间隔(ms) */
  heartbeatInterval?: number
}

export interface SseEventHandlers {
  onOpen?: (event: Event) => void
  onMessage?: (data: SseEventData) => void
  onProgress?: (event: MessageEvent, data: SseProgressData) => void
  onComplete?: (event: MessageEvent, data: SseCompleteData) => void
  onError?: (event: MessageEvent, data: SseErrorData) => void
  onClose?: (event: CloseEvent) => void
  onReconnect?: (attempt: number) => void
  onConnected?: (event: MessageEvent) => void
  onHeartbeat?: (event: MessageEvent) => void
}

export class SseClient {
  private eventSource: EventSource | null = null
  private url: string
  private options: Required<SseOptions>
  private handlers: SseEventHandlers
  private reconnectAttempts = 0
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private lastHeartbeat = 0
  private isManualClose = false

  constructor(url: string, handlers: SseEventHandlers, options: SseOptions = {}) {
    this.url = url
    this.handlers = handlers
    this.options = {
      autoReconnect: true,
      reconnectInterval: 3000,
      maxReconnectAttempts: 10,
      timeout: 300000, // 5分钟
      heartbeatInterval: 30000, // 30秒
      ...options
    }
  }

  /**
   * 连接 SSE
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.cleanup()
        this.isManualClose = false

        // 构建带token的URL
        const finalUrl = this.buildUrlWithToken(this.url)

        console.log(`连接 SSE: ${finalUrl}`)
        this.eventSource = new EventSource(finalUrl)

        // 连接打开
        this.eventSource.onopen = (event) => {
          console.log('SSE 连接建立成功')
          this.reconnectAttempts = 0
          this.startHeartbeat()
          this.handlers.onOpen?.(event)
          resolve()
        }

        // 接收消息
        this.eventSource.onmessage = (event) => {
          this.handleMessage(event)
        }

        // 连接错误
        this.eventSource.onerror = (event) => {
          console.error('SSE 连接错误:', event)
          this.handleError(event)

          // 首次连接失败时 reject Promise
          if (this.reconnectAttempts === 0) {
            reject(new Error('SSE 连接失败'))
          }
        }

        // 监听自定义事件
        this.setupEventListeners()
      } catch (error) {
        console.error('创建 SSE 连接失败:', error)
        reject(error)
      }
    })
  }

  /**
   * 手动关闭连接
   */
  close(): void {
    console.log('手动关闭 SSE 连接')
    this.isManualClose = true
    this.cleanup()
  }

  /**
   * 获取连接状态
   */
  getReadyState(): number {
    return this.eventSource?.readyState ?? EventSource.CLOSED
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.eventSource?.readyState === EventSource.OPEN
  }

  /**
   * 构建带token的URL
   */
  private buildUrlWithToken(baseUrl: string): string {
    try {
      const { accessToken } = useUserStore()

      if (!accessToken) {
        console.warn('SSE连接缺少访问token')
        return baseUrl
      }

      const url = new URL(baseUrl, window.location.origin)
      url.searchParams.set('Authorization', accessToken)

      return url.toString()
    } catch (error) {
      console.error('构建SSE URL失败:', error)
      return baseUrl
    }
  }

  /**
   * 处理消息
   */
  private handleMessage(event: MessageEvent): void {
    try {
      this.updateHeartbeat()

      const eventData: SseEventData = {
        id: event.lastEventId,
        data: this.parseEventData(event.data),
        timestamp: new Date().toISOString()
      }

      this.handlers.onMessage?.(eventData)
    } catch (error) {
      console.error('处理 SSE 消息失败:', error)
    }
  }

  /**
   * 解析事件数据
   */
  private parseEventData(data: string): any {
    try {
      return JSON.parse(data)
    } catch {
      return data
    }
  }

  /**
   * 设置事件监听器
   */
  private setupEventListeners(): void {
    if (!this.eventSource) return

    // 进度事件
    this.eventSource.addEventListener(SseEventType.PROGRESS, (event: MessageEvent) => {
      const data = this.parseEventData(event.data) as SseProgressData
      console.log('收到进度事件:', data)
      this.handlers.onProgress?.(event, data)
    })

    // 完成事件
    this.eventSource.addEventListener(SseEventType.COMPLETE, (event: MessageEvent) => {
      const data = this.parseEventData(event.data) as SseCompleteData
      console.log('收到完成事件:', data)
      this.handlers.onComplete?.(event, data)
    })

    // 错误事件
    this.eventSource.addEventListener(SseEventType.ERROR, (event: MessageEvent) => {
      const data = this.parseEventData(event.data) as SseErrorData
      console.error('收到错误事件:', data)
      this.handlers.onError?.(event, data)
    })

    // 连接事件
    this.eventSource.addEventListener(SseEventType.CONNECTED, (event: MessageEvent) => {
      console.log('SSE 连接确认:', event.data)
      this.handlers.onConnected?.(event)
    })

    // 心跳事件
    this.eventSource.addEventListener(SseEventType.HEARTBEAT, (event: MessageEvent) => {
      this.updateHeartbeat()
      this.handlers.onHeartbeat?.(event)
    })
  }

  /**
   * 处理连接错误
   */
  private handleError(event: Event): void {
    if (this.isManualClose) {
      return
    }

    console.warn(`SSE 连接中断，准备重连 (尝试次数: ${this.reconnectAttempts + 1})`)

    if (this.options.autoReconnect && this.reconnectAttempts < this.options.maxReconnectAttempts) {
      this.scheduleReconnect()
    } else {
      console.error('SSE 重连次数已达上限，停止重连')
      this.handlers.onError?.(new Error('SSE 连接失败，重连次数已达上限'))
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectAttempts++
      this.handlers.onReconnect?.(this.reconnectAttempts)

      console.log(`尝试重连 SSE (第 ${this.reconnectAttempts} 次)`)
      this.connect().catch((error) => {
        console.error(`重连失败 (第 ${this.reconnectAttempts} 次):`, error)
      })
    }, this.options.reconnectInterval)
  }

  /**
   * 启动心跳检测
   */
  private startHeartbeat(): void {
    this.updateHeartbeat()

    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
    }

    this.heartbeatTimer = window.setInterval(() => {
      const now = Date.now()
      if (now - this.lastHeartbeat > this.options.heartbeatInterval * 2) {
        console.warn('SSE 心跳超时，可能连接已断开')
        this.handleError(new Event('heartbeat_timeout'))
      }
    }, this.options.heartbeatInterval)
  }

  /**
   * 更新心跳时间
   */
  private updateHeartbeat(): void {
    this.lastHeartbeat = Date.now()
  }

  /**
   * 清理资源
   */
  private cleanup(): void {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
}

/**
 * 创建 SSE 客户端
 */
export function createSseClient(url: string, handlers: SseEventHandlers, options?: SseOptions): SseClient {
  return new SseClient(url, handlers, options)
}

/**
 * SSE 进度监听器
 */
export class SseProgressListener {
  private sseClient: SseClient
  private progressKey: string

  constructor(progressKey: string, baseUrl = '/api/sse') {
    this.progressKey = progressKey

    // 构建基础URL，SseClient会自动添加token
    const url = `${baseUrl}/progress?progressKey=${encodeURIComponent(progressKey)}`

    this.sseClient = new SseClient(url, {
      onOpen: () => console.log(`开始监听进度: ${progressKey}`),
      onError: (error) => console.error('进度监听错误:', error),
      onClose: () => console.log('进度监听结束')
    })
  }

  /**
   * 开始监听
   */
  async start(): Promise<void> {
    return this.sseClient.connect()
  }

  /**
   * 停止监听
   */
  stop(): void {
    this.sseClient.close()
  }

  /**
   * 监听进度更新
   */
  onProgress(callback: (event: MessageEvent, data: SseProgressData) => void): void {
    this.sseClient.handlers.onProgress = callback
  }

  /**
   * 监听完成事件
   */
  onComplete(callback: (event: MessageEvent, data: SseCompleteData) => void): void {
    this.sseClient.handlers.onComplete = callback
  }

  /**
   * 监听错误事件
   */
  onError(callback: (event: MessageEvent, data: SseErrorData) => void): void {
    this.sseClient.handlers.onError = callback
  }

  /**
   * 监听连接事件
   */
  onConnected(callback: (event: MessageEvent) => void): void {
    this.sseClient.handlers.onConnected = callback
  }

  /**
   * 监听心跳事件
   */
  onHeartbeat(callback: (event: MessageEvent) => void): void {
    this.sseClient.handlers.onHeartbeat = callback
  }

  /**
   * 获取连接状态
   */
  isConnected(): boolean {
    return this.sseClient.isConnected()
  }
}
