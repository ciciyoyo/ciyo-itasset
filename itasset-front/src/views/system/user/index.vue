<!-- User management page -->
<template>
  <div class="user-page art-full-height">
    <div class="flex gap-5 h-full max-md:flex-col max-md:h-auto">
      <!-- Department tree -->
      <div class="flex-shrink-0 w-64 h-full max-md:w-full max-md:h-auto max-md:mb-5">
        <el-card shadow="never" class="art-card-xs flex flex-col h-full mt-0">
          <div class="head-container">
            <el-input
              v-model="deptName"
              clearable
              :placeholder="t('system.userManagement.selectDept')"
              prefix-icon="ele-Search"
              style="margin-bottom: 10px"
            />
          </div>
          <el-scrollbar class="flex-1 min-h-0">
            <el-tree
              ref="treeRef"
              :data="deptOptions"
              :props="defaultProps"
              node-key="id"
              :default-expanded-keys="defaultExpandedKeys"
              :filter-node-method="filterMethod"
              highlight-current
              @node-click="handleNodeClick"
            />
          </el-scrollbar>
        </el-card>
      </div>

      <!-- User list -->
      <div class="flex flex-col flex-grow min-w-0">
        <!-- Search bar -->
        <UserSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
                <ElButton @click="showDialog('add')" icon="ele-Plus" type="primary" v-hasPermi="['system:user:add']" v-ripple>
                  {{ t('system.userManagement.addUser') }}
                </ElButton>
                <ElButton @click="handleImport" type="primary" icon="ele-Upload" v-hasPermi="['system:user:import']" v-ripple>
                  {{ t('common.import') }}
                </ElButton>
                <ElButton
                  @click="handleExport"
                  icon="ele-Download"
                  v-hasPermi="['system:user:export']"
                  :loading="exportLoading"
                  v-ripple
                >
                  {{ t('common.export') }}
                </ElButton>
                <ElButton
                  @click="handleBatchDelete"
                  icon="ele-Delete"
                  :disabled="!selectedRows.length"
                  v-hasPermi="['system:user:remove']"
                  type="danger"
                  v-ripple
                >
                  {{ t('common.delete') }}
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
                <el-button
                  link
                  type="primary"
                  @click="showDialog('edit', row)"
                  v-hasPermi="['system:user:edit']"
                  v-if="row.id !== 1"
                >
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  link
                  type="primary"
                  @click="handleResetPwd(row)"
                  v-hasPermi="['system:user:resetPwd']"
                  v-if="row.id !== 1"
                >
                  {{ t('system.userCenter.resetPassword') }}
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click="handleDeleteSingle(row)"
                  v-hasPermi="['system:user:remove']"
                  v-if="row.id !== 1"
                >
                  {{ t('common.delete') }}
                </el-button>
              </div>
            </template>
          </ArtTable>
        </ElCard>
      </div>
    </div>

    <!-- User dialog -->
    <UserDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :user-data="currentUserData"
      @submit="handleDialogSubmit"
      :deptOptions="deptOptions"
    />

    <!-- User import dialog -->
    <el-dialog :title="t('common.import')" v-model="upload.open" append-to-body width="400px">
      <el-upload
        ref="uploadRef"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :auto-upload="false"
        :disabled="upload.isUploading"
        :headers="upload.headers"
        :limit="1"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        accept=".xlsx, .xls"
        drag
      >
        <el-icon :size="30">
          <ele-Upload />
        </el-icon>
        <div class="el-upload__text">{{ t('common.dragUploadText') }}</div>
        <template #tip>
          <div class="el-upload__tip">
            <el-checkbox v-model="upload.updateSupport" />
            {{ t('system.userManagement.updateSupport') }}
            <el-link type="primary" @click="handleImportTemplate">{{ t('common.downloadTemplate') }}</el-link>
          </div>
          <div class="el-upload__tip"> {{ t('system.userManagement.uploadTip') }} </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">{{ t('common.confirm') }}</el-button>
          <el-button @click="upload.open = false">{{ t('common.cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
  import type { UploadInstance } from 'element-plus'
  import { ElMessageBox, ElTree, ElSwitch, ElButton } from 'element-plus'
  import { useWindowSize } from '@vueuse/core'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'
  import { changeUserStatus, delUser, exportUser, importTemplate, pageUser, resetUserPwd } from '@/api/system/user'
  import { getToken } from '@/utils/auth'
  import { DeptPostEntity, treeselect } from '@/api/system/dept'
  import { download, parseTime } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import { DialogType } from '@/types'
  import { listRole, SysRoleEntity } from '@/api/system/role'
  import { DictDataEntity, getDicts } from '@/api/system/dict/data'
  import { getConfigValue } from '@/api/system/config'

  defineOptions({ name: 'UserManagement' })

  // i18n support
  const { t } = useI18n()

  // Dialog related
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<any>({})

  // Selected rows
  const selectedRows = ref<any[]>([])

  // Search form
  const searchForm = ref({
    userName: undefined,
    nickName: undefined,
    phonenumber: undefined,
    email: undefined,
    status: undefined,
    dateRange: undefined,
    deptId: undefined,
    roleIds: undefined
  })

  // Department tree related
  const deptName = ref<string>()
  const deptOptions = ref<any[]>([])
  const defaultExpandedKeys = ref<string[]>([])
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const { height: windowHeight } = useWindowSize()

  const treeHeight = computed(() => {
    return Math.max(windowHeight.value - 200, 400)
  })

  const defaultProps = {
    children: 'children',
    label: 'label',
    value: 'id'
  }

  // Watch department name changes, filter tree nodes
  watch(deptName, (val) => {
    if (treeRef.value) {
      treeRef.value.filter(val ?? '')
    }
  })

  // Filter method
  const filterMethod = (value: string, data: any) => {
    if (!value) return true
    return data.label.toLowerCase().includes(value.toLowerCase())
  }

  // Click tree node
  const handleNodeClick = (data: any) => {
    searchForm.value.deptId = data.id
    handleSearch(searchForm.value)
  }

  // Export related
  const exportLoading = ref(false)

  // Upload related
  const upload = ref({
    open: false,
    isUploading: false,
    updateSupport: 0,
    headers: { Authorization: 'Bearer ' + getToken() },
    url: import.meta.env.VITE_BASE_URL + '/system/user/importData'
  })
  const uploadRef = ref<UploadInstance>()

  // Show search bar
  const showSearch = ref(true)

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
      apiFn: pageUser,
      apiParams: {
        current: 1,
        size: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: t('system.userManagement.userId'), minWidth: 180 },
        { prop: 'userName', label: t('system.userManagement.username'), minWidth: 280 },
        { prop: 'nickName', label: t('system.userManagement.nickName'), minWidth: 180 },
        {
          prop: 'dept',
          label: t('system.userManagement.dept'),
          minWidth: 180,
          formatter: (row) => row?.dept?.deptName || '-'
        },
        { prop: 'phonenumber', label: t('system.userManagement.phoneNumber'), minWidth: 210 },
        { prop: 'email', label: t('system.userManagement.email'), minWidth: 210 },
        {
          prop: 'status',
          label: t('system.userManagement.status'),
          minWidth: 180,
          formatter: (row) =>
            h(ElSwitch, {
              modelValue: row.status === '0',
              activeValue: true,
              inactiveValue: false,
              onChange: () => handleStatusChange(row)
            })
        },
        {
          prop: 'createTime',
          label: t('common.createTime'),
          minWidth: 180,
          formatter: (row) => parseTime(row?.createTime)
        },
        {
          prop: 'operation',
          label: t('common.operation'),
          useSlot: true,
          minWidth: 180,
          fixed: 'right'
        }
      ]
    }
  })

  // Load department tree data
  const getTreeselect = async () => {
    try {
      const response = await treeselect()
      deptOptions.value = response || []
      // Set default to expand first level nodes
      const getFirstLevelKeys = (nodes: any[]): string[] => {
        const keys: string[] = []
        nodes.forEach((node) => {
          keys.push(node.id)
        })
        return keys
      }
      defaultExpandedKeys.value = getFirstLevelKeys(response || [])
    } catch (error) {
      console.error('获取部门树失败:', error)
    }
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

  // Status change
  const handleStatusChange = (row: any) => {
    const text = row.status === '0' ? t('common.enable') : t('common.disable')
    const newStatus = row.status === '0' ? '1' : '0'

    ElMessageBox.confirm(`确认要${text}${row.userName}用户吗？`, t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          await changeUserStatus(row.id, newStatus)
          row.status = newStatus
          MessageUtil.success(text + '成功')
        } catch (error) {
          console.error('修改状态失败:', error)
        }
      })
      .catch(() => {})
  }

  // Show dialog
  const showDialog = (type: DialogType, row?: any): void => {
    dialogType.value = type
    currentUserData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  // Dialog submit
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentUserData.value = {}
      await refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  // Reset password
  const handleResetPwd = (row: any) => {
    ElMessageBox.prompt(t('system.userManagement.enterNewPassword', { username: row.userName }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      closeOnClickModal: false,
      inputPattern: /^.{5,20}$/,
      inputErrorMessage: t('system.userManagement.passwordLength')
    })
      .then(async ({ value }) => {
        try {
          await resetUserPwd(row.id, value)
          MessageUtil.success(`重置密码成功，新密码为：${value}`)
        } catch (error) {
          console.error('重置密码失败:', error)
        }
      })
      .catch(() => {})
  }

  // Delete single user
  const handleDeleteSingle = (row: any) => {
    handleDelete(row.id)
  }

  // Batch delete
  const handleBatchDelete = () => {
    const userIds = selectedRows.value.map((item) => item.id).join(',')
    handleDelete(userIds)
  }

  // Delete user (batch or single)
  const handleDelete = (userIds: any) => {
    ElMessageBox.confirm(t('common.deleteConfirm'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          await delUser(userIds)
          MessageUtil.success('删除成功')
          await refreshData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      })
      .catch(() => {})
  }

  // Export
  const handleExport = () => {
    ElMessageBox.confirm(t('common.exportConfirm'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          exportLoading.value = true
          const response = await exportUser(searchParams)
          download(response, '用户数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  // Import
  const handleImport = () => {
    upload.value.open = true
  }

  // Download template
  const handleImportTemplate = async () => {
    try {
      const response = await importTemplate()
      download(response, '用户导入模板')
    } catch (error) {
      console.error('下载模板失败:', error)
    }
  }

  // File upload in progress
  const handleFileUploadProgress = () => {
    upload.value.isUploading = true
  }

  // File upload success
  const handleFileSuccess = (response: any) => {
    upload.value.open = false
    upload.value.isUploading = false
    uploadRef.value?.clearFiles()
    ElMessageBox.alert(response.msg, '导入结果', { dangerouslyUseHTMLString: true })
    refreshData()
  }

  // Submit upload file
  const submitFileForm = () => {
    uploadRef.value?.submit()
  }

  // Table row selection change
  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  onMounted(() => {
    getTreeselect()
  })
</script>

<style scoped lang="scss">
  .user-page {
    padding: 16px;
  }

  .tree-container {
    :deep(.el-tree-v2) {
      --el-tree-node-hover-bg-color: var(--el-fill-color-light);
    }
  }
</style>
