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
              <el-button v-hasPermi="['itam:failures:export']" icon="ele-Download" v-ripple @click="handleExport">
                {{ $t('common.export') }}
              </el-button>
            </ElSpace>
          </template>
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
          @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange">
          <template #statusDesc="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">{{ row.statusDesc }}</el-tag>
          </template>
          <template #operation="{ row }">
            <el-button v-if="row.status !== FailureStatus.RESOLVED" link type="primary" @click="handleResolve(row)">
              解决
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <GeneralResolveFailureModal ref="resolveModalRef" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useTemplateRef } from 'vue'
import {
  exportFailures,
  FailuresEntity,
  FailuresVO,
  pageFailures
} from '@/api/itam/failures'
import GeneralResolveFailureModal from './components/GeneralResolveFailureModal.vue'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { download } from '@/utils/business'
import { useI18n } from 'vue-i18n'
import { AssetType, FailureStatus } from '@/api/itam/enums'

defineOptions({
  name: 'failures'
})

const { t } = useI18n()

const showSearch = ref(true)

const selectedRows = ref<FailuresEntity[]>([])

const searchForm = ref({
  targetType: undefined,
  targetName: undefined,
  failureName: undefined,
  status: undefined
})

// 表单配置
const searchFormItems = computed(() => [
  {
    label: '故障名称',
    key: 'failureName',
    type: 'input',
    placeholder: '请输入故障名称',
    clearable: true
  },
  {
    label: '关联项目',
    key: 'targetName',
    type: 'input',
    placeholder: '请输入项目名称',
    clearable: true
  },
  {
    label: '关联类型',
    key: 'targetType',
    type: 'select',
    options: [
      { label: '服务', value: AssetType.SERVICE },
      { label: '设备', value: AssetType.DEVICE },
      { label: '配件', value: AssetType.ACCESSORY }
    ],
    placeholder: '请选择类型',
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
    apiFn: pageFailures,
    apiParams: {
      current: 1,
      size: 10,
      ...searchForm.value
    },
    columnsFactory: () => [
      { type: 'selection' },
      { prop: 'targetTypeDesc', label: '关联类型', minWidth: 100 },
      { prop: 'targetName', label: '关联项目', minWidth: 150 },
      { prop: 'failureName', label: '故障名称', minWidth: 150 },
      { prop: 'failureDescription', label: '故障描述', minWidth: 200 },
      { prop: 'failureDate', label: '上报时间', minWidth: 160 },
      { prop: 'statusDesc', label: '状态', minWidth: 100, useSlot: true },
      { prop: 'reportedByName', label: '报告人', minWidth: 120 },
      { prop: 'resolvedByName', label: '修复人', minWidth: 120 },
      { prop: 'resolvedDate', label: '修复日期', minWidth: 160 },
      { prop: 'notes', label: '备注', minWidth: 150 },
      { prop: 'operation', label: '操作', width: 100, useSlot: true, fixed: 'right' }
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

const resolveModalRef = ref()

const handleResolve = (row: FailuresVO) => {
  resolveModalRef.value.open(row)
}

const getStatusType = (status: number | string) => {
  const statusMap: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    [FailureStatus.PENDING]: 'danger', // 待处理
    [FailureStatus.PROCESSING]: 'warning', // 处理中
    [FailureStatus.RESOLVED]: 'success', // 已解决
    [FailureStatus.SCRAPPED]: 'info' // 已报废
  }
  return statusMap[String(status)] || 'info'
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
        const response = await exportFailures(searchParams)
        download(response, '故障数据')
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
