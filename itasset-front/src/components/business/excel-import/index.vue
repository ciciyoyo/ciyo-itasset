<template>
  <div class="excel-import-component">
    <!-- Trigger Slot -->
    <div @click="handleOpen" class="trigger-container">
      <slot name="trigger">
        <el-button type="primary">{{ $t('components.excelImport.buttonText') }}</el-button>
      </slot>
    </div>

    <!-- Import Dialog -->
    <el-dialog v-model="visible" :title="dialogTitle" width="600px" append-to-body @close="handleClose">
      <div class="import-content">
        <!-- Upload Area -->
        <el-upload
          ref="uploadRef"
          drag
          :action="actionUrl"
          :headers="headers"
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
              {{ $t('components.excelImport.tip', { size: fileSize }) }}
            </div>
          </template>
        </el-upload>

        <!-- Description Slot -->
        <div class="import-description">
          <slot name="description"></slot>
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
  import { UploadFilled } from '@element-plus/icons-vue'
  import type { UploadInstance, UploadProps, UploadRawFile, UploadUserFile } from 'element-plus'
  import { ElMessage, genFileId } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'

  const { t } = useI18n()

  const props = defineProps({
    // Upload API URL
    url: {
      type: String,
      required: true
    },
    // Dialog Title
    title: {
      type: String,
      default: ''
    },
    // File size limit (MB)
    fileSize: {
      type: Number,
      default: 10
    }
  })

  // Emits
  const emit = defineEmits(['success', 'error'])

  // Store
  const userStore = useUserStore()

  // State
  const visible = ref(false)
  const uploadRef = ref<UploadInstance>()
  const fileList = ref<UploadUserFile[]>([])
  const uploading = ref(false)

  const dialogTitle = computed(() => {
    return props.title || t('components.excelImport.title')
  })

  const actionUrl = computed(() => {
    // If url starts with http/https, use it directly
    if (props.url.startsWith('http')) {
      return props.url
    }
    // Otherwise prepend base API URL
    return import.meta.env.VITE_API_URL + props.url
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
    fileList.value = []
    uploading.value = false
  }

  const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
    // Handle file change if needed, e.g. validation
    const fileSuffix = uploadFile.name.substring(uploadFile.name.lastIndexOf('.') + 1)
    const whiteList = ['xls', 'xlsx']
    if (whiteList.indexOf(fileSuffix) === -1) {
      ElMessage.error(t('components.excelImport.formatError'))
      uploadRef.value!.clearFiles()
      return
    }

    // Size check
    if (uploadFile.size && uploadFile.size / 1024 / 1024 > props.fileSize) {
      ElMessage.error(t('components.excelImport.sizeError', { size: props.fileSize }))
      uploadRef.value!.clearFiles()
      return
    }
    fileList.value = uploadFiles
  }

  const handleExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value!.clearFiles()
    const file = files[0] as UploadRawFile
    file.uid = genFileId()
    uploadRef.value!.handleStart(file)
  }

  const submitUpload = () => {
    if (!uploadRef.value) return

    if (fileList.value.length === 0) {
      ElMessage.warning(t('components.excelImport.pleaseSelect'))
      return
    }

    uploading.value = true
    uploadRef.value.submit()
  }

  const handleUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
    uploading.value = false

    // You might need to adjust this depending on your API response structure
    if (response.code === 200) {
      ElMessage.success(response.msg || t('components.excelImport.success'))
      visible.value = false
      emit('success', response)
    } else {
      ElMessage.error(response.msg || t('components.excelImport.fail'))
      emit('error', response)
      // Don't close dialog on error so user can retry
      // visible.value = false;
    }
  }

  const handleUploadError: UploadProps['onError'] = (error) => {
    uploading.value = false
    ElMessage.error(t('components.excelImport.error'))
    console.error(error)
    emit('error', error)
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
</style>
