<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never">
        <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadCategories">
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:categories:add']">
                {{ $t('common.add') }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Tree Table -->
        <ArtTable
          ref="tableRef"
          class="flex-1"
          :loading="loading"
          :data="categoryTree"
          :columns="columns"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          row-key="id"
        >
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" v-hasPermi="['itam:device:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:device:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- 添加或修改分类对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTreeOptions"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择上级分类"
            clearable
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入分类编码" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, computed, useTemplateRef } from 'vue'
  import { getCategoriesTree, addCategories, updateCategories, delCategories, CategoriesEntity } from '@/api/itam/categories'
  import { ElMessageBox } from 'element-plus'
  import { MessageUtil } from '@/utils/messageUtil'
  import { resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { AssetType } from '@/api/itam/enums'

  defineOptions({
    name: 'DeviceCategory'
  })

  const { t } = useI18n()

  const loading = ref(false)
  const categoryTree = ref<CategoriesEntity[]>([])
  const open = ref(false)
  const dialogTitle = ref('')
  const saveLoading = ref(false)
  const formRef = useTemplateRef('formRef')
  const tableRef = ref()

  const form = ref<Partial<CategoriesEntity>>({
    id: undefined,
    name: '',
    code: '',
    parentId: undefined,
    categoryType: AssetType.DEVICE
  })

  const formRules = {
    name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    code: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }]
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'name',
      label: '分类名称',
      minWidth: 200
    },
    {
      prop: 'code',
      label: '分类编码',
      minWidth: 150
    },
    {
      prop: 'createTime',
      label: '创建时间',
      minWidth: 160
    },
    {
      prop: 'operation',
      label: t('common.operation'),
      width: 180,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 构建树形选择器选项（添加顶级选项）
  const categoryTreeOptions = computed(() => {
    return [
      {
        id: 0,
        name: '顶级分类',
        children: categoryTree.value
      }
    ]
  })

  const loadCategories = async () => {
    loading.value = true
    try {
      const data = await getCategoriesTree(AssetType.DEVICE)
      categoryTree.value = data || []
    } catch (error) {
      console.error('加载分类失败:', error)
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    form.value = {
      id: undefined,
      name: '',
      code: '',
      parentId: undefined,
      categoryType: AssetType.DEVICE
    }
    resetFormRef(formRef)
  }

  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加分类'
  }

  const handleEdit = (row: CategoriesEntity) => {
    reset()
    form.value = { ...row }
    open.value = true
    dialogTitle.value = '修改分类'
  }

  const submitForm = () => {
    formRef.value!.validate(async (valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        try {
          const submitData = {
            ...form.value,
            parentId: form.value.parentId || 0,
            categoryType: AssetType.DEVICE
          } as CategoriesEntity

          if (form.value.id) {
            await updateCategories(submitData)
            MessageUtil.success(t('common.saveSuccess'))
          } else {
            await addCategories(submitData)
            MessageUtil.success(t('common.addSuccess'))
          }
          open.value = false
          loadCategories()
        } catch (error) {
          console.error('保存失败:', error)
        } finally {
          saveLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: CategoriesEntity) => {
    ElMessageBox.confirm(`确认删除分类"${row.name}"吗？`, t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        await delCategories(row.id)
        MessageUtil.success(t('common.deleteSuccess'))
        loadCategories()
      })
      .catch(() => {})
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  onMounted(() => {
    loadCategories()
  })
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
