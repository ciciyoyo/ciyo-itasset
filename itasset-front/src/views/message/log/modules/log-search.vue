<template>
  <ElCard shadow="never" class="art-card-xs">
    <ElForm :model="modelValue" :inline="true">
      <ElFormItem :label="$t('system.noticeLog.messageTitle')" prop="title">
        <ElInput
          v-model="modelValue.title"
          :placeholder="$t('system.noticeLog.enterMessageTitle')"
          clearable
          @keyup.enter="handleSearch"
          style="width: 250px"
        />
      </ElFormItem>
      <ElFormItem :label="$t('system.noticeLog.receiver')" prop="receiver">
        <ElInput
          v-model="modelValue.receiver"
          :placeholder="$t('system.noticeLog.enterReceiver')"
          clearable
          @keyup.enter="handleSearch"
          style="width: 250px"
        />
      </ElFormItem>
      <ElFormItem :label="$t('system.noticeLog.pushStatus')" prop="sendStatus">
        <ElSelect
          v-model="modelValue.sendStatus"
          :placeholder="$t('system.noticeLog.selectPushStatus')"
          clearable
          style="width: 250px"
        >
          <ElOption :label="$t('system.noticeLog.notPushed')" value="NOT" />
          <ElOption :label="$t('system.noticeLog.success')" value="SUCCESS" />
          <ElOption :label="$t('system.noticeLog.failure')" value="FAIL" />
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

  defineOptions({ name: 'LogSearch' })

  const { t } = useI18n()

  interface SearchForm {
    title?: string
    receiver?: string
    sendStatus?: string
  }

  interface Props {
    modelValue: SearchForm
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
