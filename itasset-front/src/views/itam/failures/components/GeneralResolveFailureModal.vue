<template>
  <el-dialog title="解决故障" v-model="visible" width="500px" append-to-body @close="reset">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="解决时间" prop="resolvedDate">
        <el-date-picker
          v-model="form.resolvedDate"
          type="datetime"
          placeholder="选择解决时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w100"
        />
      </el-form-item>
      <el-form-item label="解决方案" prop="notes">
        <el-input v-model="form.notes" type="textarea" show-word-limit maxlength="500" placeholder="请输入解决方案"> </el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button :loading="loading" type="primary" @click="submit"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, useTemplateRef } from 'vue'
  import dayjs from 'dayjs'
  import { FailuresVO, resolveFailures } from '@/api/itam/failures'
  import { FailureStatus } from '@/api/itam/enums'
  import { MessageUtil } from '@/utils/messageUtil'
  import { resetFormRef } from '@/utils/business'

  const visible = ref(false)
  const loading = ref(false)
  const formRef = useTemplateRef('formRef')
  const form = ref<{
    id?: number
    resolvedDate: string
    notes: string
    status: FailureStatus
  }>({
    resolvedDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    notes: '',
    status: FailureStatus.RESOLVED
  })

  const rules = {
    resolvedDate: [{ required: true, message: '请选择解决时间', trigger: 'change' }]
  }

  const emit = defineEmits(['success'])

  const open = (row: FailuresVO) => {
    reset()
    form.value.id = row.id
    visible.value = true
  }

  const reset = () => {
    form.value = {
      id: undefined,
      resolvedDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      notes: '',
      status: FailureStatus.RESOLVED
    }
    resetFormRef(formRef)
  }

  const submit = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        loading.value = true
        resolveFailures({
          id: form.value.id!,
          resolvedDate: form.value.resolvedDate,
          notes: form.value.notes,
          status: FailureStatus.RESOLVED
        } as any)
          .then(() => {
            MessageUtil.success('故障已解决')
            visible.value = false
            emit('success')
          })
          .catch((e: any) => console.error(e))
          .finally(() => {
            loading.value = false
          })
      }
    })
  }

  defineExpose({
    open
  })
</script>

<style scoped lang="scss">
  .w100 {
    width: 100%;
  }
</style>
