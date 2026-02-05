<template>
  <div class="excel-import-component">
    <!-- Trigger Slot -->
    <div @click="handleOpen" class="trigger-container">
      <slot name="trigger">
        <el-button type="success" icon="ele-Upload" v-ripple> 导入 </el-button>
      </slot>
    </div>

    <!-- Import Dialog -->
    <el-dialog v-model="visible" :title="dialogTitle" width="600px" append-to-body @close="handleClose">
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
          :data="uploadData"
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
            @complete="handleProgressComplete"
          />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="visible = false">{{ $t('components.excelImport.cancel') }}</el-button>
          <el-button v-if="!importCompleted" type="primary" @click="submitUpload" :loading="uploading">
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
  import { download } from '@/utils/business'
  import { storeToRefs } from 'pinia'

  const { t } = useI18n()

  type Props = {
    url: string
    templateDownloadFn?: () => Promise<any>
    templateCode?: string
    templateName?: string
    title?: string
    fileSize?: number
    data?: Record<string, any>
  }

  const props = withDefaults(defineProps<Props>(), {
    templateName: 'template',
    title: '',
    fileSize: 5,
    data: () => ({})
  })

  // Emits
  const emit = defineEmits<{
    (e: 'success', payload: any): void
    (e: 'error', payload: any): void
  }>()

  // Store
  const userStore = useUserStore()
  const { info } = storeToRefs(userStore)
  // State
  const visible = ref(false)
  const uploadRef = ref<UploadInstance>()
  const fileList = ref<UploadUserFile[]>([])
  const uploading = ref(false)
  const sseProgressRef = ref()

  // SSE Progress State
  const showProgress = ref(false)
  const pendingUploadResponse = ref<any>(null) // 存储等待进度完成的上传响应
  const importCompleted = ref(false) // 标记导入是否已完成

  // 生成进度key
  const generateProgressKey = () => {
    const userId = info.value.userId || 'anonymous'
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    return `${userId}_${timestamp}_${random}`
  }
  const progressKey = ref(generateProgressKey())

  // 合并progressKey和自定义data
  const uploadData = computed(() => {
    return {
      progressKey: progressKey.value,
      ...props.data
    }
  })

  const resetState = () => {
    fileList.value = []
    uploading.value = false
    pendingUploadResponse.value = null // 清除待处理的响应
    importCompleted.value = false // 重置导入完成状态
    showProgress.value = false // 隐藏进度显示
    uploadRef.value?.clearFiles()
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
    importCompleted.value = false // 选择文件时重置导入完成状态
  }

  const handleExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value?.clearFiles()
    const file = files[0] as UploadRawFile
    file.uid = genFileId()
    uploadRef.value?.handleStart(file)
  }

  const submitUpload = async () => {
    if (!uploadRef.value) return
    if (fileList.value.length === 0) {
      ElMessage.warning(t('components.excelImport.pleaseSelect'))
      return
    }

    // 每次导入生成新的 progressKey
    progressKey.value = generateProgressKey()
    console.log('开始导入，新 progressKey:', progressKey.value)

    uploading.value = true
    showProgress.value = true

    // 上传文件（SSE 已在打开对话框时连接，进度条组件会自动订阅新 key）
    uploadRef.value.submit()
  }

  const handleUploadSuccess: UploadProps['onSuccess'] = (response) => {
    uploading.value = false
    // sseProgressRef.value?.clearData?.()
    console.log('Upload response:', response)
    if (response?.code !== 200) {
      resetState()
      ElMessage.error(response?.msg || t('components.excelImport.fail'))
      emit('error', response)
    } else {
      // 导入成功后清空文件，防止重复上传
      fileList.value = []
      uploadRef.value?.clearFiles()
      // 存储响应，等待进度完成后再触发 success
      pendingUploadResponse.value = response
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

  // 进度完成后触发 success
  const handleProgressComplete = (result: any) => {
    console.log('Progress complete:', result)
    // 进度推送完成后，触发 success 事件
    if (pendingUploadResponse.value) {
      emit('success', pendingUploadResponse.value)
      pendingUploadResponse.value = null
      importCompleted.value = true // 标记导入已完成
    }
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
