<template>
  <el-select v-model="selectedValue" :loading="loading" :placeholder="placeholder" v-bind="$attrs" clearable>
    <el-option v-for="item in dictData" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" />
  </el-select>
</template>

<script setup name="DictSelect" lang="ts">
  import { computed } from 'vue'
  import { useDict } from '@/components/business/dict-select/useDict'

  const props = defineProps({
    modelValue: {
      type: [String, Number, Array],
      default: undefined
    },
    dictType: {
      type: String,
      required: true
    },
    placeholder: {
      type: String,
      default: '请选择'
    }
  })

  const emit = defineEmits(['update:modelValue', 'change'])

  // Obtain dictionary data logic
  const { dictData, loading } = useDict(props.dictType)

  // v-model binding
  const selectedValue = computed({
    get: () => props.modelValue,
    set: (val) => {
      emit('update:modelValue', val)
      emit('change', val)
    }
  })
</script>
