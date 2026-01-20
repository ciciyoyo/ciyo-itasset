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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple
                v-hasPermi="['itam:stocktakes:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button :disabled="!selectedRows.length" type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                v-hasPermi="['itam:stocktakes:delete']">
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:stocktakes:export']" icon="ele-Download" v-ripple @click="handleExport">
                {{ $t('common.export') }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
          @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange">
          <template #name="{ row }">
            <el-link type="primary" :underline="false" @click="handleViewDetail(row)">
              {{ row.name }}
            </el-link>
          </template>
          <template #status="{ row }">
            <el-tag v-if="row.status === 'draft'" type="info">{{ row.statusDesc || '草稿' }}</el-tag>
            <el-tag v-else-if="row.status === 'processing'" type="primary">{{ row.statusDesc || '盘点中' }}</el-tag>
            <el-tag v-else-if="row.status === 'finished'" type="success">{{ row.statusDesc || '已完成' }}</el-tag>
            <el-tag v-else-if="row.status === 'canceled'" type="danger">{{ row.statusDesc || '已取消' }}</el-tag>
            <el-tag v-else type="warning">{{ row.statusDesc || '未知' }}</el-tag>
          </template>
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:stocktakes:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:stocktakes:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改盘点任务对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="stocktakesRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="盘点任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入盘点任务名称" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="盘点位置" prop="locationId">
          <el-tree-select v-model="form.locationId" :data="locationOptions" check-strictly :render-after-expand="false"
            placeholder="请选择位置" clearable node-key="id" :props="{ label: 'name', children: 'children' }"
            class="!w-full" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="申请类型" prop="itemType">
          <DictSelect v-model="form.itemType" dict-type="asset_type" placeholder="请选择类型" class="!w-full"
            @change="handleItemTypeChange" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="盘点分类" prop="categoryId">
          <el-tree-select v-model="form.categoryId" :data="categoryOptions" check-strictly :render-after-expand="false"
            placeholder="请选择分类" clearable node-key="id" :props="{ label: 'name', children: 'children' }"
            class="!w-full" />
        </el-form-item>

        <el-form-item label="计划开始日期" prop="startDate">
          <el-date-picker clearable v-model="form.startDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择计划开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="计划结束日期" prop="endDate">
          <el-date-picker clearable v-model="form.endDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择计划结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="form.note" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 盘点明细弹窗 -->
    <StocktakeItemsModal ref="stocktakeItemsModalRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useTemplateRef, onMounted } from 'vue'
import {
  addStocktakes,
  delStocktakes,
  exportStocktakes,
  getStocktakes,
  pageStocktakes,
  StocktakesEntity,
  updateStocktakes
} from '@/api/itam/stocktakes'
import { listLocations } from '@/api/itam/locations'
import { getCategoriesTree, CategoriesEntity } from '@/api/itam/categories'
import { AssetType } from '@/api/itam/enums'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'
import { useI18n } from 'vue-i18n'
import DictSelect from '@/components/business/dict-select/index.vue'
import StocktakeItemsModal from './components/StocktakeItemsModal.vue'

defineOptions({
  name: 'stocktakes'
})

const { t } = useI18n()

const showSearch = ref(true)

const selectedRows = ref<StocktakesEntity[]>([])

// 位置选项数据
const locationOptions = ref<any[]>([])
// 分类选项数据
const categoryOptions = ref<CategoriesEntity[]>([])

// 加载位置数据
const loadLocations = async () => {
  try {
    const data = await listLocations()
    locationOptions.value = data || []
  } catch (error) {
    console.error('加载位置失败:', error)
  }
}

// 加载分类数据
const loadCategories = async (categoryType?: string) => {
  try {
    const type = categoryType || AssetType.DEVICE
    const data = await getCategoriesTree(type)
    categoryOptions.value = data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const searchForm = ref({
  name: undefined
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '盘点任务名称',
    key: 'name',
    type: 'input',
    placeholder: '请输入盘点任务名称',
    clearable: true,
    labelWidth: '120px'
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
    apiFn: pageStocktakes,
    apiParams: {
      current: 1,
      size: 10,
      ...searchForm.value
    },
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'id', label: 'Id', minWidth: 120 },
      { prop: 'name', label: '盘点任务名称', minWidth: 120, useSlot: true },
      { prop: 'locationName', label: '盘点位置', minWidth: 120 },
      { prop: 'categoryName', label: '盘点分类', minWidth: 120 },
      { prop: 'status', label: '状态', minWidth: 100, useSlot: true },
      { prop: 'managerName', label: '负责人', minWidth: 100 },
      { prop: 'startDate', label: '计划开始日期', minWidth: 120 },
      { prop: 'endDate', label: '计划结束日期', minWidth: 120 },
      { prop: 'note', label: '备注', minWidth: 120 },
      {
        prop: 'operation',
        label: t('system.noticeLog.operation'),
        useSlot: true,
        minWidth: 120,
        fixed: 'right',
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

const form = ref<Partial<StocktakesEntity> & { itemType?: string }>({
  id: undefined,
  name: '',
  locationId: undefined,
  categoryId: undefined,
  itemType: 'device',  // 默认设备类型，仅前端使用，用于控制分类加载
  startDate: '',
  endDate: '',
  note: ''
})

const formRules = ref({
  name: [
    {
      required: true,
      message: '盘点任务名称不能为空',
      trigger: 'blur'
    }
  ],

  categoryId: [
    {
      required: true,
      message: '盘点分类不能为空',
      trigger: 'blur'
    }
  ],
  itemType: [
    {
      required: true,
      message: '申请类型不能为空',
      trigger: 'change'
    }
  ],


})

const stocktakesRef = useTemplateRef('stocktakesRef')
const stocktakeItemsModalRef = useTemplateRef('stocktakeItemsModalRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
  form.value = {
    id: undefined,
    name: '',
    locationId: undefined,
    categoryId: undefined,
    itemType: 'device',  // 默认设备类型
    startDate: '',
    endDate: '',
    note: ''
  }
  resetFormRef(stocktakesRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  dialogTitle.value = '添加盘点任务'
  // 加载默认类型的分类
  loadCategories(form.value.itemType)
}

/** 查看盘点明细 */
const handleViewDetail = (row: StocktakesEntity) => {
  stocktakeItemsModalRef.value?.open(row)
}

/** 修改按钮操作 */
const handleUpdate = (row: StocktakesEntity) => {
  reset()
  if (row.id) {
    getStocktakes(row.id).then((data: StocktakesEntity) => {
      // 赋值时添加前端专用字段
      form.value = { ...data, itemType: 'device' }
      // 加载默认类型的分类（因为后端不返回 itemType）
      loadCategories(form.value.itemType!)
      open.value = true
      dialogTitle.value = '修改盘点任务'
    })
  }
}

/** 提交按钮 */
const submitForm = () => {
  stocktakesRef.value!.validate((valid: boolean) => {
    if (valid) {
      saveLoading.value = true
      // 排除前端专用字段：itemType
      const { itemType, ...submitData } = form.value

      if (form.value.id != null) {
        updateStocktakes(submitData as StocktakesEntity)
          .then(() => {
            MessageUtil.success(t('common.saveSuccess'))
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      } else {
        addStocktakes(submitData as StocktakesEntity)
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
    .then(() => delStocktakes(ids))
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
        const response = await exportStocktakes(searchParams)
        download(response, '盘点任务数据')
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exportLoading.value = false
      }
    })
    .catch(() => { })
}

// 资产类型变化时重新加载分类
const handleItemTypeChange = (val: string) => {
  // 清空分类选择
  form.value.categoryId = undefined
  // 加载新类型的分类
  if (val) {
    loadCategories(val)
  } else {
    // 如果清空类型，也清空分类选项
    categoryOptions.value = []
  }
}

// 组件挂载时加载位置数据
onMounted(() => {
  loadLocations()
  // 不需要在挂载时加载分类，会在打开对话框时加载
})
</script>

<style scoped lang="scss">
.log-page {
  padding: 16px;
}
</style>
