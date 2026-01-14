<template>
  <ElDialog
    :title="dialogTitle"
    v-model="visible"
    width="90%"
    top="5vh"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
    destroy-on-close
  >
    <JobLogPage :job-id="jobId" :job-name="jobName" :job-group="jobGroup" />

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">{{ t('common.cancel') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import JobLogPage from '../log.vue'

  defineOptions({ name: 'JobLogDialog' })

  const props = defineProps<{
    modelValue: boolean
    jobId?: string | number
    jobName?: string
    jobGroup?: string
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    close: []
  }>()

  const { t } = useI18n()

  // 控制弹窗显示
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const dialogTitle = computed(() => {
    if (props.jobName) {
      return `${t('system.tool.task.scheduleLog')} - ${props.jobName}`
    }
    return t('system.tool.task.scheduleLog')
  })

  const handleClose = () => {
    emit('update:modelValue', false)
    emit('close')
  }
</script>

<style scoped lang="scss">
  :deep(.el-dialog__body) {
    padding: 0 20px 20px;
    height: 70vh;
    overflow: hidden;
  }
</style>
