<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { getDicts } from '@/api/system/dict/data'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  interface Props {
    modelValue: Record<string, any>
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {}

  // 操作类型选项
  const typeOptions = ref<{ label: string; value: string }[]>([])

  // 状态选项
  const statusOptions = ref<{ label: string; value: string }[]>([])

  // 获取字典数据
  onMounted(async () => {
    try {
      const typeResponse = await getDicts('sys_oper_type')
      typeOptions.value = (typeResponse || []).map((item: any) => ({
        label: item.dictLabel,
        value: item.dictValue
      }))

      const statusResponse = await getDicts('sys_common_status')
      statusOptions.value = (statusResponse || []).map((item: any) => ({
        label: item.dictLabel,
        value: item.dictValue
      }))
    } catch (error) {
      console.error('获取字典数据失败:', error)
    }
  })

  // 表单配置
  const formItems = computed(() => [
    {
      label: t('system.operlog.systemModule'),
      key: 'title',
      type: 'input',
      props: {
        placeholder: t('system.operlog.systemModule'),
        clearable: true
      }
    },
    {
      label: t('system.operlog.operator'),
      key: 'operName',
      type: 'input',
      props: {
        placeholder: t('system.operlog.operator'),
        clearable: true
      }
    },
    {
      label: t('system.operlog.type'),
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: t('system.operlog.operationType'),
        clearable: true,
        options: typeOptions.value
      }
    },
    {
      label: t('system.operlog.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('system.operlog.operationStatus'),
        clearable: true,
        options: statusOptions.value
      }
    },
    {
      label: t('system.operlog.operationTime'),
      key: 'dateRange',
      type: 'datetime',
      props: {
        type: 'daterange',
        rangeSeparator: '-',
        startPlaceholder: t('system.operlog.startDate'),
        endPlaceholder: t('system.operlog.endDate'),
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  // 事件
  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value?.validate()
    emit('search', formData.value)
  }
</script>
