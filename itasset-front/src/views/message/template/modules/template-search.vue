<template>
  <ElCard shadow="never" class="art-card-xs">
    <ElForm :model="modelValue" :inline="true">
      <ElFormItem :label="$t('system.noticeTemplate.templateTitle')" prop="templateName">
        <ElInput
          v-model="modelValue.templateName"
          :placeholder="$t('system.noticeTemplate.enterTemplateTitle')"
          clearable
          @keyup.enter="handleSearch"
        />
      </ElFormItem>
      <ElFormItem label="Code" prop="templateCode">
        <ElInput
          v-model="modelValue.templateCode"
          :placeholder="$t('system.noticeTemplate.enterTemplateCode')"
          clearable
          @keyup.enter="handleSearch"
        />
      </ElFormItem>
      <ElFormItem :label="$t('system.noticeTemplate.templateType')" prop="templateType">
        <ElSelect
          v-model="modelValue.templateType"
          :placeholder="$t('system.noticeTemplate.chooseTemplateType')"
          clearable
          style="width: 200px"
        >
          <ElOption v-for="item in msgTypeList" :label="item.label" :value="item.value" :key="item.value" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElButton type="primary" icon="ele-Search" @click="handleSearch" v-ripple>
          {{ $t('common.search') }}
        </ElButton>
        <ElButton icon="ele-Refresh" @click="handleReset" v-ripple>
          {{ $t('common.reset') }}
        </ElButton>
      </ElFormItem>
    </ElForm>
  </ElCard>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'TemplateSearch' })

  const { t } = useI18n()

  interface SearchForm {
    templateName?: string
    templateCode?: string
    templateType?: number
  }

  interface Props {
    modelValue: SearchForm
    msgTypeList: Array<{ label: string; value: number }>
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{
    (e: 'update:modelValue', value: SearchForm): void
    (e: 'search', value: SearchForm): void
    (e: 'reset'): void
  }>()

  const handleSearch = () => {
    emit('search', props.modelValue)
  }

  const handleReset = () => {
    emit('reset')
  }
</script>
