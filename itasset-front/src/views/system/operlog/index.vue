<!-- Operlog management page -->
<template>
  <div class="operlog-page art-full-height">
    <div class="flex flex-col h-full">
      <!-- Search bar -->
      <OperlogSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

      <ElCard
        class="art-table-card flex flex-col flex-1 min-h-0"
        shadow="never"
        :style="{ 'margin-top': showSearch ? '12px' : '0' }"
      >
        <!-- Table header -->
        <ArtTableHeader
          v-model:columns="columnChecks"
          v-model:showSearchBar="showSearch"
          :loading="loading"
          @refresh="refreshData"
        >
          <template #left>
            <ElSpace wrap>
              <ElButton
                @click="handleBatchDelete"
                icon="ele-Delete"
                :disabled="!selectedRows.length"
                v-hasPermi="['monitor:operlog:remove']"
                type="danger"
                v-ripple
              >
                {{ t('system.operlog.delete') }}
              </ElButton>
              <ElButton @click="handleClean" icon="ele-Delete" v-hasPermi="['monitor:operlog:remove']" type="danger" v-ripple>
                {{ t('system.operlog.clear') }}
              </ElButton>
              <ElButton
                @click="handleExport"
                icon="ele-Download"
                v-hasPermi="['monitor:operlog:export']"
                :loading="exportLoading"
                v-ripple
              >
                {{ t('system.operlog.export') }}
              </ElButton>
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
            <div class="flex gap-2">
              <el-button link type="primary" @click="handleView(row)" v-hasPermi="['monitor:operlog:query']">
                {{ t('system.operlog.details') }}
              </el-button>
            </div>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Operation log detail dialog -->
    <el-dialog v-model="open" append-to-body :title="t('system.operlog.operationLogDetails')" width="50%">
      <el-form :model="form" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="t('system.operlog.operationModule') + ':'">
              {{ form.title }} / {{ typeFormat(form) }}
            </el-form-item>
            <el-form-item :label="t('system.operlog.loginInfo') + ':'">
              {{ form.operName }} / {{ form.operIp }} / {{ form.operLocation }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('system.operlog.requestAddress') + ':'">
              {{ form.operUrl }}
            </el-form-item>
            <el-form-item :label="t('system.operlog.requestMethodLabel') + ':'">
              {{ form.requestMethod }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('system.operlog.operationMethod') + ':'">
              {{ form.method }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('system.operlog.requestParams') + ':'">
              {{ form.operParam }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="t('system.operlog.responseParams') + ':'">
              {{ form.jsonResult }}
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('system.operlog.operationStatus') + ':'">
              <div v-if="form.status === 0">{{ t('system.operlog.operationStatusNormal') }}</div>
              <div v-else-if="form.status === 1">{{ t('system.operlog.operationStatusFailed') }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('system.operlog.operationTimestamp') + ':'">
              {{ parseTime(form.operTime) }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item v-if="form.status === 1" :label="t('system.operlog.errorMessage') + ':'">
              {{ form.errorMsg }}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">{{ t('system.operlog.close') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { cleanOperlog, delOperlog, exportOperlog, list, OperlogEntity } from '@/api/system/operlog'
  import { getDicts, DictDataEntity } from '@/api/system/dict/data'
  import { download, parseTime } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import OperlogSearch from './modules/operlog-search.vue'

  defineOptions({ name: 'ToolOperlog' })

  // i18n support
  const { t } = useI18n()

  // Selected rows
  const selectedRows = ref<any[]>([])

  // Search form
  const searchForm = ref({
    title: undefined,
    operName: undefined,
    businessType: undefined,
    status: undefined,
    dateRange: undefined
  })

  // Show search bar
  const showSearch = ref(true)

  // Export loading
  const exportLoading = ref(false)

  // Dialog related
  const open = ref(false)
  const form = reactive<OperlogEntity>({})

  // Dictionary options
  const typeOptions = ref<DictDataEntity[]>([])
  const statusOptions = ref<DictDataEntity[]>([])

  // useTable hook
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: list,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value,
        params: {
          orderByColumn: 'id'
        }
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'id',
          label: t('system.operlog.logNumber'),
          minWidth: 100
        },
        {
          prop: 'title',
          label: t('system.operlog.systemModule'),
          minWidth: 100
        },
        {
          prop: 'businessType',
          label: t('system.operlog.operationType'),
          formatter: (row: any) => typeFormat(row)
        },
        {
          prop: 'requestMethod',
          label: t('system.operlog.requestMethod')
        },
        {
          prop: 'operName',
          label: t('system.operlog.operator'),
          showOverflowTooltip: true,
          minWidth: 100
        },
        {
          prop: 'operIp',
          label: t('system.operlog.address'),
          showOverflowTooltip: true,
          minWidth: 180
        },
        {
          prop: 'operLocation',
          label: t('system.operlog.location'),
          showOverflowTooltip: true
        },
        {
          prop: 'status',
          label: t('system.operlog.state'),
          formatter: (row: any) => statusFormat(row)
        },
        {
          prop: 'operTime',
          label: t('system.operlog.date'),
          minWidth: 180,
          formatter: (row: any) => parseTime(row?.operTime)
        },
        {
          prop: 'operation',
          label: t('system.operlog.operation'),
          useSlot: true,
          fixed: 'right',
          minWidth: 120
        }
      ]
    }
  })

  // Load dictionary data
  const loadDictData = async () => {
    try {
      const typeResponse = await getDicts('sys_oper_type')
      typeOptions.value = typeResponse || []

      const statusResponse = await getDicts('sys_common_status')
      statusOptions.value = statusResponse || []
    } catch (error) {
      console.error('获取字典数据失败:', error)
    }
  }

  // Format status
  const statusFormat = (row: any) => {
    const dict = statusOptions.value.find((item) => item.dictValue === String(row.status))
    return dict?.dictLabel || row.status
  }

  // Format type
  const typeFormat = (row: any) => {
    const dict = typeOptions.value.find((item) => item.dictValue === String(row.businessType))
    return dict?.dictLabel || t('system.operlog.other')
  }

  // Search handling
  const handleSearch = (params: Record<string, any>) => {
    // Handle dateRange
    if (params.dateRange && params.dateRange.length === 2) {
      params.beginTime = params.dateRange[0]
      params.endTime = params.dateRange[1]
      delete params.dateRange
    }
    Object.assign(searchParams, params)
    refreshData()
  }

  // View details
  const handleView = (row: OperlogEntity) => {
    Object.assign(form, row)
    open.value = true
  }

  // Batch delete
  const handleBatchDelete = () => {
    const ids = selectedRows.value.map((item) => item.id).join(',')
    handleDelete(ids)
  }

  // Delete operlog (batch or single)
  const handleDelete = (ids: any) => {
    ElMessageBox.confirm(
      t('system.operlog.confirmDeleteLogNumber') + `"${ids}"` + t('system.operlog.dataItem'),
      t('system.operlog.warning'),
      {
        confirmButtonText: t('system.operlog.confirm'),
        cancelButtonText: t('system.operlog.cancel'),
        type: 'warning'
      }
    )
      .then(async () => {
        try {
          await delOperlog(ids)
          MessageUtil.success(t('system.operlog.deleteSuccess'))
          await refreshData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      })
      .catch(() => {})
  }

  // Clean all logs
  const handleClean = () => {
    ElMessageBox.confirm(t('system.operlog.confirmClearLogs'), t('system.operlog.warning'), {
      confirmButtonText: t('system.operlog.confirm'),
      cancelButtonText: t('system.operlog.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          await cleanOperlog()
          MessageUtil.success(t('system.operlog.clearSuccess'))
          await refreshData()
        } catch (error) {
          console.error('清空失败:', error)
        }
      })
      .catch(() => {})
  }

  // Export
  const handleExport = () => {
    ElMessageBox.confirm(t('system.operlog.confirmExportLogs'), t('system.operlog.warning'), {
      confirmButtonText: t('system.operlog.confirm'),
      cancelButtonText: t('system.operlog.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          exportLoading.value = true
          const response = await exportOperlog(searchParams)
          download(response, t('system.operlog.operationLogs'))
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  // Table row selection change
  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  onMounted(() => {
    loadDictData()
  })
</script>

<style scoped lang="scss">
  .operlog-page {
    padding: 16px;
  }
</style>
