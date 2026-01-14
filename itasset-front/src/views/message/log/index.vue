<template>
  <div class="log-page art-full-height">
    <div class="flex flex-col h-full">
      <!-- Search bar -->
      <LogSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
                @click="handleDelete"
                icon="ele-Delete"
                type="danger"
                :disabled="!selectedRows.length"
                v-hasPermi="['sys:msg:delete']"
                v-ripple
              >
                {{ $t('common.delete') }}
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
          <template #msgParams="{ row }">
            <el-button link type="primary" @click="handleDesc(row.msgParams)">
              {{ $t('system.noticeLog.details') }}
            </el-button>
          </template>
          <template #content="{ row }">
            <el-button link type="primary" @click="handleDesc(row.content)">
              {{ $t('system.noticeLog.details') }}
            </el-button>
          </template>
          <template #sendTime="{ row }">
            <span>{{ parseTime(row.sendTime) }}</span>
          </template>
          <template #operation="{ row }">
            <el-tooltip :content="$t('common.delete')" placement="top">
              <el-button
                link
                type="danger"
                icon="ele-Delete"
                @click="handleDelete(row)"
                v-hasPermi="['sys:msg:delete']"
              ></el-button>
            </el-tooltip>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Details dialog -->
    <el-dialog append-to-body :title="$t('system.noticeLog.details')" v-model="dialogVisible" width="40%">
      <span>{{ dialogDesc }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisible = false">
            {{ $t('common.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script name="MsgLog" setup lang="ts">
  import { ref } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import { delMessage, listMessage, type MessageLog } from '@/api/system/message'
  import { MessageUtil } from '@/utils/messageUtil'
  import { parseTime } from '@/utils/business'
  import LogSearch from './modules/log-search.vue'

  // i18n support
  const { t } = useI18n()

  // Dialog state
  const dialogVisible = ref(false)
  const dialogDesc = ref('')
  const showSearch = ref(true)

  // Selected rows
  const selectedRows = ref<MessageLog[]>([])

  // Search form
  const searchForm = ref({
    title: undefined,
    receiver: undefined,
    sendStatus: undefined
  })

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
      apiFn: listMessage,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'ID', minWidth: 80 },
        { prop: 'title', label: t('system.noticeLog.messageTitle'), minWidth: 200, showOverflowTooltip: true },
        { prop: 'typeDesc', label: t('system.noticeLog.sendMethod'), minWidth: 110 },
        { prop: 'receiver', label: t('system.noticeLog.receiver'), minWidth: 200 },
        { prop: 'msgParams', label: t('system.noticeLog.parameters'), minWidth: 120, useSlot: true },
        { prop: 'content', label: t('system.noticeLog.pushContent'), minWidth: 120, useSlot: true },
        { prop: 'sendTime', label: t('system.noticeLog.pushTime'), minWidth: 180, useSlot: true },
        { prop: 'sendStatusDesc', label: t('system.noticeLog.pushStatus'), minWidth: 120 },
        { prop: 'sendNum', label: t('system.noticeLog.sendCount'), minWidth: 100 },
        { prop: 'result', label: t('system.noticeLog.pushFailureReason'), minWidth: 120, showOverflowTooltip: true },
        { prop: 'remark', label: t('system.noticeLog.remark'), minWidth: 120 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
          useSlot: true,
          minWidth: 100,
          fixed: 'right'
        }
      ]
    },
    debug: {
      enableLog: true
    }
  })

  // Search handling
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    refreshData()
  }

  // Selection handling
  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  // Delete handling
  const handleDelete = (row?: any) => {
    const ids = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(t('system.noticeLog.confirmDelete') + ids + t('system.noticeLog.dataItem'), t('common.warning'))
      .then(() => delMessage(ids))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.success'))
      })
      .catch(() => {})
  }

  // Details dialog handling
  const handleDesc = (desc: string) => {
    dialogVisible.value = true
    dialogDesc.value = desc
  }
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
