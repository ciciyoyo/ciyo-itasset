<template>
  <el-dialog v-model="visible" title="我的申请记录" width="1000px" destroy-on-close :close-on-click-modal="false" append-to-body>
    <div class="py-2">
      <el-table v-loading="loading" :data="requestList" style="width: 100%" border stripe>
        <el-table-column prop="id" label="申请编号" width="160" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="plain" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestedItem" label="申请物品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reason" label="申请原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="approvalNote" label="审批备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="date" label="申请日期" width="120" align="center" />
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { getMyRequests, type AssetRequestsVO } from '@/api/itam/personal'

  // Using defineModel for two-way binding of 'visible' prop
  const visible = defineModel<boolean>('visible', { required: true })

  interface ViewRequestItem {
    id: string
    type: string
    status: string
    statusText: string
    requestedItem: string
    allocatedItem?: string
    date: string
    reason: string
    approvalNote?: string
  }

  const loading = ref(false)
  const requestList = ref<ViewRequestItem[]>([])
  const pageNo = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const fetchData = async () => {
    loading.value = true
    try {
      const res = await getMyRequests({
        current: pageNo.value,
        size: pageSize.value
      })
      total.value = res.total || 0
      requestList.value = (res.records || []).map((req: AssetRequestsVO) => ({
        id: req.requestNo || `REQ-${req.id}`,
        type: getTypeLabel(req.itemType),
        status: req.status,
        statusText: getStatusLabel(req.status),
        requestedItem: req.itemName
          ? `${req.categoryName ? req.categoryName + ' ' : ''}${req.itemName}`
          : `${req.categoryName || getTypeLabel(req.itemType)} (未分配)`,
        allocatedItem: req.allocatedItemName,
        date: req.createTime?.substring(0, 10) || '-',
        reason: req.reason || '无',
        approvalNote: req.approvalNote
      }))
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  watch(visible, (val) => {
    if (val) {
      pageNo.value = 1
      fetchData()
    }
  })

  // Helper functions
  const getTypeLabel = (type: string) => {
    const map: Record<string, string> = {
      device: '设备申请',
      accessory: '配件申请',
      consumable: '耗材申请',
      license: '软件申请',
      service: '服务申请',
      other: '其他申请'
    }
    return map[type] || '资产申请'
  }

  const getStatusLabel = (status: string) => {
    const map: Record<string, string> = {
      pending: '待处理',
      approved: '已批准',
      rejected: '已拒绝',
      canceled: '已取消'
    }
    return map[status] || status
  }

  const getStatusTagType = (status: string) => {
    switch (status) {
      case 'pending':
        return 'warning'
      case 'approved':
        return 'success'
      case 'rejected':
      case 'canceled':
        return 'danger'
      default:
        return 'info'
    }
  }
</script>

<style scoped></style>
