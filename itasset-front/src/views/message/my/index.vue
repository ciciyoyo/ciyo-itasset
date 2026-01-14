<!-- My messages page -->
<template>
  <div class="my-message-page art-full-height">
    <div class="flex flex-col h-full">
      <!-- Search bar -->
      <MyMessageSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
              <ElButton @click="handleAllRead" icon="ele-View" type="primary" v-ripple>
                {{ $t('system.myMsg.markAllRead') }}
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
          <template #sendTime="{ row }">
            <span>{{ parseTime(row.sendTime) }}</span>
          </template>
          <template #readFlag="{ row }">
            <el-tag v-if="row.readFlag" type="success">
              {{ $t('system.myMsg.read') }}
            </el-tag>
            <el-tag v-else type="danger">
              {{ $t('system.myMsg.unread') }}
            </el-tag>
          </template>
          <template #operation="{ row }">
            <div class="flex gap-2">
              <el-button link type="primary" @click="handleView(row)">
                {{ $t('system.myMsg.view') }}
              </el-button>
            </div>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Message detail dialog -->
    <el-dialog v-if="showOpen" :title="$t('system.myMsg.notificationDetails')" v-model="showOpen" width="800px" append-to-body>
      <iframe :src="showDetailUrl" class="detail-iframe" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showOpen = false">{{ $t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { onBeforeMount, onMounted, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    getMyAnnouncementSend,
    Message,
    readAllAnnouncementSend,
    readAnnouncementSend
  } from '@/api/system/announcement'
  import { addBaseUrl, getTokenUrl } from '@/utils/auth'
  import mittBus from '@/utils/sys/mittBus'
  import { parseTime } from '@/utils/business'
  import MyMessageSearch from './modules/my-message-search.vue'

  // i18n support
  const { t } = useI18n()

  // Dialog state
  const showOpen = ref(false)
  const showDetailUrl = ref('')
  const showSearch = ref(true)

  // Selected rows
  const selectedRows = ref<Message[]>([])

  // Search form
  const searchForm = ref({
    title: null as string | null
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
      apiFn: getMyAnnouncementSend,
      apiParams: {
        current: 1,
        size: 10,
        delFlag: false,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'ID', minWidth: 80 },
        { prop: 'title', label: t('system.myMsg.title'), minWidth: 400, showOverflowTooltip: true },
        { prop: 'sender', label: t('system.myMsg.publisher'), minWidth: 120 },
        { prop: 'priorityDesc', label: t('system.myMsg.priority'), minWidth: 100 },
        { prop: 'msgCategoryDesc', label: t('system.myMsg.messageType'), minWidth: 120 },
        { prop: 'sendTime', label: t('system.myMsg.publishTime'), minWidth: 180, useSlot: true },
        { prop: 'readFlag', label: t('system.myMsg.readStatus'), minWidth: 120, useSlot: true },
        {
          prop: 'operation',
          label: t('system.myMsg.operation'),
          useSlot: true,
          minWidth: 150,
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

  // View message
  const handleView = async (row: Message) => {
    if (!row.readFlag && row.anntId) {
      await readAnnouncementSend(row.anntId as number)
    }

    row.readFlag = true
    if (row.openType && row.openType === 'url') {
      window.open(addBaseUrl(row.openPage || '') + '&msgOpen=true', '_blank')
    } else {
      showOpen.value = true
      showDetailUrl.value = getTokenUrl('sys/announce/show/' + row.anntId)
    }
  }

  // Mark all as read
  const handleAllRead = () => {
    ElMessageBox.confirm(t('system.myMsg.confirmMarkAllRead'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        await readAllAnnouncementSend()
        // Emit event to update notification badge
        mittBus.emit('sysMsgNotice', {})
        await refreshData()
      })
      .catch(() => {})
  }

  // Handle route query parameter
  const route = useRoute()
  onBeforeMount(() => {
    if (route.query.msgId) {
      handleView({
        msgContent: '',
        anntId: Number(route.query.msgId)
      })
    }
  })

  // Lifecycle
  onMounted(() => {
    refreshData()
  })

  defineEmits<{
    (e: 'sysMsgNotice'): void
  }>()
</script>

<style scoped lang="scss">
  .my-message-page {
    padding: 16px;
  }

  .detail-iframe {
    border: 0;
    width: 100%;
    min-height: 600px;
  }
</style>
