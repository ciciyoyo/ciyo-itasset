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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:categories:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete" v-hasPermi="['itam:categories:delete']">
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:categories:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:categories:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:categories:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改分类对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="categoriesRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="父级 ID" prop="parentId">
          <el-input v-model="form.parentId" placeholder="请输入父级 ID" />
        </el-form-item>
        <el-form-item label="分类大类" prop="categoryType"> </el-form-item>
        <el-form-item label="是否删除" prop="deleted">
          <el-input v-model="form.deleted" placeholder="请输入是否删除" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, useTemplateRef } from 'vue'
  import {
    addCategories,
    CategoriesEntity,
    delCategories,
    exportCategories,
    getCategories,
    pageCategories,
    updateCategories
  } from '@/api/itam/categories'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'

  defineOptions({
    name: 'categories'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<CategoriesEntity[]>([])

  const searchForm = ref({
    name: undefined,
    code: undefined,
    parentId: undefined,
    categoryType: undefined,
    deleted: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '分类名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入分类名称',
      clearable: true
    },
    {
      label: '分类编码',
      key: 'code',
      type: 'input',
      placeholder: '请输入分类编码',
      clearable: true
    },
    {
      label: '父级 ID',
      key: 'parentId',
      type: 'input',
      placeholder: '请输入父级 ID',
      clearable: true
    },

    {
      label: '是否删除',
      key: 'deleted',
      type: 'input',
      placeholder: '请输入是否删除',
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
      apiFn: pageCategories,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'name', label: '分类名称', minWidth: 120 },
        { prop: 'code', label: '分类编码', minWidth: 120 },
        { prop: 'parentId', label: '父级 ID', minWidth: 120 },
        { prop: 'categoryType', label: '分类大类', minWidth: 120 },
        { prop: 'deleted', label: '是否删除', minWidth: 120 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
          useSlot: true,
          minWidth: 100,
          fixed: 'right'
        }
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

  const open = ref(false)

  const form = ref<CategoriesEntity>({
    id: 0,
    name: '',
    code: '',
    parentId: 0,
    categoryType: '',
    deleted: 0,
    createTime: '',
    updateTime: '',
    createBy: 0,
    updateBy: 0
  })

  const formRules = ref({
    id: [
      {
        required: true,
        message: 'Id不能为空',
        trigger: 'blur'
      }
    ],
    name: [
      {
        required: true,
        message: '分类名称不能为空',
        trigger: 'blur'
      }
    ],
    code: [
      {
        required: true,
        message: '分类编码不能为空',
        trigger: 'blur'
      }
    ],
    parentId: [
      {
        required: true,
        message: '父级 ID不能为空',
        trigger: 'blur'
      }
    ],
    categoryType: [
      {
        required: true,
        message: '分类大类不能为空',
        trigger: 'change'
      }
    ],
    deleted: [
      {
        required: true,
        message: '是否删除不能为空',
        trigger: 'blur'
      }
    ],
    createTime: [
      {
        required: true,
        message: '创建时间不能为空',
        trigger: 'blur'
      }
    ],
    updateTime: [
      {
        required: true,
        message: '更新时间不能为空',
        trigger: 'blur'
      }
    ]
  })

  const categoriesRef = useTemplateRef('categoriesRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: null,
      name: '',
      code: '',
      parentId: null,
      categoryType: '',
      deleted: null,
      createTime: '',
      updateTime: '',
      createBy: null,
      updateBy: null
    }
    resetFormRef(categoriesRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加分类'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: CategoriesEntity) => {
    reset()
    if (row.id) {
      getCategories(row.id).then((data: CategoriesEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改分类'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    categoriesRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateCategories(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addCategories(form.value)
            .then(() => {
              MessageUtil.success(t('common.addSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        }
      }
    })
  }

  const handleDelete = (row?: any) => {
    const ids = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(t('system.noticeLog.confirmDelete') + ids + t('system.noticeLog.dataItem'), t('common.warning'))
      .then(() => delCategories(ids))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.deleteSuccess'))
      })
      .catch(() => {})
  }

  // 取消按钮
  const cancel = () => {
    open.value = false
    reset()
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
          const response = await exportCategories(searchParams)
          download(response, '分类数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
