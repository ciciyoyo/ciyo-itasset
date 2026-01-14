<template>
  <div class="dict-page art-full-height">
    <!-- 搜索表单 -->
    <DictSearch v-show="showSearch" v-model="searchParams" @search="handleSearch" @reset="resetSearchParams" />

    <!-- 表格卡片 -->
    <ElCard
      class="art-table-card flex flex-col flex-1 min-h-0"
      shadow="never"
      :style="{ 'margin-top': showSearch ? '12px' : '0' }"
    >
      <!-- 表格头部工具栏 -->
      <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:dict:add']" v-ripple>
              {{ $t('system.dictionary.add') }}
            </ElButton>
            <ElButton
              @click="handleDelete()"
              icon="ele-Delete"
              type="danger"
              :disabled="multiple"
              v-hasPermi="['system:dict:remove']"
            >
              {{ $t('system.dictionary.delete') }}
            </ElButton>
            <ElButton @click="handleExport" icon="ele-Download" :loading="exportLoading" v-hasPermi="['system:dict:export']">
              {{ $t('system.dictionary.export') }}
            </ElButton>
            <ElButton @click="handleRefreshCache" icon="ele-Refresh" type="warning" v-hasPermi="['system:dict:remove']">
              {{ $t('system.dictionary.refreshCache') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="typeList"
        :columns="columns"
        :pagination="{
          current: queryParams.current,
          size: queryParams.size,
          total: total
        }"
        row-key="id"
        :show-table-header="false"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <template #dictType="{ row }">
          <span class="link-type" @click="handleViewData(row)">
            {{ row.dictType }}
          </span>
        </template>

        <template #status="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ statusOptions.find((item: any) => item.dictValue === row.status)?.dictLabel || row.status }}
          </el-tag>
        </template>

        <template #createTime="{ row }">
          {{ parseTime(row.createTime) }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:dict:edit']">
              {{ $t('system.dictionary.modify') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['system:dict:remove']">
              {{ $t('system.dictionary.delete') }}
            </el-button>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改字典对话框 -->
    <el-dialog :title="title" v-model="open" width="35%" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('system.dictionary.dictionaryName')" prop="dictName">
          <el-input v-model="form.dictName" :placeholder="$t('system.dictionary.enterDictionaryName')" />
        </el-form-item>
        <el-form-item :label="$t('system.dictionary.dictionaryType')" prop="dictType">
          <el-input v-model="form.dictType" :placeholder="$t('system.dictionary.enterDictionaryType')" />
        </el-form-item>
        <el-form-item :label="$t('system.dictionary.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('system.dictionary.note')" prop="remark">
          <el-input v-model="form.remark" :placeholder="$t('system.dictionary.enterContent')" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <DictDataDialog
      v-model="dataDialogVisible"
      :dict-id="selectedDictId"
      :dict-type="selectedDictType"
      @close="dataDialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, reactive, ref } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { addType, delType, exportType, getType, listType, refreshCache, updateType } from '@/api/system/dict/type'
  import { getDicts } from '@/api/system/dict/data'
  import type { FormInstance } from 'element-plus'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { parseTime, addDateRange } from '@/utils/business'
  import DictSearch from './modules/dict-search.vue'
  import DictDataDialog from './modules/dict-data-dialog.vue'

  defineOptions({ name: 'DictManagement' })

  const { t } = useI18n()

  // 状态和引用
  const loading = ref(true)
  const exportLoading = ref(false)
  const showSearch = ref(true)
  const typeList = ref<any[]>([])
  const title = ref('')
  const open = ref(false)
  const total = ref(0)
  const statusOptions = ref<any[]>([])

  // 选择相关
  const ids = ref<number[]>([])
  const single = ref(true)
  const multiple = ref(true)

  // 字典数据弹窗相关
  const dataDialogVisible = ref(false)
  const selectedDictId = ref<string | number>()
  const selectedDictType = ref<string>()

  // 搜索参数
  const searchParams = ref({
    dictName: undefined,
    dictType: undefined,
    status: undefined,
    dateRange: null as any
  })

  // 查询参数
  const queryParams = reactive({
    current: 1,
    size: 10,
    dictName: undefined as any,
    dictType: undefined as any,
    status: undefined as any
  })

  // 表单数据
  const form = reactive<any>({
    id: undefined,
    dictName: undefined,
    dictType: undefined,
    status: '0',
    remark: undefined
  })

  const formRef = ref<FormInstance | null>(null)

  // 表单校验规则
  const rules = {
    dictName: [
      {
        required: true,
        message: t('system.dictionary.dictionaryNameRequired'),
        trigger: 'blur'
      }
    ],
    dictType: [
      {
        required: true,
        message: t('system.dictionary.dictionaryTypeRequired'),
        trigger: 'blur'
      }
    ]
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      type: 'selection',
      minWidth: 55,
      align: 'center' as const
    },
    {
      prop: 'id',
      label: t('system.dictionary.dictionaryNumber'),
      align: 'center' as const,
      minWidth: 100
    },
    {
      prop: 'dictName',
      label: t('system.dictionary.dictionaryName'),
      align: 'center' as const,
      showOverflowTooltip: true,
      minWidth: 150
    },
    {
      prop: 'dictType',
      label: t('system.dictionary.dictionaryType'),
      align: 'center' as const,
      showOverflowTooltip: true,
      minWidth: 150,
      useSlot: true
    },
    {
      prop: 'status',
      label: t('system.dictionary.status'),
      align: 'center' as const,
      minWidth: 100,
      useSlot: true
    },
    {
      prop: 'remark',
      label: t('system.dictionary.note'),
      align: 'center' as const,
      showOverflowTooltip: true,
      minWidth: 150
    },
    {
      prop: 'createTime',
      label: t('system.dictionary.createTime'),
      align: 'center' as const,
      minWidth: 180,
      useSlot: true
    },
    {
      prop: 'operation',
      label: t('system.dictionary.operation'),
      align: 'center' as const,
      minWidth: 200,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 生命周期钩子
  onMounted(() => {
    getList()
    getDicts('sys_normal_disable')
      .then((response: any) => {
        statusOptions.value = response || []
      })
      .catch((error) => {
        console.error('获取状态字典失败:', error)
        statusOptions.value = []
      })
  })

  // 方法定义

  // 重置搜索参数
  const resetSearchParams = () => {
    searchParams.value = {
      dictName: undefined,
      dictType: undefined,
      status: undefined,
      dateRange: null
    }
    Object.assign(queryParams, {
      dictName: undefined,
      dictType: undefined,
      status: undefined
    })
    getList()
  }

  /** 查询字典类型列表 */
  const getList = async () => {
    loading.value = true
    // 合并搜索参数
    Object.assign(queryParams, {
      dictName: searchParams.value.dictName,
      dictType: searchParams.value.dictType,
      status: searchParams.value.status
    })

    // 添加日期范围
    const params = addDateRange(queryParams, searchParams.value.dateRange)

    try {
      const response = await listType(params)
      typeList.value = response.records
      total.value = response.total
    } catch (error) {
      console.error('获取字典列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 刷新数据
  const refreshData = () => {
    getList()
  }

  // 搜索处理
  const handleSearch = () => {
    queryParams.current = 1
    getList()
  }

  // 多选框选中数据
  const handleSelectionChange = (selection: any[]) => {
    ids.value = selection.map((item) => item.id)
    single.value = selection.length !== 1
    multiple.value = !selection.length
  }

  // 分页大小变化
  const handleSizeChange = (val: number) => {
    queryParams.size = val
    getList()
  }

  // 分页当前页变化
  const handleCurrentChange = (val: number) => {
    queryParams.current = val
    getList()
  }

  const cancel = () => {
    open.value = false
    reset()
  }

  const reset = () => {
    Object.assign(form, {
      id: undefined,
      dictName: undefined,
      dictType: undefined,
      status: '0',
      remark: undefined
    })
    formRef.value?.resetFields()
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    title.value = t('system.dictionary.addDictionaryType')
  }

  /** 修改按钮操作 */
  const handleUpdate = (row?: any) => {
    reset()
    const id = row?.id || ids.value[0]
    getType(id)
      .then((response) => {
        if (!response) {
          MessageUtil.error('获取字典信息失败')
          return
        }
        open.value = true
        nextTick(() => {
          Object.assign(form, response)
        })
        title.value = t('system.dictionary.modifyDictionaryType')
      })
      .catch((error) => {
        console.error('获取字典详情失败:', error)
        MessageUtil.error('获取字典信息失败')
      })
  }

  /** 提交按钮 */
  const submitForm = () => {
    formRef.value!.validate((valid: boolean) => {
      if (valid) {
        if (form.id !== undefined) {
          updateType(form).then(() => {
            MessageUtil.success(t('common.updateSuccess'))
            open.value = false
            getList()
          })
        } else {
          addType(form).then(() => {
            MessageUtil.success(t('common.addSuccess'))
            open.value = false
            getList()
          })
        }
      }
    })
  }

  /** 删除按钮操作 */
  const handleDelete = (row?: any) => {
    const deleteIds = row?.id || ids.value.join(',')
    ElMessageBox.confirm(
      t('system.dictionary.confirmDeleteDictionary') + '"' + deleteIds + '"' + t('system.dictionary.dataItem'),
      t('system.dictionary.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(() => {
        return delType(deleteIds)
      })
      .then(() => {
        getList()
        MessageUtil.success(t('system.dictionary.deleteSuccess'))
      })
      .catch(() => {})
  }

  /** 导出按钮操作 */
  const handleExport = () => {
    ElMessageBox.confirm(t('system.dictionary.confirmExportAllData'), t('system.dictionary.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => {
        exportLoading.value = true
        return exportType(queryParams)
      })
      .then((response) => {
        // 使用 download 工具函数处理文件下载
        const blob = new Blob([response as any])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = t('system.dictionary.dictionaryType') + '.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
        exportLoading.value = false
        MessageUtil.success('导出成功')
      })
      .catch(() => {
        exportLoading.value = false
      })
  }

  /** 刷新缓存按钮操作 */
  const handleRefreshCache = () => {
    refreshCache().then(() => {
      MessageUtil.success(t('common.saveSuccess'))
    })
  }

  /** 查看字典数据 */
  const handleViewData = (row: any) => {
    selectedDictId.value = row.id
    selectedDictType.value = row.dictType
    dataDialogVisible.value = true
  }
</script>

<style scoped lang="scss">
  .dict-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .link-type {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
</style>
