<template>
  <el-select v-model="val" :multiple="multiple" filterable placeholder="请选择设备" clearable @change="handleChange">
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="item.name + (item.deviceNo ? ` (${item.deviceNo})` : '')"
      :value="item.id!"
    />
  </el-select>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { DeviceEntity, listDevice } from '@/api/itam/device'

  const props = defineProps({
    modelValue: {
      type: [Number, String, Array],
      default: null
    },
    multiple: {
      type: Boolean,
      default: false
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

  const handleChange = (val: any) => {
    if (props.multiple) {
      const items = options.value.filter((o) => (val as any[]).includes(o.id))
      emit('change', val, items)
    } else {
      const item = options.value.find((o) => o.id === val)
      emit('change', val, item)
    }
  }

  onMounted(() => {
    getDeviceList()
  })
</script>
