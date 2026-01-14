<template>
  <el-dialog v-model="dialogVisible" :title="$t('formgen.tinymce.title')" width="40%">
    <Tinymce v-model:value="inputValue" v-bind="$attrs" @change="handleInputValueChange"></Tinymce>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">
          {{ $t('common.cancel') }}
        </el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ $t('common.enter') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script lang="ts" setup>
  import Tinymce from './index.vue'
  import { ref } from 'vue'

  const props = defineProps({
    value: {
      type: String,
      default: ''
    }
  })

  const inputValue = ref(props.value)

  const emit = defineEmits(['update:value'])

  const handleInputValueChange = (val: string) => {
    inputValue.value = val
  }

  const handleSubmit = () => {
    emit('update:value', inputValue.value)
    dialogVisible.value = false
  }
  const dialogVisible = ref(false)

  const showDialog = () => {
    dialogVisible.value = true
  }

  defineExpose({
    showDialog
  })
</script>

<style lang="scss" scoped></style>
