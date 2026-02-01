<template>
  <div v-if="visible" class="sse-progress">
    <!-- 进度条 -->
    <el-progress :percentage="progress.rate" :stroke-width="10" :status="status" />

    <!-- 提示信息 -->
    <div v-if="progress.tips" class="tips">{{ progress.tips }}</div>

    <!-- 消息区域 -->
    <div v-if="messages.length" class="message-area">
      <div class="message-header">
        <span>{{ $t('sseProgressBar.msgCount', { count: messages.length }) }}</span>
        <el-button link type="primary" @click="dialogVisible = true">{{ $t('sseProgressBar.viewAll') }}</el-button>
      </div>
      <div class="latest-message" :class="latestMessage?.type">
        <div v-html="latestMessage?.content"></div>
      </div>
    </div>

    <!-- 消息弹窗 -->
    <el-dialog v-model="dialogVisible" :title="$t('sseProgressBar.details')" width="80%" append-to-body>
      <div class="message-list" ref="listRef">
        <div v-for="(msg, i) in messages" :key="i" class="message-item" :class="msg.type">
          <span class="time">{{ formatTime(msg.time) }}</span>
          <span class="content">
            <span v-html="msg.content"></span>
          </span>
        </div>
      </div>
      <template #footer>
        <el-button @click="messages = []" type="warning">{{ $t('sseProgressBar.clear') }}</el-button>
        <el-button @click="dialogVisible = false" type="primary">{{ $t('sseProgressBar.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { sseManager, SseProgressData, SseCompleteData, SseErrorData } from '@/utils/sse'

  const { t, locale } = useI18n()

  interface Props {
    progressKey: string
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{
    complete: [data: SseCompleteData]
    error: [data: SseErrorData]
  }>()

  // 状态
  const visible = ref(false)
  const dialogVisible = ref(false)
  const listRef = ref<HTMLElement>()
  const progress = ref({ rate: 0, tips: '' })
  const messages = ref<{ content: string; type: string; time: number }[]>([])
  let currentKey = ''

  // 计算属性
  const status = computed(() => (progress.value.rate >= 100 ? 'success' : undefined))
  const latestMessage = computed(() => messages.value.at(-1))

  // 组件挂载时确保 SSE 已连接
  onMounted(async () => {
    try {
      await sseManager.ensureConnected()
      console.log(t('sseProgressBar.connected'))
    } catch (e) {
      console.warn(t('sseProgressBar.connectFailed'), e)
    }
  })

  // 开始监听
  const start = async (key: string) => {
    if (currentKey === key) return // 避免重复订阅

    visible.value = true
    progress.value = { rate: 0, tips: '' }
    currentKey = key

    await sseManager.subscribe(key, {
      onProgress: (data: SseProgressData) => {
        const newRate = Math.min(100, Math.max(0, data.rate ?? progress.value.rate))
        const newTips = data.tips ?? progress.value.tips

        // 记录 tips 变化
        if (newTips && newTips !== progress.value.tips) {
          addMessage(newTips, 'info')
        }

        progress.value = { rate: newRate, tips: newTips }
      },

      onComplete: (data: SseCompleteData) => {
        progress.value.rate = 100
        if (data.tips) addMessage(data.tips, 'success')
        emit('complete', data)
      },

      onError: (data: SseErrorData) => {
        addMessage(data.message || t('sseProgressBar.failed'), 'error')
        emit('error', data)
      }
    })
  }

  // 停止监听
  const stop = () => {
    if (currentKey) {
      sseManager.unsubscribe(currentKey)
      currentKey = ''
    }
  }

  // 添加消息
  const addMessage = (content: string, type: string) => {
    messages.value.push({ content, type, time: Date.now() })
    if (messages.value.length > 100) messages.value.shift()

    // 滚动到底部
    if (dialogVisible.value) {
      nextTick(() => listRef.value?.scrollTo({ top: listRef.value.scrollHeight, behavior: 'smooth' }))
    }
  }

  // 格式化时间
  const formatTime = (ts: number) => {
    const language = locale.value === 'zh-cn' ? 'zh-CN' : 'en-US'
    return new Date(ts).toLocaleTimeString(language)
  }

  // 重置
  const reset = () => {
    stop()
    visible.value = false
    progress.value = { rate: 0, tips: '' }
    messages.value = []
  }

  // 监听 progressKey 变化（必须在 start/stop 定义之后）
  watch(
    () => props.progressKey,
    async (key, oldKey) => {
      if (oldKey) stop()
      if (key) await start(key)
    },
    { immediate: true }
  )

  onUnmounted(() => stop())

  defineExpose({ start, stop, reset, addMessage })
</script>

<style scoped lang="scss">
  .sse-progress {
    .tips {
      margin-top: 8px;
      font-size: 13px;
      color: #606266;
    }

    .message-area {
      margin-top: 12px;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 6px;

      .message-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #909399;
        margin-bottom: 6px;
      }

      .latest-message {
        font-size: 13px;
        color: #303133;
        line-height: 1.5;

        &.success {
          color: #67c23a;
        }
        &.error {
          color: #f56c6c;
        }
        &.warning {
          color: #e6a23c;
        }
      }
    }
  }

  .message-list {
    max-height: 60vh;
    overflow-y: auto;

    .message-item {
      padding: 8px 0;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      gap: 12px;

      .time {
        flex-shrink: 0;
        font-size: 12px;
        color: #909399;
      }

      .content {
        flex: 1;
        font-size: 13px;
        color: #303133;
      }

      &.success .content {
        color: #67c23a;
      }
      &.error .content {
        color: #f56c6c;
      }
      &.warning .content {
        color: #e6a23c;
      }
    }
  }
</style>
