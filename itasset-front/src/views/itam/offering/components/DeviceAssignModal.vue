<template>
  <el-dialog title="分配设备" v-model="visible" width="500px" append-to-body @close="reset">
    <el-form label-width="80px">
      <el-form-item label="选择设备">
        <DeviceSelect v-model="deviceIds" multiple class="w-full" />
      </el-form-item>
      <el-form-item label="分配备注">
        <el-input v-model="note" type="textarea" :rows="3" placeholder="请输入分配备注信息" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="!deviceIds.length" @click="handleSubmit">
          确认分配 (已选 {{ deviceIds.length }} 个设备)
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import { batchAllocateOffering } from '@/api/itam/offering'
  import { MessageUtil } from '@/utils/messageUtil'
  import { OfferingVO } from '@/api/itam/offering'
  import { AssetType } from '@/api/itam/enums'
  import DeviceSelect from '@/views/itam/components/device-select/index.vue'

  const visible = ref(false)
  const submitLoading = ref(false)
  const currentOffering = ref<OfferingVO | null>(null)
  const deviceIds = ref<number[]>([])
  const note = ref('')

  const reset = () => {
    deviceIds.value = []
    note.value = ''
    currentOffering.value = null
  }

  const open = (offering: OfferingVO) => {
    reset()
    currentOffering.value = offering
    visible.value = true
  }

  const handleSubmit = async () => {
    if (!currentOffering.value) return

    submitLoading.value = true
    try {
      await batchAllocateOffering({
        itemType: AssetType.SERVICE,
        itemId: currentOffering.value.id,
        ownerType: 'asset',
        ownerIds: deviceIds.value,
        note: note.value
      })
      MessageUtil.success('分配成功')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error('分配失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const emit = defineEmits(['success'])

  defineExpose({
    open
  })
</script>

<style scoped lang="scss">
  .w-full {
    width: 100%;
  }
</style>
