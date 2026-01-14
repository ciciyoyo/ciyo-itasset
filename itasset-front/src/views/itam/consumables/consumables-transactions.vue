<template>
  <div class="transactions-page art-full-height">
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
        </ArtTableHeader>

        <!-- Table -->
        <ArtTable
          class="flex-1"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        >
          <template #actionType="{ row }">
            <el-tag v-if="row.actionType === 'STOCK_IN'" type="success">入库</el-tag>
            <el-tag v-else-if="row.actionType === 'STOCK_OUT'" type="warning">出库</el-tag>
            <el-tag v-else>{{ row.actionTypeDesc || row.actionType }}</el-tag>
          </template>
          <template #operator="{ row }">
            <span v-if="row.actionType === 'STOCK_OUT'">
              {{ row.targetName || '-' }}
            </span>
            <span v-else>
              {{ row.operatorName || '-' }}
            </span>
          </template>
        </ArtTable>
      </ElCard>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { pageConsumableTransactions } from '@/api/itam/consumables'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'

  defineOptions({
    name: 'ConsumablesTransactions'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const searchForm = ref({
    consumableId: undefined,
    actionType: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '耗材ID',
      key: 'consumableId',
      type: 'input',
      placeholder: '请输入耗材ID',
      clearable: true
    },
    {
      label: '操作类型',
      key: 'actionType',
      type: 'select',
      placeholder: '请选择操作类型',
      clearable: true,
      options: [
        { label: '入库', value: 'STOCK_IN' },
        { label: '出库', value: 'STOCK_OUT' }
      ]
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
        { prop: 'id', label: 'ID', minWidth: 80 },
        { prop: 'consumableName', label: '耗材名称', minWidth: 150 },
        {
          prop: 'actionType',
          label: '操作类型',
          minWidth: 100,
          useSlot: true
        },
        { prop: 'quantity', label: '数量', minWidth: 100 },
        { prop: 'remainingQuantity', label: '剩余数量', minWidth: 100 },
        {
          prop: 'operator',
          label: '操作人/领取人',
          minWidth: 120,
          useSlot: true
        },
        { prop: 'note', label: '备注', minWidth: 150, showOverflowTooltip: true },
        { prop: 'createTime', label: '操作时间', minWidth: 160 }
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
</script>

<style scoped lang="scss">
  .transactions-page {
    padding: 16px;
  }
</style>
