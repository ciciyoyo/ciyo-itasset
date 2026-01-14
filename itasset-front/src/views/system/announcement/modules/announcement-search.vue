<template>
  <ElCard shadow="never" class="art-card-xs">
    <ElForm :model="modelValue" :inline="true">
      <ElFormItem :label="$t('system.announcement.title')" prop="title">
        <ElInput
          v-model="modelValue.title"
          :placeholder="$t('system.announcement.enterTitle')"
          clearable
          @keyup.enter="handleSearch"
          style="width: 250px"
        />
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

  defineOptions({ name: 'AnnouncementSearch' })

  const { t } = useI18n()

  interface SearchForm {
    title?: string | null
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
