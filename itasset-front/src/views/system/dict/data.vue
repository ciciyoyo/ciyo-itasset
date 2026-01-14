<template>
  <div class="dict-data-page art-full-height">
    <!-- 搜索表单 -->
    <DictDataSearch
      v-show="showSearch"
      v-model="searchParams"
      :type-options="typeOptions"
      @search="handleSearch"
      @reset="resetSearchParams"
    />

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
              {{ $t('system.dictionaryData.add') }}
            </ElButton>
            <ElButton
              @click="handleUpdate()"
              icon="ele-Edit"
              type="success"
              plain
              :disabled="single"
              v-hasPermi="['system:dict:edit']"
            >
              {{ $t('system.dictionaryData.modify') }}
            </ElButton>
            <ElButton
              @click="handleDelete()"
              icon="ele-Delete"
              type="danger"
              plain
              :disabled="multiple"
              v-hasPermi="['system:dict:remove']"
            >
              {{ $t('system.dictionaryData.delete') }}
            </ElButton>
            <ElButton
              @click="handleExport"
              icon="ele-Download"
              type="warning"
              plain
              :loading="exportLoading"
              v-hasPermi="['system:dict:export']"
            >
              {{ $t('system.dictionaryData.export') }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        class="flex-1"
        :loading="loading"
        :data="dataList"
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
        <template #dictLabel="{ row }">
          <span v-if="!row.listClass || row.listClass === 'default'">
            {{ row.dictLabel }}
          </span>
          <ElTag v-else :type="row.listClass === 'primary' ? '' : row.listClass">
            {{ row.dictLabel }}
          </ElTag>
        </template>

        <template #status="{ row }">
          <ElTag :type="row.status === '0' ? 'success' : 'danger'">
            {{ statusOptions.find((item) => item.dictValue === row.status)?.dictLabel || row.status }}
          </ElTag>
        </template>

        <template #createTime="{ row }">
          {{ parseTime(row.createTime) }}
        </template>

        <template #operation="{ row }">
          <div class="flex gap-2">
            <ElButton link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:dict:edit']">
              {{ $t('system.dictionaryData.modify') }}
            </ElButton>
            <ElButton link type="danger" @click="handleDelete(row)" v-hasPermi="['system:dict:remove']">
              {{ $t('system.dictionaryData.delete') }}
            </ElButton>
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 添加或修改对话框 -->
    <ElDialog :title="title" v-model="open" width="40%" append-to-body :close-on-click-modal="false">
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="128px">
        <ElFormItem :label="$t('system.dictionaryData.dictionaryType')">
          <ElInput v-model="form.dictType" :disabled="true" />
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.dictionaryLabel')" prop="dictLabel">
          <ElInput v-model="form.dictLabel" :placeholder="$t('system.dictionaryData.enterDictionaryLabel')" />
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.dictionaryKey')" prop="dictValue">
          <ElInput v-model="form.dictValue" :placeholder="$t('system.dictionaryData.enterDataKey')" />
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.styleAttribute')" prop="cssClass">
          <ElInput v-model="form.cssClass" :placeholder="$t('system.dictionaryData.enterStyleAttribute')" />
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.displayOrder')" prop="dictSort">
          <ElInputNumber v-model="form.dictSort" :min="0" controls-position="right" />
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.echoStyle')" prop="listClass">
          <ElSelect v-model="form.listClass">
            <ElOption v-for="item in listClassOptions" :key="item.value" :label="item.label" :value="item.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.status')" prop="status">
          <ElRadioGroup v-model="form.status">
            <ElRadio v-for="dict in statusOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem :label="$t('system.dictionaryData.note')" prop="remark">
          <ElInput v-model="form.remark" :placeholder="$t('system.dictionaryData.enterContent')" type="textarea" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton type="primary" @click="submitForm">
            {{ $t('common.confirm') }}
          </ElButton>
          <ElButton @click="cancel">{{ $t('common.cancel') }}</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, reactive, ref, watch } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessageBox } from 'element-plus'
  import type { FormInstance } from 'element-plus'
  import { addData, delData, exportData, getData, listData, updateData } from '@/api/system/dict/data'
  import { getType, optionselect } from '@/api/system/dict/type'
  import { getDicts } from '@/api/system/dict/data'
  import { MessageUtil } from '@/utils/messageUtil'
  import { useI18n } from 'vue-i18n'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { parseTime } from '@/utils/business'
  import DictDataSearch from './modules/dict-data-search.vue'

  defineOptions({ name: 'DictDataManagement' })

  const props = defineProps<{
    dictId?: string | number
  }>()

  const { t } = useI18n()
  const route = useRoute()

  // 状态和引用
  const loading = ref(true)
  const exportLoading = ref(false)
  const showSearch = ref(true)
  const dataList = ref<any[]>([])
  const title = ref('')
  const open = ref(false)
  const total = ref(0)
  const statusOptions = ref<any[]>([])
  const typeOptions = ref<any[]>([])
  const defaultDictType = ref('')

  // 选择相关
  const ids = ref<number[]>([])
  const single = ref(true)
  const multiple = ref(true)

  // 搜索参数
  const searchParams = ref<{
    dictType?: string
    dictLabel?: string
    status?: string
  }>({
    dictType: undefined,
    dictLabel: undefined,
    status: undefined
  })

  // 查询参数
  const queryParams = reactive({
    current: 1,
    size: 10,
    dictType: undefined as any,
    dictLabel: undefined as any,
    status: undefined as any
  })

  // 表单数据
  const form = reactive<any>({
    id: undefined,
    dictType: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: 'default',
    dictSort: 0,
    status: '0',
    remark: undefined
  })

  const formRef = ref<FormInstance | null>(null)

  // 数据标签回显样式选项
  const listClassOptions = [
    { value: 'default', label: t('system.dictionaryData.default') },
    { value: 'primary', label: t('system.dictionaryData.primary') },
    { value: 'success', label: t('system.dictionaryData.success') },
    { value: 'info', label: t('system.dictionaryData.info') },
    { value: 'warning', label: t('system.dictionaryData.warning') },
    { value: 'danger', label: t('system.dictionaryData.danger') }
  ]

  // 表单校验规则
  const rules = {
    dictLabel: [
      {
        required: true,
        message: t('system.dictionaryData.dataLabelRequired'),
        trigger: 'blur'
      }
    ],
    dictValue: [
      {
        required: true,
        message: t('system.dictionaryData.dataKeyRequired'),
        trigger: 'blur'
      }
    ],
    dictSort: [
      {
        required: true,
        message: t('system.dictionaryData.dataOrderRequired'),
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
      label: t('system.dictionaryData.dictionaryCode'),
      align: 'center' as const,
      minWidth: 100
    },
    {
      prop: 'dictLabel',
      label: t('system.dictionaryData.dictionaryLabel'),
      align: 'center' as const,
      minWidth: 120,
      useSlot: true
    },
    {
      prop: 'dictValue',
      label: t('system.dictionaryData.dictionaryKey'),
      align: 'center' as const,
      minWidth: 120
    },
    {
      prop: 'dictSort',
      label: t('system.dictionaryData.dictionaryOrder'),
      align: 'center' as const,
      minWidth: 100
    },
    {
      prop: 'status',
      label: t('system.dictionaryData.status'),
      align: 'center' as const,
      minWidth: 100,
      useSlot: true
    },
    {
      prop: 'remark',
      label: t('system.dictionaryData.note'),
      align: 'center' as const,
      showOverflowTooltip: true,
      minWidth: 150
    },
    {
      prop: 'createTime',
      label: t('system.dictionaryData.createTime'),
      align: 'center' as const,
      minWidth: 180,
      useSlot: true
    },
    {
      prop: 'operation',
      label: t('system.dictionaryData.operation'),
      align: 'center' as const,
      minWidth: 200,
      fixed: 'right' as const,
      useSlot: true
    }
  ])

  // 生命周期钩子
  onMounted(async () => {
    // 优先使用 props 传入的 dictId，否则使用路由参数
    const dictId = props.dictId?.toString() || (route.params.dictId as string)
    if (dictId) {
      await fetchDictType(dictId)
    }
    await fetchTypeList()
    await fetchStatusOptions()
  })

  // 监听路由参数变化
  watch(
    () => route.params.dictId,
    async (newDictId) => {
      if (newDictId && !props.dictId) {
        await fetchDictType(newDictId as string)
      }
    }
  )

  // 监听 props.dictId 变化
  watch(
    () => props.dictId,
    async (newDictId) => {
      if (newDictId) {
        await fetchDictType(newDictId.toString())
      }
    }
  )

  // 方法定义

  /** 查询字典类型详细 */
  const fetchDictType = async (dictId: string) => {
    try {
      const response = await getType(dictId)
      queryParams.dictType = response.dictType
      searchParams.value.dictType = response.dictType
      defaultDictType.value = response.dictType
      await getList()
    } catch (error) {
      console.error('获取字典类型失败:', error)
    }
  }

  /** 查询字典类型列表 */
  const fetchTypeList = async () => {
    try {
      const response = await optionselect()
      typeOptions.value = response
    } catch (error) {
      console.error('获取字典类型列表失败:', error)
    }
  }

  /** 获取状态字典 */
  const fetchStatusOptions = async () => {
    try {
      const response = await getDicts('sys_normal_disable')
      statusOptions.value = response || []
    } catch (error) {
      console.error('获取状态字典失败:', error)
      statusOptions.value = []
    }
  }

  /** 查询字典数据列表 */
  const getList = async () => {
    loading.value = true
    // 合并搜索参数
    Object.assign(queryParams, {
      dictType: searchParams.value.dictType,
      dictLabel: searchParams.value.dictLabel,
      status: searchParams.value.status
    })

    try {
      const response = await listData(queryParams)
      dataList.value = response.records
      total.value = response.total
    } catch (error) {
      console.error('获取字典数据列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 刷新数据
  const refreshData = () => {
    getList()
  }

  // 重置搜索参数
  const resetSearchParams = () => {
    searchParams.value = {
      dictType: defaultDictType.value,
      dictLabel: undefined,
      status: undefined
    }
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
      dictType: undefined,
      dictLabel: undefined,
      dictValue: undefined,
      cssClass: undefined,
      listClass: 'default',
      dictSort: 0,
      status: '0',
      remark: undefined
    })
    formRef.value?.resetFields()
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    title.value = t('system.dictionaryData.addDictionaryData')
    form.dictType = queryParams.dictType
  }

  /** 修改按钮操作 */
  const handleUpdate = async (row?: any) => {
    reset()
    const id = row?.id || ids.value[0]
    try {
      const response = await getData(id)
      if (!response) {
        MessageUtil.error('获取字典数据失败')
        return
      }
      open.value = true
      nextTick(() => {
        Object.assign(form, response)
      })
      title.value = t('system.dictionaryData.modifyDictionaryData')
    } catch (error) {
      console.error('获取字典数据详情失败:', error)
      MessageUtil.error('获取字典数据失败')
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    formRef.value!.validate(async (valid: boolean) => {
      if (valid) {
        try {
          if (form.id !== undefined) {
            await updateData(form)
            MessageUtil.success(t('common.updateSuccess'))
          } else {
            await addData(form)
            MessageUtil.success(t('common.addSuccess'))
          }
          open.value = false
          await getList()
        } catch (error) {
          console.error('提交失败:', error)
        }
      }
    })
  }

  /** 删除按钮操作 */
  const handleDelete = (row?: any) => {
    const deleteIds = row?.id || ids.value.join(',')
    ElMessageBox.confirm(
      t('system.dictionaryData.confirmDeleteDictionaryCode') + '"' + deleteIds + '"' + t('system.dictionaryData.dataItem'),
      t('system.dictionaryData.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
      .then(async () => {
        await delData(deleteIds)
        await getList()
        MessageUtil.success(t('system.dictionaryData.deleteSuccess'))
      })
      .catch(() => {})
  }

  /** 导出按钮操作 */
  const handleExport = () => {
    ElMessageBox.confirm(t('system.dictionaryData.confirmExportAllData'), t('system.dictionaryData.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        exportLoading.value = true
        const response = await exportData(queryParams)
        const blob = new Blob([response as any])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = t('system.dictionaryData.dictionaryData') + '.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
        exportLoading.value = false
        MessageUtil.success('导出成功')
      })
      .catch(() => {
        exportLoading.value = false
      })
  }
</script>

<style scoped lang="scss">
  .dict-data-page {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
</style>
