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
  import { computed, ref, onMounted } from 'vue'
  import { getDicts } from '@/api/system/dict/data'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()

  interface Props {
    modelValue: Record<string, any>
    typeOptions: Array<{ id: number; dictName: string; dictType: string }>
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

  // 表单配置
  const formItems = computed(() => [
    {
      label: t('system.dictionaryData.dictionaryType'),
      key: 'dictType',
      type: 'select',
      props: {
        placeholder: t('system.dictionaryData.dictionaryType'),
        clearable: true,
        options: props.typeOptions.map((item) => ({
          label: item.dictName,
          value: item.dictType
        }))
      }
    },
    {
      label: t('system.dictionaryData.dictionaryLabel'),
      key: 'dictLabel',
      type: 'input',
      props: {
        placeholder: t('system.dictionaryData.enterDictionaryLabel'),
        clearable: true
      }
    },
    {
      label: t('system.dictionaryData.status'),
      key: 'status',
      type: 'select',
      props: {
        placeholder: t('system.dictionaryData.dataStatus'),
        clearable: true,
        options: statusOptions.value
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
