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
              <el-button v-hasPermi="['itam:offering:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button type="primary" text @click="handleUnbind(row)"> 解除关联 </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { unbindAsset } from '@/api/itam/offering'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'

  import { download } from '@/utils/business'
  import { useI18n } from 'vue-i18n'

  import { AllocationsVO, exportAllocations, pageAllocations } from '@/api/itam/allocations'
  import { AssetType } from '@/api/itam/enums'

  defineOptions({
    name: 'offering'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<AllocationsVO[]>([])

  const searchForm = ref({
    id: undefined,
    itemType: AssetType.SERVICE,
    itemId: undefined,
    ownerType: undefined,
    ownerId: undefined,
    quantity: undefined,
    status: 'active',
    assignDate: undefined,
    returnDate: undefined,
    note: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '服务名称',
      key: 'itemName',
      type: 'input',
      placeholder: '请输入服务名称',
      clearable: true
    },
    {
      label: '设备名称',
      key: 'ownerName',
      type: 'input',
      placeholder: '请输入设备名称',
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
      apiFn: pageAllocations,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'itemName', label: '服务名称', minWidth: 150 },
        { prop: 'ownerName', label: '关联设备', minWidth: 120 },
        { prop: 'assignDate', label: '关联时间', minWidth: 160 },
        { prop: 'operation', label: '操作', width: 100, useSlot: true, minWidth: 280, fixed: 'right' }
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

  const handleUnbind = (row: any) => {
    ElMessageBox.confirm('确认解除该服务的关联吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await unbindAsset({ id: row.itemId })
        ElMessage.success('解除关联成功')
        getData()
      } catch (e) {
        console.error(e)
      }
    })
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
          const response = await exportAllocations(searchParams)
          download(response, '服务数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss"></style>
