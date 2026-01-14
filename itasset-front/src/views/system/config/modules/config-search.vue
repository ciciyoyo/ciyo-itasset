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

  // 系统内置选项
  const typeOptions = ref<{ label: string; value: string }[]>([])

  // 获取字典数据
  onMounted(async () => {
    try {
      const response = await getDicts('sys_yes_no')
      typeOptions.value = (response || []).map((item: any) => ({
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
      label: t('system.parameter.parameterName'),
      key: 'configName',
      type: 'input',
      props: {
        placeholder: t('system.parameter.enterParameterName'),
        clearable: true
      }
    },
    {
      label: t('system.parameter.parameterKey'),
      key: 'configKey',
      type: 'input',
      props: {
        placeholder: t('system.parameter.enterParameterKey'),
        clearable: true
      }
    },
    {
      label: t('system.parameter.systemBuiltIn'),
      key: 'configType',
      type: 'select',
      props: {
        placeholder: t('system.parameter.systemBuiltIn'),
        clearable: true,
        options: typeOptions.value
      }
    },
    {
      label: t('system.parameter.createTime'),
      key: 'dateRange',
      type: 'datetime',
      props: {
        type: 'daterange',
        rangeSeparator: '-',
        startPlaceholder: t('system.parameter.startDate'),
        endPlaceholder: t('system.parameter.endDate'),
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
