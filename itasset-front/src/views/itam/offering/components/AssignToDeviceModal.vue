<template>
  <el-dialog title="分配到设备" v-model="visible" width="500px" append-to-body @close="reset">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="设备" prop="deviceId">
        <DeviceSelect v-model="form.deviceId" />
        <div>
          <ElSpace>
            <ElIcon><QuestionFilled /></ElIcon>
            <span class="text-gray-500 text-size-13px">选择新设备后，将会自动解除此服务程序与老设备的归属关系。</span>
          </ElSpace>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer" style="display: flex; justify-content: flex-end">
        <el-button :loading="loading" @click="visible = false"> 取消 </el-button>
        <el-button :loading="loading" type="primary" @click="submit"> 提交 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, useTemplateRef } from 'vue'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import { bindAsset, OfferingVO } from '@/api/itam/offering'
  import { MessageUtil } from '@/utils/messageUtil'
  import { resetFormRef } from '@/utils/business'
  import DeviceSelect from '@/views/itam/components/devcie-select/index.vue'

  const visible = ref(false)
  const loading = ref(false)
  const formRef = useTemplateRef('formRef')
  const currentOffering = ref<OfferingVO | null>(null)

  const form = ref({
    deviceId: undefined as number | undefined
  })

  const rules = {
    deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }]
  }

  const emit = defineEmits(['success'])

  const open = (row: OfferingVO) => {
    reset()
    currentOffering.value = row
    visible.value = true
  }

  const reset = () => {
    form.value = {
      deviceId: undefined
    }
    currentOffering.value = null
    resetFormRef(formRef)
  }

  const submit = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid && currentOffering.value) {
        loading.value = true
        bindAsset({
          id: currentOffering.value.id,
          targetId: form.value.deviceId!
        })
          .then(() => {
            MessageUtil.success('分配成功')
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

<style scoped lang="scss"></style>
