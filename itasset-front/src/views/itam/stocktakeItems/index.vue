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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:stocktakeItems:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:stocktakeItems:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:stocktakeItems:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:stocktakeItems:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:stocktakeItems:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改盘点明细对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="stocktakeItemsRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="关联盘点任务 ID" prop="stocktakeId">
          <el-input v-model="form.stocktakeId" placeholder="请输入关联盘点任务 ID" />
        </el-form-item>
        <el-form-item label="关联资产 ID" prop="assetId">
          <el-input v-model="form.assetId" placeholder="请输入关联资产 ID" />
        </el-form-item>
        <el-form-item label="盘点结果" prop="status"> </el-form-item>
        <el-form-item label="执行盘点的人员" prop="scannedBy">
          <el-input v-model="form.scannedBy" placeholder="请输入执行盘点的人员" />
        </el-form-item>
        <el-form-item label="盘点/扫描时间" prop="scannedAt">
          <el-date-picker
            clearable
            v-model="form.scannedAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择盘点/扫描时间"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="系统记录位置 ID" prop="expectedLocationId">
          <el-input v-model="form.expectedLocationId" placeholder="请输入系统记录位置 ID" />
        </el-form-item>
        <el-form-item label="实际发现位置 ID" prop="actualLocationId">
          <el-input v-model="form.actualLocationId" placeholder="请输入实际发现位置 ID" />
        </el-form-item>
        <el-form-item label="说明/异常备注" prop="note">
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
    addStocktakeItems,
    delStocktakeItems,
    exportStocktakeItems,
    getStocktakeItems,
    pageStocktakeItems,
    StocktakeItemsEntity,
    updateStocktakeItems
  } from '@/api/itam/stocktakeItems'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'

  defineOptions({
    name: 'stocktakeItems'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<StocktakeItemsEntity[]>([])

  const searchForm = ref({
    stocktakeId: undefined,
    assetId: undefined,
    status: undefined,
    scannedBy: undefined,
    scannedAt: undefined,
    expectedLocationId: undefined,
    actualLocationId: undefined,
    note: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '关联盘点任务 ID',
      key: 'stocktakeId',
      type: 'input',
      placeholder: '请输入关联盘点任务 ID',
      clearable: true
    },
    {
      label: '关联资产 ID',
      key: 'assetId',
      type: 'input',
      placeholder: '请输入关联资产 ID',
      clearable: true
    },
    {
      label: '执行盘点的人员',
      key: 'scannedBy',
      type: 'input',
      placeholder: '请输入执行盘点的人员',
      clearable: true
    },
    {
      label: '盘点/扫描时间',
      key: 'scannedAt',
      type: 'date',
      clearable: true
    },
    {
      label: '系统记录位置 ID',
      key: 'expectedLocationId',
      type: 'input',
      placeholder: '请输入系统记录位置 ID',
      clearable: true
    },
    {
      label: '实际发现位置 ID',
      key: 'actualLocationId',
      type: 'input',
      placeholder: '请输入实际发现位置 ID',
      clearable: true
    },
    {
      label: '说明/异常备注',
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
      apiFn: pageStocktakeItems,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'stocktakeId', label: '关联盘点任务 ID', minWidth: 120 },
        { prop: 'assetId', label: '关联资产 ID', minWidth: 120 },
        { prop: 'status', label: '盘点结果', minWidth: 120 },
        { prop: 'scannedBy', label: '执行盘点的人员', minWidth: 120 },
        { prop: 'scannedAt', label: '盘点/扫描时间', minWidth: 120 },
        { prop: 'expectedLocationId', label: '系统记录位置 ID', minWidth: 120 },
        { prop: 'actualLocationId', label: '实际发现位置 ID', minWidth: 120 },
        { prop: 'note', label: '说明/异常备注', minWidth: 120 },
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

  const form = ref<StocktakeItemsEntity>({
    id: 0,
    stocktakeId: 0,
    assetId: 0,
    status: '',
    scannedBy: 0,
    scannedAt: '',
    expectedLocationId: 0,
    actualLocationId: 0,
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
    stocktakeId: [
      {
        required: true,
        message: '关联盘点任务 ID不能为空',
        trigger: 'blur'
      }
    ],
    assetId: [
      {
        required: true,
        message: '关联资产 ID不能为空',
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: '盘点结果不能为空',
        trigger: 'change'
      }
    ],
    scannedBy: [
      {
        required: true,
        message: '执行盘点的人员不能为空',
        trigger: 'blur'
      }
    ],
    scannedAt: [
      {
        required: true,
        message: '盘点/扫描时间不能为空',
        trigger: 'blur'
      }
    ],
    expectedLocationId: [
      {
        required: true,
        message: '系统记录位置 ID不能为空',
        trigger: 'blur'
      }
    ],
    actualLocationId: [
      {
        required: true,
        message: '实际发现位置 ID不能为空',
        trigger: 'blur'
      }
    ],
    note: [
      {
        required: true,
        message: '说明/异常备注不能为空',
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

  const stocktakeItemsRef = useTemplateRef('stocktakeItemsRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: null,
      stocktakeId: null,
      assetId: null,
      status: '',
      scannedBy: null,
      scannedAt: '',
      expectedLocationId: null,
      actualLocationId: null,
      note: '',
      createTime: '',
      updateTime: '',
      createBy: null,
      updateBy: null
    }
    resetFormRef(stocktakeItemsRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加盘点明细'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: StocktakeItemsEntity) => {
    reset()
    if (row.id) {
      getStocktakeItems(row.id).then((data: StocktakeItemsEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改盘点明细'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    stocktakeItemsRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateStocktakeItems(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addStocktakeItems(form.value)
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
      .then(() => delStocktakeItems(ids))
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
          const response = await exportStocktakeItems(searchParams)
          download(response, '盘点明细数据')
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
