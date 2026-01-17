<template>
  <el-dialog v-model="visible" title="发起资产申请" width="500px" destroy-on-close :close-on-click-modal="false" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="py-2">
      <el-form-item label="申请类型" prop="itemType">
        <DictSelect
          v-model="form.itemType"
          dict-type="asset_type"
          placeholder="请选择类型"
          class="w-full"
          @change="handleTypeChange"
        />
      </el-form-item>
      <el-form-item label="资产分类" prop="categoryId">
        <el-tree-select
          v-model="form.categoryId"
          :data="categoryOptions"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择具体分类"
          check-strictly
          filterable
          class="w-full"
        />
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number
          v-model="form.quantity"
          :min="1"
          :max="form.itemType === 'device' ? 1 : 100"
          class="w-full !w-full"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="申请原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请输入申请原因，例如：&#10;1. 新员工入职配置&#10;2. 原设备故障无法使用&#10;3. 项目测试需求"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      <template v-if="['device', 'accessory', 'asset'].includes(form.itemType)">
        <el-form-item label="长期使用" prop="isLongTerm">
          <el-switch v-model="form.isLongTerm" />
        </el-form-item>
        <el-form-item
          v-if="!form.isLongTerm"
          label="预计归还"
          prop="expectedReturnTime"
          :rules="[{ required: true, message: '请选择预计归还时间', trigger: 'change' }]"
        >
          <el-date-picker
            v-model="form.expectedReturnTime"
            type="datetime"
            placeholder="选择预计归还时间"
            class="w-full"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit"> 提交申请 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, reactive, watch } from 'vue'
  import { submitAssetRequest, type SubmitRequestParams } from '@/api/itam/personal'
  import { getCategoriesTree, type CategoriesEntity } from '@/api/itam/categories'
  import { ElMessage, type FormInstance } from 'element-plus'
  import DictSelect from '@/components/business/dict-select/index.vue'

  // Using defineModel for two-way binding of 'visible' prop (Vue 3.4+)
  const visible = defineModel<boolean>('visible', { required: true })
  const emit = defineEmits(['success'])

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const categoryOptions = ref<CategoriesEntity[]>([])

  const form = reactive<SubmitRequestParams>({
    itemType: 'device',
    categoryId: undefined,
    quantity: 1,
    reason: '',
    isLongTerm: true,
    expectedReturnTime: undefined
  })

  const rules = {
    itemType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
    categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
    quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
  }

  const fetchCategories = async (type: string) => {
    try {
      const res = await getCategoriesTree(type)
      categoryOptions.value = res || []
    } catch (error) {
      console.error('Failed to fetch categories:', error)
      categoryOptions.value = []
    }
  }

  const handleTypeChange = (val: string) => {
    form.categoryId = undefined
    fetchCategories(val)
  }

  // Reset form when dialog opens
  watch(visible, (val) => {
    if (val) {
      form.itemType = 'device'
      form.categoryId = undefined
      form.quantity = 1
      form.reason = ''
      form.isLongTerm = true
      form.expectedReturnTime = undefined

      // Load initial categories
      fetchCategories(form.itemType)

      // Clear validation errors
      setTimeout(() => formRef.value?.clearValidate(), 0)
    }
  })

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          loading.value = true
          await submitAssetRequest(form)
          ElMessage.success('申请提交成功')
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

<style scoped>
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }
</style>
