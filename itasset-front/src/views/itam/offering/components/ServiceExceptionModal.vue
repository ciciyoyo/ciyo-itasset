<template>
  <el-dialog title="报告异常" v-model="visible" width="500px" append-to-body @close="reset">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="故障说明" prop="failureName">
        <el-input v-model="form.failureName" show-word-limit clearable maxlength="120" placeholder="输入故障说明"> </el-input>
      </el-form-item>
      <el-form-item label="故障时间" prop="failureDate">
        <el-date-picker
          v-model="form.failureDate"
          type="datetime"
          placeholder="输入服务异常出现时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="w100"
        />
      </el-form-item>
      <el-form-item label="故障描述" prop="failureDescription">
        <el-input v-model="form.failureDescription" type="textarea" show-word-limit clearable placeholder="输入补充故障描述">
        </el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button :loading="loading" type="primary" @click="submit"> 提交 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, useTemplateRef } from 'vue'
  import dayjs from 'dayjs'
  import { AssetType } from '@/api/itam/enums'
  import { FailuresEntity } from '@/api/itam/failures'
  import { OfferingVO, reportException } from '@/api/itam/offering'
  import { MessageUtil } from '@/utils/messageUtil'
  import { resetFormRef } from '@/utils/business'

  const visible = ref(false)
  const loading = ref(false)
  const formRef = useTemplateRef('formRef')

  const form = ref<FailuresEntity>({
    targetType: AssetType.SERVICE,
    targetId: 0,
    failureName: '',
    failureDescription: '',
    failureDate: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })

  const rules = {
    failureName: [{ required: true, message: '请输入故障说明', trigger: 'blur' }],
    failureDate: [{ required: true, message: '请选择故障出现时间', trigger: 'change' }]
  }

  const emit = defineEmits(['success'])

  const open = (row: OfferingVO) => {
    reset()
    form.value.targetId = row.id || 0
    visible.value = true
  }

  const reset = () => {
    form.value = {
      targetType: AssetType.SERVICE,
      targetId: form.value.targetId,
      failureName: '',
      failureDescription: '',
      failureDate: dayjs().format('YYYY-MM-DD HH:mm:ss')
    }
    resetFormRef(formRef)
  }

  const submit = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        loading.value = true
        reportException({
          ...form.value
        })
          .then(() => {
            MessageUtil.success('报告异常成功')
            visible.value = false
            emit('success')
          })
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
