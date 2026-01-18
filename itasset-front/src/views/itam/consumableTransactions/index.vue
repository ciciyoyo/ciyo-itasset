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
              <el-button
                type="primary"
                icon="ele-Plus"
                @click="handleAdd"
                v-ripple
                v-hasPermi="['itam:consumableTransactions:add']"
              >
                {{ $t('common.add') }}
              </el-button>
              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:consumableTransactions:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:consumableTransactions:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:consumableTransactions:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:consumableTransactions:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改耗材出入库明细对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="consumableTransactionsRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="关联耗材 ID" prop="consumableId">
          <el-input v-model="form.consumableId" placeholder="请输入关联耗材 ID" />
        </el-form-item>
        <el-form-item label="操作人 ID" prop="operatorId">
          <el-input v-model="form.operatorId" placeholder="请输入操作人 ID" />
        </el-form-item>
        <el-form-item label="变动类型" prop="actionType"> </el-form-item>
        <el-form-item label="变动数量" prop="quantity">
          <el-input v-model="form.quantity" placeholder="请输入变动数量" />
        </el-form-item>
        <el-form-item label="变动后结存" prop="remainingQuantity">
          <el-input v-model="form.remainingQuantity" placeholder="请输入变动后结存" />
        </el-form-item>
        <el-form-item label="关联对象类型" prop="targetType"> </el-form-item>
        <el-form-item label="关联对象 ID" prop="targetId">
          <el-input v-model="form.targetId" placeholder="请输入关联对象 ID" />
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
    addConsumableTransactions,
    ConsumableTransactionsEntity,
    delConsumableTransactions,
    exportConsumableTransactions,
    getConsumableTransactions,
    pageConsumableTransactions,
    updateConsumableTransactions
  } from '@/api/itam/consumableTransactions'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'

  defineOptions({
    name: 'consumableTransactions'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<ConsumableTransactionsEntity[]>([])

  const searchForm = ref({
    consumableId: undefined,
    operatorId: undefined,
    actionType: undefined,
    quantity: undefined,
    remainingQuantity: undefined,
    targetType: undefined,
    targetId: undefined,
    note: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '关联耗材 ID',
      key: 'consumableId',
      type: 'input',
      placeholder: '请输入关联耗材 ID',
      clearable: true
    },
    {
      label: '操作人 ID',
      key: 'operatorId',
      type: 'input',
      placeholder: '请输入操作人 ID',
      clearable: true
    },
    {
      label: '变动数量',
      key: 'quantity',
      type: 'input',
      placeholder: '请输入变动数量',
      clearable: true
    },
    {
      label: '变动后结存',
      key: 'remainingQuantity',
      type: 'input',
      placeholder: '请输入变动后结存',
      clearable: true
    },
    {
      label: '关联对象 ID',
      key: 'targetId',
      type: 'input',
      placeholder: '请输入关联对象 ID',
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
      apiFn: pageConsumableTransactions,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'consumableId', label: '关联耗材 ID', minWidth: 120 },
        { prop: 'operatorId', label: '操作人 ID', minWidth: 120 },
        { prop: 'actionType', label: '变动类型', minWidth: 120 },
        { prop: 'quantity', label: '变动数量', minWidth: 120 },
        { prop: 'remainingQuantity', label: '变动后结存', minWidth: 120 },
        { prop: 'targetType', label: '关联对象类型', minWidth: 120 },
        { prop: 'targetId', label: '关联对象 ID', minWidth: 120 },
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

  const form = ref<ConsumableTransactionsEntity>({
    id: 0,
    consumableId: 0,
    operatorId: 0,
    actionType: '',
    quantity: 0,
    remainingQuantity: 0,
    targetType: '',
    targetId: 0,
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
    consumableId: [
      {
        required: true,
        message: '关联耗材 ID不能为空',
        trigger: 'blur'
      }
    ],
    operatorId: [
      {
        required: true,
        message: '操作人 ID不能为空',
        trigger: 'blur'
      }
    ],
    actionType: [
      {
        required: true,
        message: '变动类型不能为空',
        trigger: 'change'
      }
    ],
    quantity: [
      {
        required: true,
        message: '变动数量不能为空',
        trigger: 'blur'
      }
    ],
    remainingQuantity: [
      {
        required: true,
        message: '变动后结存不能为空',
        trigger: 'blur'
      }
    ],
    targetType: [
      {
        required: true,
        message: '关联对象类型不能为空',
        trigger: 'change'
      }
    ],
    targetId: [
      {
        required: true,
        message: '关联对象 ID不能为空',
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

  const consumableTransactionsRef = useTemplateRef('consumableTransactionsRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: null,
      consumableId: null,
      operatorId: null,
      actionType: '',
      quantity: null,
      remainingQuantity: null,
      targetType: '',
      targetId: null,
      note: '',
      createTime: '',
      updateTime: '',
      createBy: null,
      updateBy: null
    }
    resetFormRef(consumableTransactionsRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加耗材出入库明细'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: ConsumableTransactionsEntity) => {
    reset()
    if (row.id) {
      getConsumableTransactions(row.id).then((data: ConsumableTransactionsEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改耗材出入库明细'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    consumableTransactionsRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateConsumableTransactions(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addConsumableTransactions(form.value)
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
      .then(() => delConsumableTransactions(ids))
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
          const response = await exportConsumableTransactions(searchParams)
          download(response, '耗材出入库明细数据')
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
