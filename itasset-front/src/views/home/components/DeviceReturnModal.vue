<template>
  <el-dialog v-model="visible" title="设备归还" width="500px" destroy-on-close :close-on-click-modal="false" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="py-2">
      <el-form-item label="归还设备" prop="deviceId">
        <el-select
          v-model="form.deviceId"
          placeholder="请选择归还设备"
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
      <el-form-item label="归还原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入归还原因（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="归还日期" prop="returnDate">
        <el-date-picker
          v-model="form.returnDate"
          type="date"
          placeholder="选择归还日期"
          class="w-full !w-full"
          value-format="YYYY-MM-DD"
          :default-value="new Date()"
          :disabled-date="(time) => time.getTime() < Date.now() - 8.64e7"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit"> 确认归还 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch, onMounted } from 'vue'
  import { submitDeviceReturn, getMyDevices, type DeviceVO, type ReturnDeviceParams } from '@/api/itam/personal'
  import { ElMessage, type FormInstance } from 'element-plus'
  import dayjs from 'dayjs'

  const visible = defineModel<boolean>('visible', { required: true })
  const emit = defineEmits(['success'])

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const loadingDevices = ref(false)
  const myDevices = ref<DeviceVO[]>([])

  const form = reactive<ReturnDeviceParams>({
    deviceId: undefined as unknown as number,
    reason: '',
    returnDate: dayjs().format('YYYY-MM-DD')
  })

  const rules = {
    deviceId: [{ required: true, message: '请选择归还设备', trigger: 'change' }],
    returnDate: [{ required: true, message: '请选择归还日期', trigger: 'change' }]
  }

  const fetchDevices = async () => {
    try {
      loadingDevices.value = true
      // Fetch all devices (pagination simplified for now, assuming user < 100 devices)
      const res = await getMyDevices({ current: 1, size: 100 } as any)
      myDevices.value = res.records || []
      // Auto-select if only one device
      if (myDevices.value.length === 1) {
        form.deviceId = myDevices.value[0].id
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
      form.deviceId = undefined as unknown as number
      form.reason = ''
      form.returnDate = dayjs().format('YYYY-MM-DD')
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
          await submitDeviceReturn(form)
          ElMessage.success('归还申请提交成功')
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
