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
    <DictDataPage :dict-id="dictId" />

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">{{ t('common.cancel') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { getType } from '@/api/system/dict/type'
  import { useI18n } from 'vue-i18n'
  import DictDataPage from '../data.vue'

  defineOptions({ name: 'DictDataDialog' })

  const props = defineProps<{
    modelValue: boolean
    dictId?: string | number
    dictType?: string
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

  const dialogTitle = ref('')

  // 监听弹窗打开，获取字典类型名称
  watch(
    () => props.modelValue,
    async (newVal) => {
      if (newVal && props.dictId) {
        try {
          const response = await getType(props.dictId.toString())
          dialogTitle.value = `${t('system.dictionaryData.dictionaryData')} - ${response.dictName}`
        } catch (error) {
          console.error('获取字典类型失败:', error)
          dialogTitle.value = t('system.dictionaryData.dictionaryData')
        }
      }
    },
    { immediate: true }
  )

  const handleClose = () => {
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
