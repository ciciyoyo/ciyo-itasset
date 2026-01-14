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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:locations:add']">
                新增
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                v-hasPermi="['itam:locations:delete']">
                删除
              </el-button>

              <el-button v-hasPermi="['itam:locations:export']" icon="ele-Download" v-ripple @click="handleExport">
                导出
              </el-button>

              <el-button icon="ele-Sort" v-ripple @click="toggleExpandAll">
                {{ isExpandAll ? '折叠全部' : '展开全部' }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable ref="tableRef" class="flex-1" :loading="loading" :data="data" :columns="columns" row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" @selection-change="handleSelectionChange">
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:locations:update']"> 编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:locations:delete']"> 删除
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改物理位置对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="locationsRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="位置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入位置名称" />
        </el-form-item>
        <el-form-item label="上级位置" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="data"
            :props="{ label: 'name', value: 'id', children: 'children' }" placeholder="请选择上级位置" clearable check-strictly
            :render-after-expand="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="该处负责人" prop="managerId">
          <el-input v-model="form.managerId" placeholder="请输入该处负责人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取消</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useTemplateRef } from 'vue'
import {
  addLocations,
  delLocations,
  exportLocations,
  getLocations,
  listLocations,
  LocationsEntity,
  updateLocations
} from '@/api/itam/locations'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'

defineOptions({
  name: 'locations'
})

const showSearch = ref(true)

const selectedRows = ref<LocationsEntity[]>([])

const searchForm = ref({
  name: undefined,
  parentId: undefined,
  managerId: undefined
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '位置名称',
    key: 'name',
    type: 'input',
    placeholder: '请输入位置名称',
    clearable: true
  }
])

const {
  columns,
  columnChecks,
  data,
  loading,
  getData,
  searchParams,
  resetSearchParams,
  refreshData
} = useTable({
  core: {
    apiFn: async (params) => {
      const treeData = await listLocations(params as any)
      return { records: treeData as any[], total: treeData.length }
    },
    apiParams: {
      ...searchForm.value
    },
    excludeParams: ['current', 'size'],  // 排除分页参数
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'id', label: 'Id', minWidth: 120 },
      { prop: 'name', label: '位置名称', minWidth: 120 },
      { prop: 'managerId', label: '负责人', minWidth: 120 },
      {
        prop: 'operation',
        label: '操作',
        useSlot: true,
        minWidth: 100,
        fixed: 'right'
      }
    ]
  }
})

const searchBarRef = ref()
const tableRef = ref()
const isExpandAll = ref(false)

const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  toggleRowExpansionAll(data.value, isExpandAll.value)
}

const toggleRowExpansionAll = (data: any[], isExpansion: boolean) => {
  data.forEach((item) => {
    tableRef.value.elTableRef.toggleRowExpansion(item, isExpansion)
    if (item.children && item.children.length > 0) {
      toggleRowExpansionAll(item.children, isExpansion)
    }
  })
}

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

const form = ref<LocationsEntity>({
  id: 0,
  name: '',
  parentId: 0,
  managerId: 0
})

const formRules = ref({
  name: [
    {
      required: true,
      message: '位置名称不能为空',
      trigger: 'blur'
    }
  ],
  parentId: [
    {
      required: true,
      message: '父级 ID不能为空',
      trigger: 'blur'
    }
  ]
})

const locationsRef = useTemplateRef('locationsRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
  form.value = {
    id: undefined,
    name: '',
    parentId: undefined,
    managerId: undefined
  }
  resetFormRef(locationsRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  dialogTitle.value = '添加物理位置'
}

/** 修改按钮操作 */
const handleUpdate = (row: LocationsEntity) => {
  reset()
  if (row.id) {
    getLocations(row.id).then((data: LocationsEntity) => {
      form.value = data
      open.value = true
      dialogTitle.value = '修改物理位置'
    })
  }
}

/** 提交按钮 */
const submitForm = () => {
  locationsRef.value!.validate((valid: boolean) => {
    if (valid) {
      saveLoading.value = true
      if (form.value.id != null) {
        updateLocations(form.value)
          .then(() => {
            MessageUtil.success('保存成功')
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      } else {
        addLocations(form.value)
          .then(() => {
            MessageUtil.success('添加成功')
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
  ElMessageBox.confirm('是否确认删除编号为' + ids + '的数据项?', '警告')
    .then(() => delLocations(ids))
    .then(() => {
      refreshData()
      MessageUtil.success('删除成功')
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
  ElMessageBox.confirm('是否确认导出所有数据项?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        exportLoading.value = true
        const response = await exportLocations(searchParams)
        download(response, '物理位置数据')
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exportLoading.value = false
      }
    })
    .catch(() => { })
}
</script>

<style scoped lang="scss">
.log-page {
  padding: 16px;
}
</style>
