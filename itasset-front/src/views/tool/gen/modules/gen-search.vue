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
  import { computed, ref } from 'vue'
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

  // 表单配置
  const formItems = computed(() => [
    {
      label: t('system.tool.gen.tableName'),
      key: 'tableName',
      type: 'input',
      props: {
        placeholder: t('system.tool.gen.enterTableName'),
        clearable: true
      }
    },
    {
      label: t('system.tool.gen.tableComment'),
      key: 'tableComment',
      type: 'input',
      props: {
        placeholder: t('system.tool.gen.enterTableComment'),
        clearable: true
      }
    },
    {
      label: t('system.tool.gen.createTime'),
      key: 'dateRange',
      type: 'daterange',
      props: {
        startPlaceholder: t('system.tool.gen.startDate'),
        endPlaceholder: t('system.tool.gen.endDate'),
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
