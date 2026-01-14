<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar v-if="showSearch" ref="searchBarRef" v-model="searchForm" :items="searchFormItems"
        @reset="resetSearchParams" @search="handleSearch">
      </ArtSearchBar>
      <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }">
        <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading"
          @refresh="refreshData">
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:models:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                v-hasPermi="['itam:models:delete']">
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:models:export']" icon="ele-Download" v-ripple @click="handleExport">
                {{ $t('common.export') }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
          @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange">
          <template #imageUrl="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" :preview-src-list="[row.imageUrl]"
              style="width: 50px; height: 50px; border-radius: 4px;" fit="cover" preview-teleported />
            <span v-else class="text-gray-400">暂无图片</span>
          </template>
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:models:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:models:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改型号对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="modelsRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="型号名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入型号名称" />
        </el-form-item>
        <el-form-item label="型号图片" prop="imageUrl">
          <ImageUpload v-model:value="imageUrlValue" />
        </el-form-item>
        <el-form-item label="厂商" prop="manufacturerId">
          <el-select v-model="form.manufacturerId" placeholder="请选择厂商" clearable filterable class="w-full">
            <el-option v-for="item in manufacturerOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="折旧规则" prop="depreciationId">
          <el-select v-model="form.depreciationId" placeholder="请选择折旧规则" clearable filterable class="w-full">
            <el-option v-for="item in depreciationOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号编码" prop="modelNumber">
          <el-input v-model="form.modelNumber" placeholder="请输入型号编码" />
        </el-form-item>
        <el-form-item label="报废年限 (月)" prop="eol">
          <el-input v-model="form.eol" placeholder="请输入报废年限 (月)" />
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
import { computed, ref, useTemplateRef, onMounted } from 'vue'
import {
  addModels,
  delModels,
  exportModels,
  getModels,
  ModelsEntity,
  pageModels,
  updateModels
} from '@/api/itam/models'
import { listManufacturers } from '@/api/itam/manufacturers'
import { listDepreciations } from '@/api/itam/depreciations'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'
import { useI18n } from 'vue-i18n'
import ImageUpload from '@/components/business/image-upload/index.vue'

defineOptions({
  name: 'models'
})

const { t } = useI18n()

const showSearch = ref(true)

const selectedRows = ref<ModelsEntity[]>([])

// 下拉选项数据
const manufacturerOptions = ref<any[]>([])
const depreciationOptions = ref<any[]>([])

// 加载选项数据
const loadManufacturers = async () => {
  try {
    const data = await listManufacturers()
    manufacturerOptions.value = data || []
  } catch (error) {
    console.error('加载厂商失败:', error)
  }
}

const loadDepreciations = async () => {
  try {
    const data = await listDepreciations()
    depreciationOptions.value = data || []
  } catch (error) {
    console.error('加载折旧规则失败:', error)
  }
}

const searchForm = ref({
  name: undefined,
  modelNumber: undefined,
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '型号名称',
    key: 'name',
    type: 'input',
    placeholder: '请输入型号名称',
    clearable: true
  },
  {
    label: '型号编码',
    key: 'modelNumber',
    type: 'input',
    placeholder: '请输入型号编码',
    clearable: true
  }
])

const {
  columns,
  columnChecks,
  data,
  loading,
  getData,
  pagination,
  searchParams,
  resetSearchParams,
  handleSizeChange,
  handleCurrentChange,
  refreshData
} = useTable({
  core: {
    apiFn: pageModels,
    apiParams: {
      current: 1,
      size: 10,
      ...searchForm.value
    },
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'modelNumber', label: '型号编码', minWidth: 150 },
      { prop: 'name', label: '型号名称', minWidth: 150 },
      { prop: 'imageUrl', label: '型号图片', minWidth: 120, useSlot: true },
      { prop: 'manufacturerName', label: '厂商', minWidth: 120 },
      { prop: 'depreciationName', label: '折旧规则', minWidth: 120 },
      { prop: 'eol', label: '报废年限(月)', minWidth: 120 },
      {
        prop: 'operation',
        label: t('system.noticeLog.operation'),
        useSlot: true,
        minWidth: 150,
        fixed: 'right'
      }
    ]
  }
})

const searchBarRef = ref()

