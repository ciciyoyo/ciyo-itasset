<!-- Post management page -->
<template>
  <div class="app-container">
    <!-- Search bar -->
    <PostSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard
      class="art-table-card flex flex-col flex-1 min-h-0"
      shadow="never"
      :style="{ 'margin-top': showSearch ? '12px' : '0' }"
    >
      <!-- Table header -->
      <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:post:add']" v-ripple>
              {{ t('system.position.add') }}
            </ElButton>
            <ElButton
              @click="handleExport"
              icon="ele-Download"
              v-hasPermi="['system:post:export']"
              :loading="exportLoading"
              v-ripple
            >
              {{ t('system.position.export') }}
            </ElButton>
            <ElButton
              @click="handleBatchDelete"
              icon="ele-Delete"
              :disabled="!selectedRows.length"
              v-hasPermi="['system:post:remove']"
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
        <template #leaderPost="{ row }">
          <el-tag v-if="row.leaderPost === 1" type="success">
            {{ t('system.position.yes') }}
          </el-tag>
          <el-tag v-else type="danger">
            {{ t('system.position.no') }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag v-if="row.status === '0'" type="success">
            {{ getStatusLabel(row.status) }}
          </el-tag>
          <el-tag v-else type="danger">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:post:edit']">
              {{ t('common.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDeleteSingle(row)" v-hasPermi="['system:post:remove']">
              {{ t('common.delete') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- Add or edit post dialog -->
    <el-dialog :title="title" v-model="open" append-to-body width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="135px">
        <el-form-item :label="t('system.position.name')" prop="postName">
          <el-input v-model="form.postName" :placeholder="t('system.position.enterName')" />
        </el-form-item>
        <el-form-item :label="t('system.position.code')" prop="postCode">
          <el-input v-model="form.postCode" :placeholder="t('system.position.enterCode')" />
        </el-form-item>
        <el-form-item :label="t('system.position.managerPosition')" prop="leaderPost">
          <el-switch v-model="leaderPostBool" @change="(val) => (form.leaderPost = val ? 1 : 0)" />
        </el-form-item>
        <el-form-item :label="t('system.position.positionStatus')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('system.position.remark')" prop="remark">
          <el-input v-model="form.remark" :placeholder="t('system.position.enterContent')" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm">
            {{ t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, reactive, ref } from 'vue'
  import type { FormInstance } from 'element-plus'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'
  import {
    addPost,
    delPost,
    exportPost,
    getPost,
    listPost,
    updatePost,
    type SysPostEntity,
    type PostQueryParams
  } from '@/api/system/post'
  import { DictDataEntity, getDicts } from '@/api/system/dict/data'
  import { download, parseTime } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import PostSearch from './modules/post-search.vue'

  defineOptions({ name: 'PostManagement' })

  // i18n support
  const { t } = useI18n()

  // Dialog related
  const title = ref('')
  const open = ref(false)
  const formRef = ref<FormInstance>()

  // Selected rows
  const selectedRows = ref<any[]>([])
  const single = ref(true)

  // Search form
  const searchForm = ref({
    postCode: undefined,
    postName: undefined,
    status: undefined
  })

  // Export related
  const exportLoading = ref(false)

  // Show search bar
  const showSearch = ref(true)

  // Status options
  const statusOptions = ref<DictDataEntity[]>([])

  // Form data
  const form = reactive<Partial<SysPostEntity>>({
    id: undefined,
    postCode: undefined,
    postName: undefined,
    leaderPost: 0,
    status: '0',
    remark: undefined
  })

  // leaderPost boolean for switch
  const leaderPostBool = ref(false)

  // Form validation rules
  const rules = reactive({
    postName: [
      {
        required: true,
        message: t('system.position.nameNotEmpty'),
        trigger: 'blur'
      }
    ],
    postCode: [
      {
        required: true,
        message: t('system.position.codeNotEmpty'),
        trigger: 'blur'
      }
    ]
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
      apiFn: listPost,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'id',
          label: 'Id',
          minWidth: 180,
          hidden: true
        },
        {
          prop: 'postCode',
          label: t('system.position.code'),
          minWidth: 180
        },
        {
          prop: 'postName',
          label: t('system.position.name'),
          minWidth: 200
        },
        {
          prop: 'leaderPost',
          label: t('system.position.managerPosition'),
          minWidth: 180,
          useSlot: true
        },
        {
          prop: 'status',
          label: t('system.position.status'),
          minWidth: 120,
          useSlot: true
        },
        {
          prop: 'createTime',
          label: t('system.position.createTime'),
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

  // Load dictionary data
  const loadDictData = async () => {
    try {
      const response = await getDicts('sys_normal_disable')
      statusOptions.value = response || []
    } catch (error) {
      console.error('获取字典数据失败:', error)
    }
  }

  // Get status label
  const getStatusLabel = (status: string) => {
    const dict = statusOptions.value.find((item) => item.dictValue === status)
    return dict?.dictLabel || status
  }

  // Search handling
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchParams, params)
    refreshData()
  }

  // Reset form
  const resetForm = () => {
    Object.assign(form, {
      id: undefined,
      postCode: undefined,
      postName: undefined,
      leaderPost: 0,
      status: '0',
      remark: undefined
    })
    leaderPostBool.value = false
    formRef.value?.resetFields()
  }

  // Cancel button
  const cancel = () => {
    open.value = false
    resetForm()
  }

  // Add button
  const handleAdd = () => {
    resetForm()
    open.value = true
    title.value = t('system.position.add')
  }

  // Batch update button (single selection required)
  const handleBatchUpdate = () => {
    if (selectedRows.value.length === 1) {
      handleUpdate(selectedRows.value[0])
    }
  }

  // Update button
  const handleUpdate = async (row: any) => {
    resetForm()
    try {
      // HTTP interceptor returns response.data.data directly
      const postData = await getPost(row.id)
      Object.assign(form, postData)
      leaderPostBool.value = postData.leaderPost === 1
      open.value = true
      title.value = t('system.position.edit')
    } catch (error) {
      console.error('获取岗位详情失败:', error)
    }
  }

  // Submit form
  const submitForm = () => {
    formRef.value?.validate(async (valid) => {
      if (valid) {
        try {
          if (form.id !== undefined) {
            await updatePost(form)
            MessageUtil.success(t('common.updateSuccess'))
          } else {
            await addPost(form)
            MessageUtil.success(t('system.position.addSuccess'))
          }
          open.value = false
          await refreshData()
        } catch (error) {
          console.error('提交失败:', error)
        }
      }
    })
  }

  // Delete single post
  const handleDeleteSingle = (row: any) => {
    handleDelete(row.id)
  }

  // Batch delete
  const handleBatchDelete = () => {
    const postIds = selectedRows.value.map((item) => item.id).join(',')
    handleDelete(postIds)
  }

  // Delete post (batch or single)
  const handleDelete = (postIds: any) => {
    ElMessageBox.confirm(t('system.position.confirmDelete'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          await delPost(postIds)
          MessageUtil.success(t('system.position.deleteSuccess'))
          await refreshData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      })
      .catch(() => {})
  }

  // Export
  const handleExport = () => {
    ElMessageBox.confirm(t('system.position.confirmExport'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          exportLoading.value = true
          const response = await exportPost(searchParams)
          download(response, t('system.position.positionInfo'))
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
    single.value = selection.length !== 1
  }

  onMounted(() => {
    loadDictData()
  })
</script>

<style scoped lang="scss">
  .post-page {
    padding: 16px;
  }
</style>
