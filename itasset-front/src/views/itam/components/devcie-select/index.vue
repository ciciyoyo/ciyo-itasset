<template>
  <el-select v-model="val" filterable placeholder="请选择设备" clearable @change="handleChange">
    <el-option v-for="item in options" :key="item.id" :label="item.name" :value="item.id" />
  </el-select>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { DeviceEntity, listDevice } from '@/api/itam/device'

  const props = defineProps({
    modelValue: {
      type: [Number, String],
      default: null
    }
  })

  const emit = defineEmits(['update:modelValue', 'change'])

  const val = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
  })

  const options = ref<DeviceEntity[]>([])

  const getDeviceList = (name?: string) => {
    listDevice({ name }).then((res: any) => {
      options.value = res || []
    })
  }

  const handleChange = (id: any) => {
    const item = options.value.find((o) => o.id === id)
    emit('change', id, item)
  }

  onMounted(() => {
    getDeviceList()
  })
</script>
