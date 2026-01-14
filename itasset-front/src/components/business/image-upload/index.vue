<template>
  <div class="component-image-upload">
    <el-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :action="uploadUrl"
      :headers="headers"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-remove="handleRemove"
      :on-preview="handlePictureCardPreview"
      :limit="limit"
      :class="{ hide: fileList.length >= limit }"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    <div class="el-upload__tip" v-if="isShowTip">
      {{ $t('components.imageUpload.pleaseUpload') }}
      <template v-if="fileSize">
        {{ $t('components.imageUpload.sizeNotExceed') }} <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        {{ $t('components.imageUpload.formatIs') }} <b style="color: #f56c6c">{{ fileType.join('/') }}</b>
      </template>
      {{ $t('components.imageUpload.file') }}
    </div>
    <el-dialog v-model="dialogVisible" :title="$t('components.imageUpload.preview')" width="800" append-to-body>
      <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'

  // Define props
  const props = defineProps({
    value: {
      type: [String, Array],
      default: ''
    },
    limit: {
      type: Number,
      default: 1
    },
    fileSize: {
      type: Number,
      default: 5
    },
    fileType: {
      type: Array,
      default: () => ['png', 'jpg', 'jpeg']
    },
    isShowTip: {
      type: Boolean,
      default: true
    }
  })

  // Define emits
  const emit = defineEmits(['update:value'])

  const { t } = useI18n()

  // State
  const dialogImageUrl = ref('')
  const dialogVisible = ref(false)
  const fileList = ref<Array<any>>([])
  const uploadUrl = ref(import.meta.env.VITE_API_URL + '/storage/file/upload')
  const userStore = useUserStore()

  // Headers for upload request
  const headers = computed(() => {
    return {
      Authorization: userStore.accessToken
    }
  })

  // Watch 'value' to sync with 'fileList'
  watch(
    () => props.value,
    (val) => {
      if (val) {
        if (typeof val === 'string') {
          fileList.value = [{ name: val, url: val }]
        } else if (Array.isArray(val)) {
          fileList.value = val.map((item) => {
            if (typeof item === 'string') {
              return { name: item, url: item }
            }
            return item
          })
        }
      } else {
        fileList.value = []
      }
    },
    { deep: true, immediate: true }
  )

  /**
   * Update the model value based on current file list
   */
  function updateModelValue() {
    let newValue
    if (props.limit === 1) {
      // Single file mode
      newValue = fileList.value.length > 0 ? fileList.value[0].url : ''
    } else {
      // Multiple file mode
      newValue = fileList.value.map((file) => file.url)
    }
    emit('update:value', newValue)
  }

  /**
   * Before upload hook
   */
  function handleBeforeUpload(file: any) {
    const isTypeOk = props.fileType.includes(file.type.replace('image/', ''))
    const isSizeOk = file.size / 1024 / 1024 < props.fileSize

    if (props.fileType.length && !isTypeOk) {
      ElMessage.error(t('components.imageUpload.formatError', { format: props.fileType.join('/') }))
      return false
    }
    if (!isSizeOk) {
      ElMessage.error(t('components.imageUpload.sizeError', { size: props.fileSize }))
      return false
    }
    return true
  }

  /**
   * Upload success hook
   */
  function handleUploadSuccess(res: any, file: any) {
    if (res.code === 200) {
      // Assume response data contains the url. Adjust based on actual API response.
      // Usually res.data is the url or an object with url.
      // If res.data is string:
      const url = res.data?.fileUrl || res.data
      file.url = url

      // In multiple mode, fileList is automatically updated by Element,
      // but the file object in list might not have 'url' set correctly from response yet?
      // Actually handleUploadSuccess(response, file, fileList)
      // We already bound v-model:file-list, so we just need to make sure the file in the list has the correct URL.

      // Note: 'file' arg is the file object element-plus created.
      // We need to ensure updateModelValue uses the correct url.

      updateModelValue()
      ElMessage.success(t('components.imageUpload.uploadSuccess'))
    } else {
      ElMessage.error(res.msg || t('components.imageUpload.uploadFailed'))
      // Remove the failed file from list
      const index = fileList.value.indexOf(file)
      if (index !== -1) {
        fileList.value.splice(index, 1)
      }
    }
  }

  /**
   * Upload error hook
   */
  function handleUploadError() {
    ElMessage.error(t('components.imageUpload.uploadFailedRetry'))
  }

  /**
   * Remove file hook
   */
  function handleRemove(file: any, fileListProp: any) {
    // fileList is already updated via v-model, but safe to use the prop passed
    // fileList.value = fileListProp; // not needed due to v-model
    updateModelValue()
  }

  /**
   * Preview file hook
   */
  function handlePictureCardPreview(file: any) {
    dialogImageUrl.value = file.url!
    dialogVisible.value = true
  }
</script>

<style scoped>
  /* Hide the add button when limit is reached */
  :deep(.hide .el-upload--picture-card) {
    display: none;
  }
</style>
