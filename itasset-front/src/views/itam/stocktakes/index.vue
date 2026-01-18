<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar
        v-if="showSearch"
        ref="searchBarRef"
        v-model="searchForm"
        :items="searchFormItems"
        @reset="resetSearchParams"
        @search="handleSearch"
      >
      </ArtSearchBar>
      <ElCard
        class="art-table-card flex flex-col flex-1 min-h-0"
        shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }"
      >
        <ArtTableHeader
          v-model:columns="columnChecks"
          v-model:showSearchBar="showSearch"
          :loading="loading"
          @refresh="refreshData"
        >
          <template #left>
            <ElSpace wrap>
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:stocktakes:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:stocktakes:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:stocktakes:export']" icon="ele-Download" v-ripple @click="handleExport">
                {{ $t('common.export') }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable
          class="flex-1"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        >
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
      <el-form ref="stocktakesRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="盘点任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入盘点任务名称" />
        </el-form-item>
        <el-form-item label="盘点位置范围 ID" prop="locationId">
          <el-input v-model="form.locationId" placeholder="请输入盘点位置范围 ID" />
        </el-form-item>
        <el-form-item label="盘点分类范围 ID" prop="categoryId">
          <el-input v-model="form.categoryId" placeholder="请输入盘点分类范围 ID" />
        </el-form-item>
        <el-form-item label="状态" prop="status"> </el-form-item>
        <el-form-item label="负责人 ID" prop="managerId">
          <el-input v-model="form.managerId" placeholder="请输入负责人 ID" />
        </el-form-item>
        <el-form-item label="计划开始日期" prop="startDate">
          <el-date-picker
            clearable
            v-model="form.startDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择计划开始日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="计划/实际结束日期" prop="endDate">
          <el-date-picker
            clearable
            v-model="form.endDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择计划/实际结束日期"
          >
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
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, useTemplateRef } from 'vue'
  import {
    addStocktakes,
    delStocktakes,
    exportStocktakes,
    getStocktakes,
    pageStocktakes,
    StocktakesEntity,
    updateStocktakes
  } from '@/api/itam/stocktakes'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'

  defineOptions({
    name: 'stocktakes'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<StocktakesEntity[]>([])

  const searchForm = ref({
    name: undefined,
    locationId: undefined,
    categoryId: undefined,
    status: undefined,
    managerId: undefined,
    startDate: undefined,
    endDate: undefined,
    note: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '盘点任务名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入盘点任务名称',
      clearable: true
    },
    {
      label: '盘点位置范围 ID',
      key: 'locationId',
      type: 'input',
      placeholder: '请输入盘点位置范围 ID',
      clearable: true
    },
    {
      label: '盘点分类范围 ID',
      key: 'categoryId',
      type: 'input',
      placeholder: '请输入盘点分类范围 ID',
      clearable: true
    },
    {
      label: '负责人 ID',
      key: 'managerId',
      type: 'input',
      placeholder: '请输入负责人 ID',
      clearable: true
    },
    {
      label: '计划开始日期',
      key: 'startDate',
      type: 'date',
      clearable: true
    },
    {
      label: '计划/实际结束日期',
      key: 'endDate',
      type: 'date',
      clearable: true
    },
    {
      label: '备注',
      key: 'note',
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
      apiFn: pageStocktakes,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'name', label: '盘点任务名称', minWidth: 120 },
        { prop: 'locationId', label: '盘点位置范围 ID', minWidth: 120 },
        { prop: 'categoryId', label: '盘点分类范围 ID', minWidth: 120 },
        { prop: 'status', label: '状态', minWidth: 120 },
        { prop: 'managerId', label: '负责人 ID', minWidth: 120 },
        { prop: 'startDate', label: '计划开始日期', minWidth: 120 },
        { prop: 'endDate', label: '计划/实际结束日期', minWidth: 120 },
        { prop: 'note', label: '备注', minWidth: 120 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
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

  const form = ref<StocktakesEntity>({
    id: 0,
    name: '',
    locationId: 0,
    categoryId: 0,
    status: '',
    managerId: 0,
    startDate: '',
    endDate: '',
    note: '',
    createTime: '',
    updateTime: '',
    createBy: 0,
    updateBy: 0
  })

  const formRules = ref({
    id: [
      {
        required: true,
        message: 'Id不能为空',
        trigger: 'blur'
      }
    ],
    name: [
      {
        required: true,
        message: '盘点任务名称不能为空',
        trigger: 'blur'
      }
    ],
    locationId: [
      {
        required: true,
        message: '盘点位置范围 ID不能为空',
        trigger: 'blur'
      }
    ],
    categoryId: [
      {
        required: true,
        message: '盘点分类范围 ID不能为空',
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: '状态不能为空',
        trigger: 'change'
      }
    ],
    managerId: [
      {
        required: true,
        message: '负责人 ID不能为空',
        trigger: 'blur'
      }
    ],
    startDate: [
      {
        required: true,
        message: '计划开始日期不能为空',
        trigger: 'blur'
      }
    ],
    endDate: [
      {
        required: true,
        message: '计划/实际结束日期不能为空',
        trigger: 'blur'
      }
    ],
    note: [
      {
        required: true,
        message: '备注不能为空',
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

  const stocktakesRef = useTemplateRef('stocktakesRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: null,
      name: '',
      locationId: null,
      categoryId: null,
      status: '',
      managerId: null,
      startDate: '',
      endDate: '',
      note: '',
      createTime: '',
      updateTime: '',
      createBy: null,
      updateBy: null
    }
    resetFormRef(stocktakesRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加盘点任务'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: StocktakesEntity) => {
    reset()
    if (row.id) {
      getStocktakes(row.id).then((data: StocktakesEntity) => {
        form.value = data
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
        if (form.value.id != null) {
          updateStocktakes(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addStocktakes(form.value)
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
      .catch(() => {})
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
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
