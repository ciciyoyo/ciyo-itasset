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

  // 任务组选项
  const jobGroupOptions = ref<{ label: string; value: string }[]>([])
  // 任务状态选项
  const jobStatusOptions = ref<{ label: string; value: string }[]>([])

  // 获取字典数据
  onMounted(async () => {
    try {
      const groupResponse = await getDicts('sys_job_group')
      jobGroupOptions.value = (groupResponse || []).map((item: any) => ({
        label: item.dictLabel,
        value: item.dictValue
      }))

      const statusResponse = await getDicts('sys_job_status')
      jobStatusOptions.value = (statusResponse || []).map((item: any) => ({
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
      label: t('system.tool.task.taskName'),
      key: 'jobName',
      type: 'input',
      props: {
        placeholder: t('system.tool.task.enterTaskName'),
        clearable: true
      }
    },
    {
      label: t('system.tool.task.taskGroupName'),
      key: 'jobGroup',
      type: 'select',
      props: {
        placeholder: t('system.tool.task.selectTaskGroupName'),
        clearable: true,
        options: jobGroupOptions.value
      }
    },
    {
      label: t('system.tool.task.taskStatus'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('system.tool.task.selectTaskStatus'),
        clearable: true,
        options: jobStatusOptions.value
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
