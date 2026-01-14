<template>
  <ArtSearchBar ref="searchBarRef" v-model="formData" :items="formItems" @reset="handleReset" @search="handleSearch">
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { getDicts } from '@/api/system/dict/data'
  import { addDateRange } from '@utils/business'
  import { useI18n } from 'vue-i18n'

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
  const { t } = useI18n()

  const searchBarRef = ref()

  // 表单数据双向绑定
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 状态选项
  const statusOptions = ref<{ label: string; value: string }[]>([])

  // 获取状态字典数据
  onMounted(async () => {
    try {
      const response = await getDicts('sys_normal_disable')
      statusOptions.value = (response || []).map((item: any) => ({
        label: item.dictLabel,
        value: item.dictValue
      }))
    } catch (error) {
      console.error('获取状态字典失败:', error)
    }
  })

  // 搜索表单配置项
  const formItems = computed(() => [
    {
      label: t('system.roleManagement.roleName'),
      key: 'roleName',
      type: 'input',
      props: {
        placeholder: t('system.roleManagement.enterRoleName'),
        clearable: true
      }
    },
    {
      label: t('system.roleManagement.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('system.roleManagement.roleStatus'),
        clearable: true,
        options: statusOptions.value
      }
    },
    {
      label: t('system.roleManagement.createTime'),
      key: 'dateRange',
      type: 'datetime',
      props: {
        type: 'daterange',
        rangeSeparator: '-',
        startPlaceholder: t('system.roleManagement.startDate'),
        endPlaceholder: t('system.roleManagement.endDate'),
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  // 处理重置事件
  const handleReset = () => {
    emit('reset')
  }

  // 处理搜索事件
  const handleSearch = async () => {
    await searchBarRef.value?.validate()
    const params = { ...formData.value }
    addDateRange(params, formData.value.dateRange)
    emit('search', params)
  }

  // 暴露方法
  defineExpose({
    reset: () => searchBarRef.value?.reset()
  })
</script>
