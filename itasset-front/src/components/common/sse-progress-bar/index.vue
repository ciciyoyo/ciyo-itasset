<template>
  <div v-if="showContent" class="sse-progress-container">
    <!-- 进度条 -->
    <el-progress :percentage="progressData.rate || 0" :show-text="true" :stroke-width="8" class="progress-bar" />

    <!-- 最新消息显示 -->
    <div class="message-display" v-if="latestMessage">
      <div class="message-header">
        <span class="message-title">最新消息</span>
        <el-button type="primary" size="small" plain @click="showFullScreenMessages" :icon="FullScreen"> 全屏查看 </el-button>
      </div>

      <div class="latest-message-container">
        <div class="latest-message">
          <div class="message-meta">
            <span class="message-time-type">
              {{ formatTime(latestMessage.timestamp) }} |
              <span :class="`type-${latestMessage.type}`">{{ getMessageTypeText(latestMessage.type) }}</span>
            </span>
          </div>
          <div class="message-content" :title="latestMessage.content">{{ latestMessage.content }}</div>
        </div>
      </div>
    </div>

    <!-- 全屏消息弹窗 -->
    <el-dialog
      v-model="fullScreenVisible"
      title="消息详情"
      width="90%"
      :close-on-click-modal="false"
      append-to-body
      class="fullscreen-dialog"
    >
      <div class="fullscreen-message-container" ref="fullScreenMessageRef">
        <div v-for="(message, index) in messageList" :key="index" class="message-item" :class="`type-${message.type}`">
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          <div class="message-content-item">{{ message.content }}</div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="scrollToTop">回到顶部</el-button>
          <el-button @click="scrollToBottom">滚动到底部</el-button>
          <el-button @click="clearMessages" type="warning">清空消息</el-button>
          <el-button @click="fullScreenVisible = false" type="primary">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
  import { SseProgressData, SseProgressListener } from '@/utils/sse'
  import { FullScreen } from '@element-plus/icons-vue'

  interface Props {
    /** 进度标识 */
    progressKey: string
    /** SSE 基础URL */
    baseUrl?: string
    /** 自动开始监听 */
    autoStart?: boolean
  }

  interface ProgressData {
    rate?: number
    current?: number
    total?: number
    tips?: string
    url?: string
  }

  interface MessageItem {
    content: string
    type: 'info' | 'success' | 'warning' | 'error' | 'connected'
    timestamp: number
  }

  const props = withDefaults(defineProps<Props>(), {
    baseUrl: '/api/sse',
    autoStart: true
  })

  const emit = defineEmits<{
    complete: [result: any]
    error: [error: any]
    cancel: []
    close: []
  }>()

  // 响应式数据
  const progressData = ref<ProgressData>({})

  // 消息相关数据
  const messageList = ref<MessageItem[]>([])
  const fullScreenVisible = ref(false)
  const fullScreenMessageRef = ref<HTMLElement>()

  // SSE 监听器
  let sseListener: SseProgressListener | null = null
  let currentProgressKey: string = ''

  // 最新消息
  const latestMessage = computed(() => {
    return messageList.value.length > 0 ? messageList.value[messageList.value.length - 1] : null
  })

  // 是否显示内容（progressData有数据或有业务消息时显示）
  const showContent = computed(() => {
    const hasProgressData = Object.keys(progressData.value).length > 0
    return hasProgressData
  })

  // 监听 props 变化 - 只在 key 变化时重置状态，不自动启动连接
  watch(
    () => props.progressKey,
    (newKey, oldKey) => {
      console.log(`progressKey 变化: 从 ${oldKey} 到 ${newKey}`)
      if (oldKey && oldKey !== newKey) {
        stopProgress()
        progressData.value = {}
        messageList.value = []
      }
    }
  )

  // 生命周期 - 不自动启动，由父组件调用 ensureConnection
  onMounted(() => {
    // 移除自动启动逻辑，改为由父组件主动调用
  })

  onUnmounted(() => {
    stopProgress()
  })

  /**
   * 开始进度监听
   */
  async function startProgress() {
    if (!props.progressKey) {
      console.warn('progressKey 为空，无法启动进度监听')
      return
    }

    if (sseListener && currentProgressKey === props.progressKey && sseListener.isConnected()) {
      console.log(`已存在相同 progressKey (${props.progressKey}) 的有效 SSE 连接，跳过创建`)
      return
    }

    if (sseListener && currentProgressKey !== props.progressKey) {
      console.log(`停止之前的SSE监听 (${currentProgressKey})，启动新的监听 (${props.progressKey})`)
      stopProgress()
    }

    try {
      sseListener = new SseProgressListener(props.progressKey, props.baseUrl)
      currentProgressKey = props.progressKey

      sseListener.onProgress((event: MessageEvent, progress: SseProgressData) => {
        try {
          console.log('收到进度更新:', progress)

          // 验证进度数据的有效性
          if (!progress || typeof progress !== 'object') {
            console.warn('收到无效的进度数据:', progress)
            return
          }

          const prevProgressData = { ...progressData.value }

          // 更新进度数据，确保数值范围有效
          const newProgressData = {
            rate: typeof progress.rate === 'number' ? Math.max(0, Math.min(100, progress.rate)) : prevProgressData.rate,
            current: typeof progress.current === 'number' ? Math.max(0, progress.current) : prevProgressData.current,
            total: typeof progress.total === 'number' ? Math.max(0, progress.total) : prevProgressData.total,
            tips: typeof progress.tips === 'string' ? progress.tips : prevProgressData.tips
          }

          progressData.value = newProgressData

          // 记录进度提示消息变化
          if (newProgressData.tips && newProgressData.tips !== prevProgressData.tips && newProgressData.tips.trim()) {
            addMessage(newProgressData.tips, 'info')
          }

          // 记录重要的进度变化（避免频繁的小幅度变化消息）
          const rateDiff = Math.abs((newProgressData.rate || 0) - (prevProgressData.rate || 0))
          if (rateDiff >= 5 || (newProgressData.rate === 100 && prevProgressData.rate !== 100)) {
            const progressMsg = `进度更新: ${newProgressData.rate || 0}% (${newProgressData.current || 0}/${newProgressData.total || 0})`
            addMessage(progressMsg, 'info')
          }

          // 记录关键节点
          if (newProgressData.rate === 100 && prevProgressData.rate !== 100) {
            addMessage('进度已完成 100%', 'success')
          }
        } catch (error) {
          console.error('处理进度更新时发生错误:', error)
          addMessage('进度更新处理异常', 'warning')
        }
      })

      sseListener.onComplete((event: MessageEvent, result) => {
        console.log('任务完成:', result)
        // 更新进度到 100%
        progressData.value = {
          ...progressData.value,
          rate: 100
        }
        // 记录 complete 消息的 tips
        if (result?.tips) {
          addMessage(result.tips, 'success')
        }
        emit('complete', result)
      })

      sseListener.onError((event: MessageEvent, error) => {
        console.error('任务错误:', error)
        const errorMsg = error.message || error.error || '处理失败'
        addMessage(`处理错误: ${errorMsg}`, 'error')
        emit('error', error)
      })

      await sseListener.start()
      console.log('SSE 连接已启动')
    } catch (error) {
      console.error('启动进度监听失败:', error)
      addMessage('连接服务器失败', 'error')
    }
  }

  /**
   * 停止进度监听
   */
  function stopProgress() {
    if (sseListener) {
      sseListener.stop()
      sseListener = null
    }
    currentProgressKey = ''
  }

  /**
   * 检测连接是否存在，不存在则重新连接
   */
  async function ensureConnection() {
    if (!props.progressKey) {
      console.warn('progressKey 为空，无法建立连接')
      return
    }

    // 检测连接是否存在且有效
    if (sseListener && currentProgressKey === props.progressKey && sseListener.isConnected()) {
      console.log(`连接已存在且有效 (${props.progressKey})，无需重新连接`)
      return
    }

    // 连接不存在或已断开，重新连接
    console.log(`连接不存在或已断开，开始建立连接 (${props.progressKey})`)
    await startProgress()
  }

  /**
   * 检查连接状态
   */
  function isConnected(): boolean {
    return !!(sseListener && sseListener.isConnected())
  }

  /**
   * 添加消息到列表
   */
  function addMessage(content: string, type: MessageItem['type'] = 'info') {
    // 过滤掉连接相关的逻辑消息
    if (content === '连接成功，等待任务开始...' || content.toLowerCase().includes('connected') || type === 'connected') {
      return
    }

    const message: MessageItem = {
      content,
      type,
      timestamp: Date.now()
    }
    messageList.value.push(message)

    // 保持最多100条消息
    if (messageList.value.length > 100) {
      messageList.value.shift()
    }
  }

  /**
   * 显示全屏消息
   */
  function showFullScreenMessages() {
    fullScreenVisible.value = true
    nextTick(() => {
      scrollToBottom()
    })
  }

  /**
   * 滚动到顶部
   */
  function scrollToTop() {
    if (fullScreenMessageRef.value) {
      fullScreenMessageRef.value.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  /**
   * 滚动到底部
   */
  function scrollToBottom() {
    if (fullScreenMessageRef.value) {
      fullScreenMessageRef.value.scrollTo({
        top: fullScreenMessageRef.value.scrollHeight,
        behavior: 'smooth'
      })
    }
  }

  /**
   * 清空消息
   */
  function clearMessages() {
    messageList.value = []
  }

  /**
   * 清空数据（进度+消息），但不断开连接
   */
  function clearData() {
    progressData.value = {}
    messageList.value = []
    fullScreenVisible.value = false
  }

  /**
   * 清空所有数据（进度+消息+停止连接）
   */
  function clearAll() {
    stopProgress()
    clearData()
  }

  /**
   * 格式化时间
   */
  function formatTime(timestamp: number): string {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }

  /**
   * 获取消息类型文本
   */
  function getMessageTypeText(type: MessageItem['type']): string {
    const typeMap = {
      info: '信息',
      success: '成功',
      warning: '警告',
      error: '错误',
      connected: '连接'
    }
    return typeMap[type] || '未知'
  }

  // 暴露方法
  defineExpose({
    startProgress,
    stopProgress,
    ensureConnection,
    isConnected,
    addMessage,
    clearMessages,
    clearData,
    clearAll
  })
</script>

<style scoped lang="scss">
  .sse-progress-container {
    .progress-bar {
      margin-bottom: 16px;
    }

    // 消息显示区域样式
    .message-display {
      .message-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .message-title {
          font-size: 14px;
          font-weight: 500;
          color: #606266;
        }
      }

      .latest-message-container {
        .latest-message {
          background: #f8f9fa;
          border: 1px solid #e9ecef;
          border-radius: 6px;
          padding: 8px 10px;
          font-size: 12px;
          line-height: 1.4;

          .message-meta {
            margin-bottom: 4px;

            .message-time-type {
              color: #6c757d;
              font-size: 11px;
              font-weight: 500;

              .type-info {
                color: #0d6efd;
              }
              .type-success {
                color: #198754;
              }
              .type-warning {
                color: #fd7e14;
              }
              .type-error {
                color: #dc3545;
              }
            }
          }

          .message-content {
            color: #212529;
            font-size: 13px;
            line-height: 1.5;
            word-break: break-all;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
          }
        }
      }
    }
  }

  // 全屏对话框样式
  .fullscreen-dialog {
    .fullscreen-message-container {
      max-height: 80vh;
      overflow-y: auto;
      padding: 12px 8px;
      line-height: 1.6;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

      .message-item {
        display: flex;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        gap: 16px;
        font-size: 14px;

        &:last-child {
          border-bottom: none;
        }

        .message-time {
          flex-shrink: 0;
          width: 80px;
          color: #95a5a6;
          font-size: 12px;
          text-align: right;
          padding-top: 2px;
        }

        .message-content-item {
          flex: 1;
          color: #2c3e50;
          line-height: 1.7;
          word-break: break-word;
        }

        &.type-error .message-content-item {
          color: #e74c3c;
        }
        &.type-success .message-content-item {
          color: #27ae60;
        }
        &.type-warning .message-content-item {
          color: #f39c12;
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding-top: 16px;
      border-top: 1px solid #ecf0f1;
    }
  }
</style>
