<template>
  <el-dialog v-model="visible" title="故障报修" width="500px" destroy-on-close :close-on-click-modal="false" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="py-2">
      <el-form-item label="故障设备" prop="targetId">
        <el-select
          v-model="form.targetId"
          placeholder="请选择故障设备"
          class="w-full"
          :loading="loadingDevices"
          no-data-text="暂无关联设备"
        >
          <el-option v-for="item in myDevices" :key="item.id" :label="item.name" :value="item.id">
            <span class="font-medium">{{ item.name }}</span>
            <span class="float-right text-gray-400 text-xs ml-2">{{ item.assetNo }}</span>
          </el-option>
        </el-select>
        <div v-if="!loadingDevices && myDevices.length === 0" class="text-xs text-orange-400 mt-1"> 您当前名下没有任何设备 </div>
      </el-form-item>
      <el-form-item label="故障标题" prop="failureName">
        <el-input v-model="form.failureName" placeholder="简要描述故障，如：屏幕碎裂" maxlength="50" />
      </el-form-item>
      <el-form-item label="详细描述" prop="failureDescription">
        <el-input
          v-model="form.failureDescription"
          type="textarea"
          :rows="4"
          placeholder="请详细描述故障情况、发生时的操作等"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="发生时间" prop="failureDate">
        <el-date-picker
          v-model="form.failureDate"
          type="datetime"
          placeholder="选择故障发生时间"
          class="w-full !w-full"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-value="new Date()"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit"> 提交报修 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, onMounted } from 'vue'
  import { reportFailure, type FailuresEntity } from '@/api/itam/failures'
  import { getMyDevices, type DeviceVO } from '@/api/itam/personal'
  import { AssetType, FailureStatus } from '@/api/itam/enums'
  import { ElMessage, type FormInstance } from 'element-plus'
  import dayjs from 'dayjs'

  const visible = defineModel<boolean>('visible', { required: true })
  const emit = defineEmits(['success'])

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const loadingDevices = ref(false)
  const myDevices = ref<DeviceVO[]>([])

  const form = reactive<FailuresEntity>({
    targetType: AssetType.DEVICE, // Default to Device
    targetId: undefined as unknown as number,
    failureName: '',
    failureDescription: '',
    failureDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    status: FailureStatus.PENDING
  })

  const rules = {
    targetId: [{ required: true, message: '请选择故障设备', trigger: 'change' }],
    failureName: [{ required: true, message: '请输入故障标题', trigger: 'blur' }],
    failureDate: [{ required: true, message: '请选择发生时间', trigger: 'change' }]
  }

  const fetchDevices = async () => {
    try {
      loadingDevices.value = true
      // Fetch all devices (pagination simplified for now, assuming user < 100 devices)
      const res = await getMyDevices({ current: 1, size: 100 } as any)
      myDevices.value = res.records || []
      // Auto-select if only one device
      if (myDevices.value.length === 1) {
        form.targetId = myDevices.value[0].id
      }
    } catch (e) {
      console.error(e)
    } finally {
      loadingDevices.value = false
    }
  }

  // Reset form when dialog opens
  watch(visible, (val) => {
    if (val) {
      form.targetId = undefined as unknown as number
      form.failureName = ''
      form.failureDescription = ''
      form.failureDate = dayjs().format('YYYY-MM-DD HH:mm:ss')
      fetchDevices()
      setTimeout(() => formRef.value?.clearValidate(), 0)
    }
  })

  // Also fetch on mount just in case or if we want to preload
  onMounted(() => {
    // We defer fetching until open usually, but valid to check
  })

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = true
          await reportFailure(form)
          ElMessage.success('报修提交成功')
          visible.value = false
          emit('success')
        } catch (error) {
          console.error(error)
        } finally {
          loading.value = false
        }
      }
    })
  }
</script>