const handleSearch = () => {
  searchBarRef.value.validate().then(() => {
    Object.assign(searchParams, searchForm.value)
    getData()
  })
}

const handleSelectionChange = (selection: any[]): void => {
  selectedRows.value = selection
}

const open = ref(false)

const form = ref<Partial<ModelsEntity>>({
  name: '',
  imageUrl: '',
  manufacturerId: undefined,
  depreciationId: undefined,
  modelNumber: '',
  eol: undefined
})

const formRules = ref({
  id: [
    {
      required: true,
      message: 'Id不能为空',
      trigger: 'blur'
    }
  ],
  deleted: [
    {
      required: true,
      message: '是否删除不能为空',
      trigger: 'blur'
    }
  ],
  name: [
    {
      required: true,
      message: '型号名称不能为空',
      trigger: 'blur'
    }
  ],
  imageUrl: [
    {
      required: true,
      message: '型号图片不能为空',
      trigger: 'blur'
    }
  ],
  manufacturerId: [
    {
      required: true,
      message: '厂商 ID不能为空',
      trigger: 'blur'
    }
  ],
  categoryId: [
    {
      required: true,
      message: '分类 ID不能为空',
      trigger: 'blur'
    }
  ],
  depreciationId: [
    {
      required: true,
      message: '关联折旧规则 ID不能为空',
      trigger: 'blur'
    }
  ],
  modelNumber: [
    {
      required: true,
      message: '型号编码不能为空',
      trigger: 'blur'
    }
  ],
  eol: [
    {
      required: true,
      message: '报废年限 (月)不能为空',
      trigger: 'blur'
    }
  ],
  createTime: [
    {
      required: true,
      message: '创建时间不能为空',
      trigger: 'blur'
    }
  ],
  updateTime: [
    {
      required: true,
      message: '更新时间不能为空',
      trigger: 'blur'
    }
  ]
})

// Computed property to handle imageUrl null conversion for ImageUpload component
const imageUrlValue = computed({
  get: () => form.value.imageUrl ?? '',
  set: (val: string | unknown[]) => {
    // Convert empty string back to empty string (not null) for consistency
    form.value.imageUrl = val === '' ? '' : (typeof val === 'string' ? val : (val as string[]).join(','))
  }
})

const modelsRef = useTemplateRef('modelsRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
  form.value = {
    name: '',
    imageUrl: '',
    manufacturerId: undefined,
    depreciationId: undefined,
    modelNumber: '',
    eol: undefined
  }
  resetFormRef(modelsRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  dialogTitle.value = '添加型号'
}

/** 修改按钮操作 */
const handleUpdate = (row: ModelsEntity) => {
  reset()
  if (row.id) {
    getModels(row.id).then((data: ModelsEntity) => {
      form.value = {
        ...data,
      }
      open.value = true
      dialogTitle.value = '修改型号'
    })
  }
}

/** 提交按钮 */
const submitForm = () => {
  modelsRef.value!.validate((valid: boolean) => {
    if (valid) {
      saveLoading.value = true
      if (form.value.id != null) {
        updateModels(form.value)
          .then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      } else {
        addModels(form.value)
          .then(() => {
            MessageUtil.success(t('common.addSuccess'))
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      }
    }
  })
}

const handleDelete = (row?: any) => {
  const ids = row?.id || selectedRows.value.map((item) => item.id).join(',')
  ElMessageBox.confirm(t('system.noticeLog.confirmDelete') + ids + t('system.noticeLog.dataItem'), t('common.warning'))
    .then(() => delModels(ids))
    .then(() => {
      refreshData()
      MessageUtil.success(t('common.deleteSuccess'))
    })
    .catch(() => { })
}

// 取消按钮
const cancel = () => {
  open.value = false
  reset()
}

const exportLoading = ref(false)
const handleExport = () => {
  ElMessageBox.confirm(t('common.exportConfirm'), t('common.warning'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      try {
        exportLoading.value = true
        const response = await exportModels(searchParams)
        download(response, '型号数据')
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exportLoading.value = false
      }
    })
    .catch(() => { })
}

// 组件挂载时加载选项数据
onMounted(() => {
  loadManufacturers()
  loadDepreciations()
})
</script>

<style scoped lang="scss">
.log-page {
  padding: 16px;
}
</style>
