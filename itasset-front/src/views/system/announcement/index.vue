<!-- System announcement management page -->
<template>
  <div class="announcement-page art-full-height">
    <div class="flex flex-col h-full">
      <!-- Search bar -->
      <AnnouncementSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
              <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['sys:announcement:save']" v-ripple>
                {{ $t('common.add') }}
              </ElButton>
              <ElButton
                @click="() => handleDelete()"
                icon="ele-Delete"
                type="danger"
                :disabled="!selectedRows.length"
                v-hasPermi="['sys:announcement:delete']"
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
          <template #sendStatus="{ row }">
            <el-tag v-if="row.sendStatus === '1'">{{ $t('system.announcement.unpublished') }}</el-tag>
            <el-tag v-else-if="row.sendStatus === '2'" type="success">{{ $t('system.announcement.published') }}</el-tag>
            <el-tag v-else type="danger">{{ $t('system.announcement.revoked') }}</el-tag>
          </template>
          <template #sendTime="{ row }">
            <span>{{ parseTime(row.sendTime) }}</span>
          </template>
          <template #cancelTime="{ row }">
            <span>{{ parseTime(row.cancelTime) }}</span>
          </template>
          <template #operation="{ row }">
            <div class="flex gap-2">
              <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['sys:announcement:update']">
                {{ $t('common.modify') }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['sys:announcement:delete']">
                {{ $t('common.delete') }}
              </el-button>
              <el-button link type="success" @click="handleRelease(row)" v-hasPermi="['sys:announcement:release']">
                {{ $t('system.announcement.publish') }}
              </el-button>
              <el-button link type="warning" @click="handleRevoke(row)" v-hasPermi="['sys:announcement:revoke']">
                {{ $t('system.announcement.revoke') }}
              </el-button>
            </div>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Add or edit dialog -->
    <el-dialog v-if="open" :title="title" v-model="open" width="50%" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('system.announcement.title')" prop="title">
          <el-input v-model="form.title" :placeholder="$t('system.announcement.enterTitle')" />
        </el-form-item>
        <el-form-item :label="$t('system.announcement.startTime')" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            clearable
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            :placeholder="$t('system.announcement.selectStartTime')"
          />
        </el-form-item>
        <el-form-item :label="$t('system.announcement.endTime')" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            clearable
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            :placeholder="$t('system.announcement.selectEndTime')"
          />
        </el-form-item>
        <el-form-item :label="$t('system.announcement.priority')" prop="priority">
          <el-select v-model="form.priority" :placeholder="$t('system.announcement.selectPriority')">
            <el-option :label="$t('system.announcement.low')" :value="'L'" />
            <el-option :label="$t('system.announcement.medium')" :value="'M'" />
            <el-option :label="$t('system.announcement.high')" :value="'H'" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('system.announcement.notificationScope')" prop="msgType">
          <el-select v-model="form.msgType" :placeholder="$t('system.announcement.selectNotificationScope')">
            <el-option :label="$t('system.announcement.specifiedUsers')" :value="'1'" />
            <el-option :label="$t('system.announcement.allUsers')" :value="'2'" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.msgType == '1'" :label="$t('system.announcement.selectUsers')" prop="userIdList">
          <el-select
            v-model="form.userIdList"
            class="width-full"
            multiple
            :placeholder="$t('system.announcement.selectUsers')"
            @remove-tag="handleRemoveUserTag"
            @click="handleOpenUserTable"
          >
            <el-option
              v-for="item in form.userSelectList"
              :key="item.id"
              :disabled="true"
              :label="item.nickName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('system.announcement.summary')" prop="msgAbstract">
          <el-input v-model="form.msgAbstract" type="textarea" :placeholder="$t('system.announcement.summaryPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('system.announcement.content')" prop="msgContent">
          <tinymce v-model:value="form.msgContent" :height="192" :placeholder="$t('system.announcement.summaryPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm">{{ $t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- User choose table -->
    <user-choose-table ref="userChooseTable" v-model:value="form.userSelectList" @submit="handleUserTableSubmit" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue'
  import { ElMessageBox, FormInstance } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useTable } from '@/hooks/core/useTable'
  import {
    addAnnouncement,
    delAnnouncement,
    getAnnouncement,
    listAnnouncement,
    releaseAnnouncement,
    revokeAnnouncement,
    updateAnnouncement,
    type AnnouncementForm,
    type AnnouncementItem,
    type User
  } from '@/api/system/announcement'
  import UserChooseTable from '@/views/system/user/chooseTable.vue'
  import { parseTime } from '@/utils/business'
  import Tinymce from '@/components/business/tinymce/index.vue'
  import { MessageUtil } from '@/utils/messageUtil'
  import AnnouncementSearch from './modules/announcement-search.vue'

  // i18n support
  const { t } = useI18n()

  // Dialog state
  const title = ref<string>('')
  const open = ref<boolean>(false)
  const showSearch = ref<boolean>(true)

  // Selected rows
  const selectedRows = ref<AnnouncementItem[]>([])

  // Search form
  const searchForm = ref({
    title: null as string | null
  })

  // Form data
  const form = reactive<AnnouncementForm>({
    priority: 'L',
    msgCategory: '1',
    msgType: '1',
    userIdList: [],
    userSelectList: []
  })

  // Form validation rules
  const validateEndTime = (rule: any, value: any, callback: any) => {
    if (!value) {
      callback(new Error(t('system.announcement.selectEndTime')))
    } else {
      if (!form.startTime) {
        callback(new Error(t('system.announcement.selectStartTime')))
      } else if (Date.parse(form.startTime) >= Date.parse(value)) {
        callback(new Error(t('system.announcement.endTimeGreaterStartTime')))
      } else {
        callback()
      }
    }
  }

  const rules = reactive({
    title: [{ required: true, message: t('system.announcement.titleRequired'), trigger: 'blur' }],
    endTime: [
      { required: true, message: t('system.announcement.endTimeRequired'), trigger: 'blur' },
      { required: true, validator: validateEndTime, trigger: 'blur' }
    ],
    startTime: [{ required: true, message: t('system.announcement.startTimeRequired'), trigger: 'blur' }],
    msgType: [
      {
        required: true,
        message: t('system.announcement.notificationScopeRequired'),
        trigger: 'blur'
      }
    ],
    noticeContent: [{ required: true, message: t('system.announcement.summaryPlaceholder'), trigger: 'blur' }],
    userIdList: [{ required: true, message: t('system.announcement.selectNotificationUsers'), trigger: 'blur' }]
  })

  // Template refs
  const formRef = ref<FormInstance | null>(null)
  const userChooseTable = ref()

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
      apiFn: listAnnouncement,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'ID', minWidth: 80 },
        {
          prop: 'title',
          label: t('system.announcement.title'),
          minWidth: 200,
          showOverflowTooltip: true
        },
        { prop: 'sender', label: t('system.announcement.publisher'), minWidth: 120 },
        { prop: 'priorityDesc', label: t('system.announcement.priority'), minWidth: 100 },
        { prop: 'msgTypeDesc', label: t('system.announcement.notificationScope'), minWidth: 155 },
        {
          prop: 'sendStatus',
          label: t('system.announcement.publishStatus'),
          minWidth: 120,
          useSlot: true
        },
        {
          prop: 'sendTime',
          label: t('system.announcement.publishTime'),
          minWidth: 180,
          useSlot: true
        },
        {
          prop: 'cancelTime',
          label: t('system.announcement.revokeTime'),
          minWidth: 180,
          useSlot: true
        },
        {
          prop: 'msgAbstract',
          label: t('system.announcement.summary'),
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: t('system.announcement.operation'),
          useSlot: true,
          minWidth: 280,
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

  // Dialog methods
  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    Object.assign(form, {
      id: null,
      title: null,
      msgContent: null,
      startTime: null,
      endTime: null,
      priority: 'L',
      msgCategory: '1',
      msgType: '1',
      sendTime: null,
      userIds: null,
      msgAbstract: null,
      userIdList: [],
      userSelectList: []
    })
    formRef.value?.resetFields()
  }

  // CRUD operations
  const handleAdd = () => {
    reset()
    open.value = true
    title.value = t('system.announcement.addSystemAnnouncement')
  }

  const handleUpdate = async (row?: AnnouncementItem) => {
    reset()
    const id = row?.id || selectedRows.value[0]?.id
    try {
      const res = await getAnnouncement(id)
      Object.assign(form, res)
      if (res.userList) {
        form.userSelectList = res.userList
        form.userIdList = res!.userList!.map((item: User) => item.id)
      }
      open.value = true
      title.value = t('system.announcement.modifySystemAnnouncement')
    } catch (error) {
      console.error('获取公告详情失败:', error)
    }
  }

  const submitForm = () => {
    if (!formRef.value) return

    formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          if (form.userIdList.length > 0) {
            form.userIds = form.userIdList.join(',')
          } else {
            form.userIds = ''
          }

          if (form.id != null) {
            await updateAnnouncement(form)
            MessageUtil.success(t('system.announcement.modifySuccess'))
          } else {
            await addAnnouncement(form)
            MessageUtil.success(t('system.announcement.addSuccess'))
          }
          open.value = false
          refreshData()
        } catch (error) {
          console.error('提交表单失败:', error)
        }
      }
    })
  }

  const handleDelete = async (row?: AnnouncementItem) => {
    const deleteIds = row?.id || selectedRows.value.map((item) => item.id)
    try {
      await ElMessageBox.confirm(
        t('system.announcement.confirmDelete') + '"' + deleteIds + '"' + t('system.announcement.dataItem') + '?',
        t('common.warning'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )

      await delAnnouncement(deleteIds)
      refreshData()
      MessageUtil.success(t('system.announcement.deleteSuccess'))
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除公告失败:', error)
      }
    }
  }

  // User selection
  const handleUserTableSubmit = (val: User[]) => {
    form.userSelectList = val
    form.userIdList = val.map((item) => item.id)
  }

  const handleRemoveUserTag = (val: string | number) => {
    form.userSelectList = form.userSelectList.filter((item) => item.id !== val)
  }

  const handleOpenUserTable = () => {
    if (userChooseTable.value) {
      userChooseTable.value.showDialog(form.userSelectList || [])
    }
  }

  // Publish and revoke
  const handleRelease = async (row: AnnouncementItem) => {
    try {
      await ElMessageBox.confirm(t('system.announcement.confirmRelease') + '"' + row.title + '"?', t('common.warning'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })

      await releaseAnnouncement(row.id)
      refreshData()
      MessageUtil.success(t('common.saveSuccess'))
    } catch (error) {
      if (error !== 'cancel') {
        console.error('发布公告失败:', error)
      }
    }
  }

  const handleRevoke = async (row: AnnouncementItem) => {
    try {
      await ElMessageBox.confirm(t('system.announcement.confirmRevoke') + '"' + row.title + '"?', t('common.warning'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })

      await revokeAnnouncement(row.id)
      refreshData()
      MessageUtil.success(t('common.saveSuccess'))
    } catch (error) {
      if (error !== 'cancel') {
        console.error('撤销公告失败:', error)
      }
    }
  }
</script>

<style scoped lang="scss">
  .announcement-page {
    padding: 16px;
  }
</style>
