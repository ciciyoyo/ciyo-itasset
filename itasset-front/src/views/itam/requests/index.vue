<template>
  <div class="requests-page art-full-height">
    <div class="flex flex-col h-full">
      <ArtSearchBar
        ref="searchBarRef"
        v-model="searchForm"
        :items="searchFormItems"
        @reset="resetSearchParams"
        @search="handleSearch"
      />

      <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never" style="margin-top: 12px">
        <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
          <template #left>
            <h3 class="font-bold">资产申请管理</h3>
          </template>
        </ArtTableHeader>

        <ArtTable
          class="flex-1"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        >
          <template #itemType="{ row }">
            <el-tag :type="getItemTypeTag(row.itemType)">{{ row.itemTypeDesc }}</el-tag>
          </template>

          <template #status="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusDesc }}</el-tag>
          </template>

          <template #operation="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              link
              type="primary"
              @click="handleApprove(row)"
              v-hasPermi="['itam:request:approve']"
            >
              审批
            </el-button>
            <span v-else class="text-gray-400 text-xs">无需操作</span>
          </template>
        </ArtTable>
      </ElCard>

      <ApproveModal v-model:visible="approveVisible" v-model:requestData="currentRequest" @success="refreshData" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { pageAssetRequests, type AssetRequestVO } from '@/api/itam/requests'
  import ApproveModal from './components/ApproveModal.vue'

  defineOptions({
    name: 'AssetRequests'
  })

  // Status Helpers
  const getStatusType = (status: string) => {
    switch (status) {
      case 'pending':
        return 'warning'
      case 'approved':
        return 'success'
      case 'rejected':
        return 'danger'
      default:
        return 'info'
    }
  }

  // Search
  const searchForm = reactive({
    requestNo: '',
    status: '',
    itemType: ''
  })

  const searchFormItems = computed(() => [
    { label: '申请单号', key: 'requestNo', type: 'input', placeholder: '请输入单号' },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      options: [
        { label: '待审批', value: 'pending' },
        { label: '已通过', value: 'approved' },
        { label: '已驳回', value: 'rejected' }
      ]
    },
    {
      label: '类型',
      key: 'itemType',
      type: 'select',
      options: [
        { label: '设备', value: 'device' },
        { label: '配件', value: 'accessory' },
        { label: '耗材', value: 'consumable' },
        { label: '软件', value: 'license' },
        { label: '服务', value: 'service' }
      ]
    }
  ])

  // Table
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    getData,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: pageAssetRequests,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm
      },
      columnsFactory: () => [
        { prop: 'requestNo', label: '单号', minWidth: 180 },
        { prop: 'userName', label: '申请人', minWidth: 100 },
        { prop: 'itemType', label: '类型', minWidth: 100, useSlot: true },
        { prop: 'categoryName', label: '分类', minWidth: 120 },
        { prop: 'quantity', label: '数量', minWidth: 80 },
        { prop: 'reason', label: '原因', minWidth: 200, showOverflowTooltip: true },
        { prop: 'createTime', label: '申请时间', minWidth: 160 },
        { prop: 'status', label: '状态', minWidth: 100, useSlot: true },
        { prop: 'approverName', label: '审批人', minWidth: 100 },
        { prop: 'operation', label: '操作', width: 120, fixed: 'right', useSlot: true }
      ]
    }
  })

  const searchBarRef = ref()
  const handleSearch = () => {
    Object.assign(searchParams, searchForm)
    getData()
  }

  // Item Type Helpers
  const getItemTypeTag = (type: string) => {
    const map: Record<string, string> = {
      device: '',
      accessory: 'success',
      consumable: 'warning',
      license: 'info',
      service: 'danger'
    }
    return map[type] || ''
  }

  // Approve Logic
  const approveVisible = ref(false)
  const currentRequest = ref<AssetRequestVO>()

  const handleApprove = (row: AssetRequestVO) => {
    currentRequest.value = row
    approveVisible.value = true
  }
</script>

<style scoped>
  .requests-page {
    padding: 16px;
  }
</style>
