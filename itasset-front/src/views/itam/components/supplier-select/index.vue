<template>
  <el-select v-model="val" filterable placeholder="请选择供应商" clearable @change="handleChange">
    <el-option v-for="item in options" :key="item.id" :label="item.name" :value="item.id!" />
  </el-select>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listSuppliers, SuppliersEntity } from '@/api/itam/suppliers'

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

const options = ref<SuppliersEntity[]>([])

const getSupplierList = (name?: string) => {
  listSuppliers({ name }).then((res) => {
    options.value = res || []
  })
}

const handleChange = (id: any) => {
  const item = options.value.find((o) => o.id === id)
  emit('change', id, item)
}

onMounted(() => {
  getSupplierList()
})
</script>
