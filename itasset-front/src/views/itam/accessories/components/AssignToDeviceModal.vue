<template>
  <el-dialog title="分配到设备" v-model="visible" width="500px" append-to-body @close="reset">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="设备" prop="deviceId">
        <DeviceSelect v-model="form.deviceId" />
        <div>
          <ElSpace>
            <ElIcon>
              <QuestionFilled />
            </ElIcon>
            <span class="text-gray-500 text-size-13px">选择新设备后，将会自动解除此配件与老设备的归属关系。</span>
          </ElSpace>
        </div>
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number
          v-model="form.quantity"
          :min="1"
          :max="maxQuantity"
          controls-position="right"
          class="!w-full"
          placeholder="请输入数量"
        />
      </el-form-item>
      <el-form-item label="备注" prop="note">
        <el-input v-model="form.note" type="textarea" :rows="3" placeholder="请输入备注信息" />
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
  import { ref, useTemplateRef, computed } from 'vue'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import { allocateAccessory, AccessoriesEntity } from '@/api/itam/accessories'
  import { MessageUtil } from '@/utils/messageUtil'
  import { resetFormRef } from '@/utils/business'
  import DeviceSelect from '@/views/itam/components/devcie-select/index.vue'

  const visible = ref(false)
  const loading = ref(false)
  const formRef = useTemplateRef('formRef')
  const currentAccessory = ref<AccessoriesEntity | null>(null)

  const form = ref({
    deviceId: undefined as number | undefined,
    quantity: 1,
    note: ''
  })

  // 最大可分配数量（当前配件的库存数量）
  const maxQuantity = computed(() => {
    return currentAccessory.value?.quantity ? Number(currentAccessory.value.quantity) : 999
  })

  const rules = {
    deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
    quantity: [
      { required: true, message: '请输入数量', trigger: 'blur' },
      {
        validator: (rule: any, value: number, callback: any) => {
          if (value < 1) {
            callback(new Error('数量至少为1'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }

  const emit = defineEmits(['success'])

  const open = (row: AccessoriesEntity) => {
    reset()
    currentAccessory.value = row
    visible.value = true
  }

  const reset = () => {
    form.value = {
      deviceId: undefined,
      quantity: 1,
      note: ''
    }
    currentAccessory.value = null
    resetFormRef(formRef)
  }

  const submit = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid && currentAccessory.value) {
        loading.value = true
        allocateAccessory({
          itemId: currentAccessory.value.id,
          ownerId: form.value.deviceId!,
          ownerType: 'asset',
          quantity: form.value.quantity,
          note: form.value.note || undefined
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
