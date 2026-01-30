<template>
  <div class="excel-import-component">
    <!-- Trigger Slot -->
    <div @click="handleOpen" class="trigger-container">
      <slot name="trigger">
        <el-button type="primary">{{ $t('components.excelImport.buttonText') }}</el-button>
      </slot>
    </div>

    <!-- Import Dialog -->
    <el-dialog v-model="visible" :title="dialogTitle" :destroy-on-close="true" width="600px" append-to-body @close="handleClose">
      <div class="import-content">
        <!-- Template Download -->
        <div v-if="templateCode || templateDownloadFn" class="template-download">
          <el-button type="success" :icon="Download" @click="downloadTemplate">
            {{ $t('components.excelImport.downloadTemplate') }}
          </el-button>
          <span class="template-tip">{{ $t('components.excelImport.templateTip') }}</span>
        </div>

        <!-- Upload Area -->
        <el-upload
          ref="uploadRef"
          drag
          :action="actionUrl"
          :headers="headers"
          :data="{ progressKey: progressKey }"
          :limit="1"
          :on-exceed="handleExceed"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :auto-upload="false"
          accept=".xlsx, .xls"
          :on-change="handleChange"
          class="upload-demo"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            {{ $t('components.excelImport.dragText') }} <em>{{ $t('components.excelImport.clickUpload') }}</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              {{ $t('components.excelImport.tip', { size: props.fileSize }) }}
            </div>
            <div class="el-upload__tip" v-if="fileList.length"> 已选择文件大小：{{ selectedFileSizeText }} </div>
          </template>
        </el-upload>

        <!-- Description Slot -->
        <div class="import-description">
          <slot name="description"></slot>
        </div>
        <!-- SSE 进度显示 -->
        <div v-show="showProgress" class="progress-section">
          <SseProgressBar
            ref="sseProgressRef"
            :progress-key="progressKey"
            :show-details="true"
            :show-actions="true"
            @close="handleProgressClose"
          />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="visible = false">{{ $t('components.excelImport.cancel') }}</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">
            {{ $t('components.excelImport.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { UploadFilled, Download } from '@element-plus/icons-vue'
  import type { UploadInstance, UploadProps, UploadRawFile, UploadUserFile } from 'element-plus'
  import { ElMessage, genFileId } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'
  import { baseUrl } from '@/utils/auth'
  import SseProgressBar from '@/components/common/sse-progress-bar/index.vue'
  import { importTemplateDownlaod } from '@/api/common'

  const { t } = useI18n()

  type Props = {
    url: string
    templateDownloadFn?: () => Promise<any>
    templateCode?: string
    templateName?: string
    title?: string
    fileSize?: number
  }

  const props = withDefaults(defineProps<Props>(), {
    templateName: 'template',
    title: '',
    fileSize: 5
  })

  // Emits
  const emit = defineEmits<{
    (e: 'success', payload: any): void
    (e: 'error', payload: any): void
  }>()

  // Store
  const userStore = useUserStore()

  // State
  const visible = ref(false)
  const uploadRef = ref<UploadInstance>()
  const fileList = ref<UploadUserFile[]>([])
  const uploading = ref(false)
  const sseProgressRef = ref()

  // SSE Progress State
  const showProgress = ref(false)
  const progressKey = ref('')

  // 生成进度key
  const generateProgressKey = () => {
    const userId = userStore.userId || 'anonymous'
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    return `${userId}_${timestamp}_${random}`
  }

  const resetState = () => {
    fileList.value = []
    uploading.value = false
    // 清理上传组件
    uploadRef.value?.clearFiles()
    // 清空所有进度和日志信息
    sseProgressRef.value?.clearAll?.()
  }

  const dialogTitle = computed(() => {
    return props.title || t('components.excelImport.title')
  })

  const actionUrl = computed(() => {
    return props.url.startsWith('http') ? props.url : baseUrl + props.url
  })

  // Headers
  const headers = computed(() => {
    return {
      Authorization: userStore.accessToken
    }
  })

  // Methods
  const handleOpen = () => {
    // 预先生成progressKey
    progressKey.value = generateProgressKey()
    visible.value = true
  }

  const handleClose = () => {
    resetState()
  }

  const isExcelFile = (name: string) => /\.xlsx?$/.test(name.toLowerCase())
  const exceedsLimit = (size?: number) => (size ?? 0) / 1024 / 1024 > props.fileSize

  // 格式化文件大小为人类可读
  const formatBytes = (size?: number) => {
    const bytes = size ?? 0
    if (!bytes || bytes < 0) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
    const value = bytes / Math.pow(1024, i)
    return `${value.toFixed(value >= 100 ? 0 : 2)} ${units[i]}`
  }

  // 当前选择文件大小显示文本
  const selectedFileSizeText = computed(() => {
    const size = fileList.value[0]?.size
    return formatBytes(size)
  })

  const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
    if (!isExcelFile(uploadFile.name)) {
      ElMessage.error(t('components.excelImport.formatError'))
      uploadRef.value?.clearFiles()
      return
    }
    if (exceedsLimit(uploadFile.size)) {
      ElMessage.error(t('components.excelImport.sizeError', { size: props.fileSize }))
      uploadRef.value?.clearFiles()
      return
    }
    fileList.value = uploadFiles
  }

  const handleExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value?.clearFiles()
    const file = files[0] as UploadRawFile
    file.uid = genFileId()
    uploadRef.value?.handleStart(file)
  }

  const submitUpload = async () => {
    showProgress.value = false
    if (!uploadRef.value) return
    if (fileList.value.length === 0) {
      ElMessage.warning(t('components.excelImport.pleaseSelect'))
      return
    }

    uploading.value = true
    // 选择新文件时清空之前的进度信息（只清数据，不断开连接）
    sseProgressRef.value?.clearData?.()

    // 显示进度组件
    showProgress.value = true

    // 检测连接是否存在，不存在则重新连接
    await sseProgressRef.value?.ensureConnection?.()

    // 上传文件
    uploadRef.value.submit()
  }

  const handleUploadSuccess: UploadProps['onSuccess'] = (response) => {
    uploading.value = false
    console.log('Upload response:', response)
    if (response?.code !== 200) {
      resetState()
      ElMessage.error(response?.msg || t('components.excelImport.fail'))
      emit('error', response)
    } else {
      // 导入成功后清空文件，防止重复上传
      fileList.value = []
      uploadRef.value?.clearFiles()
      emit('success', response)
    }
    showProgress.value = true
  }

  const handleUploadError: UploadProps['onError'] = (error) => {
    uploading.value = false
    resetState()
    console.error(error)
    emit('error', error)
  }

  const handleProgressClose = () => {
    showProgress.value = false
    // 关闭进度条时重置文件选择，允许重新选择文件
    resetState()
  }

  // Download template method
  const downloadTemplate = async () => {
    try {
      let blob: any
      if (props.templateCode) {
        blob = await importTemplateDownlaod(props.templateCode)
      } else if (props.templateDownloadFn) {
        blob = await props.templateDownloadFn()
      } else {
        ElMessage.warning(t('components.excelImport.noTemplateUrl'))
        return
      }
      const { download } = await import('@/utils/business')
      download(blob, props.templateName || 'template')
      ElMessage.success(t('components.excelImport.downloadSuccess'))
    } catch (error) {
      console.error('Template download error:', error)
      ElMessage.error(t('components.excelImport.downloadError'))
    }
  }
</script>

<style scoped>
  .excel-import-component {
    display: inline-block;
  }
  .trigger-container {
    display: inline-block;
  }
  .import-description {
    margin-top: 20px;
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
  }
  .progress-section {
    margin-top: 20px;
    border-top: 1px solid #e4e7ed;
    padding-top: 20px;
  }
  .template-download {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .template-tip {
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
  }
</style>
