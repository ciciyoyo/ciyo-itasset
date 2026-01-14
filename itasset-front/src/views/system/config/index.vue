<!-- Config management page -->
<template>
  <div class="config-page art-full-height">
    <div class="flex flex-col h-full">
      <!-- Search bar -->
      <ConfigSearch v-show="showSearch" v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

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
              <ElButton @click="handleAdd" icon="ele-Plus" type="primary" v-hasPermi="['system:config:add']" v-ripple>
                {{ t('system.parameter.add') }}
              </ElButton>
              <ElButton
                @click="handleExport"
                icon="ele-Download"
                v-hasPermi="['system:config:export']"
                :loading="exportLoading"
                v-ripple
              >
                {{ t('system.parameter.export') }}
              </ElButton>
              <ElButton
                @click="handleRefreshCache"
                icon="ele-Refresh"
                v-hasPermi="['system:config:remove']"
                type="danger"
                v-ripple
              >
                {{ t('system.parameter.refreshCache') }}
              </ElButton>
              <ElButton
                @click="handleBatchDelete"
                icon="ele-Delete"
                :disabled="!selectedRows.length"
                v-hasPermi="['system:config:remove']"
                type="danger"
                v-ripple
              >
                {{ t('system.parameter.delete') }}
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
              <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['system:config:edit']">
                {{ t('system.parameter.modify') }}
              </el-button>
              <el-button link type="danger" @click="handleDeleteSingle(row)" v-hasPermi="['system:config:remove']">
                {{ t('system.parameter.delete') }}
              </el-button>
            </div>
          </template>
        </ArtTable>
      </ElCard>
    </div>

    <!-- Add or modify parameter configuration dialog -->
    <el-dialog :title="title" v-model="open" append-to-body width="40%">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="t('system.parameter.parameterName')" prop="configName">
          <el-input v-model="form.configName" :placeholder="t('system.parameter.enterParameterName')" />
        </el-form-item>
        <el-form-item :label="t('system.parameter.parameterKey')" prop="configKey">
          <el-input v-model="form.configKey" :placeholder="t('system.parameter.enterParameterKey')" />
        </el-form-item>
        <el-form-item :label="t('system.parameter.parameterKeyValue')" prop="configValue">
          <el-input v-model="form.configValue" :placeholder="t('system.parameter.enterParameterKeyValue')" />
        </el-form-item>
        <el-form-item :label="t('system.parameter.systemBuiltIn')" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio v-for="dict in typeOptions" :key="dict.dictValue" :value="dict.dictValue">
              {{ dict.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('system.parameter.note')" prop="remark">
          <el-input :rows="6" v-model="form.remark" :placeholder="t('system.parameter.enterContent')" type="textarea" />
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
  import { ref, reactive, nextTick, onMounted } from 'vue'
  import { ElMessageBox, type FormInstance } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { addConfig, delConfig, exportConfig, getConfig, listConfig, refreshCache, updateConfig } from '@/api/system/config'
  import { getDicts, DictDataEntity } from '@/api/system/dict/data'
  import { download, parseTime } from '@/utils/business'
  import { MessageUtil } from '@/utils/messageUtil'
  import ConfigSearch from './modules/config-search.vue'
  import { i18n } from '@/i18n'

  defineOptions({ name: 'Config' })

  const { t } = i18n.global

  // Type definition for form data
  interface ConfigFormData {
    id?: number
    configName?: string
    configKey?: string
    configValue?: string
    configType?: string
    remark?: string
  }

  // Selected rows
  const selectedRows = ref<any[]>([])

  // Search form
  const searchForm = ref({
    configName: undefined,
    configKey: undefined,
    configType: undefined,
    dateRange: undefined
  })

  // Show search bar
  const showSearch = ref(true)

  // Export loading
  const exportLoading = ref(false)

  // Dialog related
  const open = ref(false)
  const title = ref('')
  const formRef = ref<FormInstance>()

  // Form data
  const form = reactive<ConfigFormData>({
    id: undefined,
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: 'Y',
    remark: undefined
  })

  // Dictionary options
  const typeOptions = ref<DictDataEntity[]>([])

  // Form validation rules
  const rules = reactive({
    configName: [
      {
        required: true,
        message: t('system.parameter.parameterNameRequired'),
        trigger: 'blur'
      }
    ],
    configKey: [
      {
        required: true,
        message: t('system.parameter.parameterKeyRequired'),
        trigger: 'blur'
      }
    ],
    configValue: [
      {
        required: true,
        message: t('system.parameter.parameterValueRequired'),
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
      apiFn: listConfig,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'id',
          label: t('system.parameter.parameterPrimaryKey'),
          minWidth: 180
        },
        {
          prop: 'configName',
          label: t('system.parameter.parameterName'),
          showOverflowTooltip: true,
          minWidth: 200
        },
        {
          prop: 'configKey',
          label: t('system.parameter.parameterKey'),
          showOverflowTooltip: true,
          minWidth: 200
        },
        {
          prop: 'configValue',
          label: t('system.parameter.parameterKeyValue'),
          minWidth: 200
        },
        {
          prop: 'configType',
          label: t('system.parameter.systemBuiltIn'),
          minWidth: 150,
          formatter: (row: any) => {
            const dict = typeOptions.value.find((item) => item.dictValue === row.configType)
            return dict?.dictLabel || row.configType
          }
        },
        {
          prop: 'remark',
          label: t('system.parameter.note'),
          showOverflowTooltip: true,
          minWidth: 200
        },
        {
          prop: 'createTime',
          label: t('system.parameter.createTime'),
          minWidth: 180,
          formatter: (row: any) => parseTime(row?.createTime)
        },
        {
          prop: 'operation',
          label: t('system.parameter.operation'),
          useSlot: true,
          fixed: 'right',
          minWidth: 180
        }
      ]
    }
  })

  // Load dictionary data
  const loadDictData = async () => {
    try {
      const response = await getDicts('sys_yes_no')
      typeOptions.value = response || []
    } catch (error) {
      console.error('获取字典数据失败:', error)
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

  // Reset form
  const resetForm = () => {
    form.id = undefined
    form.configName = undefined
    form.configKey = undefined
    form.configValue = undefined
    form.configType = 'Y'
    form.remark = undefined
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
    title.value = t('system.parameter.addParameter')
  }

  // Update button
  const handleUpdate = async (row: any) => {
    try {
      const id = row.id
      // HTTP interceptor returns response.data.data directly
      const configData = await getConfig(id)
      // Populate form data
      form.id = configData.id
      form.configName = configData.configName
      form.configKey = configData.configKey
      form.configValue = configData.configValue
      form.configType = configData.configType
      form.remark = configData.remark
      // Set dialog title and open
      title.value = t('system.parameter.modifyParameter')
      await nextTick()
      open.value = true
    } catch (error) {
      console.error('获取配置详情失败:', error)
    }
  }

  // Submit form
  const submitForm = () => {
    formRef.value?.validate(async (valid) => {
      if (valid) {
        try {
          if (form.id !== undefined) {
            await updateConfig(form)
            MessageUtil.success(t('system.parameter.updateSuccess'))
          } else {
            await addConfig(form)
            MessageUtil.success(t('system.parameter.addSuccess'))
          }
          open.value = false
          await refreshData()
        } catch (error) {
          console.error('提交失败:', error)
        }
      }
    })
  }

  // Delete single config
  const handleDeleteSingle = (row: any) => {
    handleDelete(row.id)
  }

  // Batch delete
  const handleBatchDelete = () => {
    const ids = selectedRows.value.map((item) => item.id).join(',')
    handleDelete(ids)
  }

  // Delete config (batch or single)
  const handleDelete = (ids: any) => {
    ElMessageBox.confirm(t('system.parameter.confirmDeleteParameter', { id: ids }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          await delConfig(ids)
          MessageUtil.success(t('system.parameter.deleteSuccess'))
          await refreshData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      })
      .catch(() => {})
  }

  // Export
  const handleExport = () => {
    ElMessageBox.confirm(t('system.parameter.confirmExportAllData'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(async () => {
        try {
          exportLoading.value = true
          const response = await exportConfig(searchParams)
          download(response, t('system.parameter.systemConfiguration'))
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  // Refresh cache
  const handleRefreshCache = () => {
    refreshCache()
      .then(() => {
        MessageUtil.success(t('common.saveSuccess'))
      })
      .catch((error) => {
        console.error('刷新缓存失败:', error)
      })
  }

  // Table row selection change
  const handleSelectionChange = (selection: any[]): void => {
    selectedRows.value = selection
  }

  onMounted(() => {
    loadDictData()
  })
</script>

<style scoped lang="scss">
  .config-page {
    padding: 16px;
  }
</style>
