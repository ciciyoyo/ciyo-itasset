<template>
  <div class="p-4 art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar v-if="showSearch" ref="searchBarRef" v-model="searchForm" :items="searchFormItems"
        @reset="resetSearchParams" @search="handleSearch">
      </ArtSearchBar>
      <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never" :class="showSearch ? 'mt-3' : 'mt-0'">
        <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading"
          @refresh="refreshData">
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple
                v-hasPermi="['itam:depreciations:add']">
                新增
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                v-hasPermi="['itam:depreciations:delete']">
                删除
              </el-button>

              <el-button v-hasPermi="['itam:depreciations:export']" icon="ele-Download" v-ripple @click="handleExport">
                导出
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
          @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange">
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:depreciations:update']">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:depreciations:delete']"> 删除
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改折旧规则对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="800px" append-to-body>
      <el-form ref="depreciationsRef" :model="form" :rules="formRules" label-width="140px">
        <el-form-item label="折旧规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入折旧规则名称" />
        </el-form-item>
        <el-form-item label="规则" prop="stages">
          <div class="w-full">
            <div class="relative top-0 text-[#909399] text-xs leading-normal pt-1 flex items-start">
              <el-icon class="mr-1 mt-0.5"><ele-QuestionFilled /></el-icon>
              <span>周期填入最大即可，例如2年代表超过2年，5年代表超过5年。规则会优先从高到低匹配，先判断是否超过5年，如果没有超过则再判断是否超过3年，以此类推。
              </span>
            </div>
            <!-- 表头 -->
            <el-table :data="stages" class="mb-2 border-rd">
              <el-table-column label="周期" align="center">
                <template #default="{ row }">
                  <el-input-number controls-position="right" v-model="row.period" :min="1" placeholder="周期"
                    class="!w-full" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="120" align="center">
                <template #default="{ row }">
                  <el-select v-model="row.unit" placeholder="单位" class="!w-full">
                    <el-option label="年" value="YEAR" />
                    <el-option label="月" value="MONTH" />
                    <el-option label="日" value="DAY" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="比率" width="160" align="center">
                <template #default="{ row }">
                  <el-input-number v-model="row.ratio" :min="0" :max="1" :step="0.1" :precision="2" placeholder="比例"
                    controls-position="right" class="!w-full" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-icon @click="removeRule($index)" v-if="stages.length > 1" class="text-red-500 cursor-pointer">
                    <ele-Delete />
                  </el-icon>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" icon="ele-Plus" @click="addRule">添加规则</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { computed, reactive, ref, useTemplateRef } from 'vue'
import {
  addDepreciations,
  delDepreciations,
  DepreciationsEntity,
  exportDepreciations,
  getDepreciations,
  pageDepreciations,
  updateDepreciations
} from '@/api/itam/depreciations'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'

defineOptions({
  name: 'depreciations'
})

const showSearch = ref(true)

const selectedRows = ref<DepreciationsEntity[]>([])

// 规则列表
interface RuleItem {
  period: number
  unit: string
  ratio: number
}

const stages = ref<RuleItem[]>([{ period: 1, unit: 'YEAR', ratio: 0 }])

const addRule = () => {
  stages.value.push({ period: 1, unit: 'YEAR', ratio: 0 })
}

const removeRule = (index: number) => {
  stages.value.splice(index, 1)
}

const searchForm = ref({
  name: undefined,
  stages: undefined
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '规则名称',
    key: 'name',
    type: 'input',
    placeholder: '请输入折旧规则名称',
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
    apiFn: pageDepreciations,
    apiParams: {
      current: 1,
      size: 10,
      ...searchForm.value
    },
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'id', label: '', minWidth: 120 },
      { prop: 'name', label: '折旧规则名称', minWidth: 120 },
      { prop: 'remark', label: '描述', minWidth: 120 },
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

const form = ref<DepreciationsEntity>({
  id: undefined,
  name: '',
  stages: [],
  remark: ''
})

const formRules = ref({
  name: [
    {
      required: true,
      message: '折旧规则名称不能为空',
      trigger: 'blur'
    }
  ],
  stages: [
    {
      validator: (_rule: any, _value: any, callback: any) => {
        if (stages.value.length === 0) {
          callback(new Error('请至少添加一条规则'))
        } else if (stages.value.some((item) => !item.period || !item.unit || item.ratio === undefined)) {
          callback(new Error('请完善规则信息'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const depreciationsRef = useTemplateRef('depreciationsRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
  form.value = {
    id: undefined,
    name: '',
    stages: [],
    remark: ''
  }
  // 重置 stages 数组
  stages.value = [{ period: 1, unit: 'YEAR', ratio: 0 }]
  resetFormRef(depreciationsRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  dialogTitle.value = '添加折旧规则'
}

/** 修改按钮操作 */
const handleUpdate = (row: DepreciationsEntity) => {
  reset()
  if (row.id) {
    getDepreciations(row.id).then((data: DepreciationsEntity) => {
      form.value = data
      // 回显 stages 数据
      if (data.stages && data.stages.length > 0) {
        stages.value = data.stages
      }
      open.value = true
      dialogTitle.value = '修改折旧规则'
    })
  }
}

/** 提交按钮 */
const submitForm = () => {
  depreciationsRef.value!.validate((valid: boolean) => {
    if (valid) {
      saveLoading.value = true
      // 只提交必要的参数
      const submitData = {
        id: form.value.id,
        name: form.value.name,
        stages: stages.value,
        remark: form.value.remark
      }
      if (form.value.id != null) {
        updateDepreciations(submitData)
          .then(() => {
            MessageUtil.success('保存成功')
            open.value = false
            refreshData()
          })
          .finally(() => {
            saveLoading.value = false
          })
      } else {
        addDepreciations(submitData)
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
    .then(() => delDepreciations(ids))
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
        const response = await exportDepreciations(searchParams)
        download(response, '折旧规则数据')
      } catch (error) {
        console.error('导出失败:', error)
      } finally {
        exportLoading.value = false
      }
    })
    .catch(() => { })
}
</script>
