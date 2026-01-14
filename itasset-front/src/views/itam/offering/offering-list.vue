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
        <template #supplierId>
          <SupplierSelect v-model="searchForm.supplierId" />
        </template>
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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:offering:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete" v-hasPermi="['itam:offering:delete']">
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:offering:export']" icon="ele-Download" v-ripple @click="handleExport">
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
          <template #targetName="{ row }">
            <div class="flex items-center" v-if="row.targetName">
              <el-icon class="mr-1"><Monitor /></el-icon>
              <span>{{ row.targetName }}</span>
            </div>
          </template>
          <template #offeringStatus="{ row }">
            <el-tag v-if="row.offeringStatus" :type="getStatusInfo(row.offeringStatus).type">
              {{ getStatusInfo(row.offeringStatus).label }}
            </el-tag>
          </template>
          <template #endDate="{ row }">
            <el-tooltip v-if="isExpired(row.endDate)" :content="row.endDate" placement="top">
              <el-tag effect="dark" type="danger">已过期</el-tag>
            </el-tooltip>
            <span v-else>{{ row.endDate }}</span>
          </template>
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleAssignToDevice(row)" v-hasPermi="['itam:offering:update']">
              分配设备
            </el-button>
            <el-button link type="warning" @click="handleReportException(row)" v-hasPermi="['itam:offering:update']">
              报告异常
            </el-button>
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:offering:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:offering:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改服务对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="offeringRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称" clearable />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <SupplierSelect v-model="form.supplierId!" />
        </el-form-item>
        <el-form-item label="服务单号" prop="serviceNumber">
          <el-input v-model="form.serviceNumber" placeholder="请输入服务单号" clearable />
        </el-form-item>
        <el-form-item label="购入日期" prop="startDate">
          <el-date-picker
            clearable
            v-model="form.startDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择购入日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="过保日期" prop="endDate">
          <el-date-picker
            clearable
            v-model="form.endDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择过保日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="价格" prop="cost">
          <el-input-number v-model="form.cost" placeholder="请输入价格" controls-position="right">
            <template #prefix>
              <span>￥</span>
            </template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input v-model="form.notes" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报告异常对话框 -->
    <ServiceExceptionModal ref="serviceExceptionModalRef" @success="refreshData" />
    <AssignToDeviceModal ref="assignToDeviceModalRef" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, useTemplateRef } from 'vue'
  import {
    addOffering,
    delOffering,
    exportOffering,
    getOffering,
    OfferingEntity,
    OfferingVO,
    pageOffering,
    updateOffering
  } from '@/api/itam/offering'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import SupplierSelect from '@/views/itam/components/supplier-select/index.vue'

  import ServiceExceptionModal from './components/ServiceExceptionModal.vue'
  import AssignToDeviceModal from './components/AssignToDeviceModal.vue'
  import { Monitor } from '@element-plus/icons-vue'

  defineOptions({
    name: 'offering'
  })

  const { t } = useI18n()

  const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'danger' | 'primary' }> = {
    normal: { label: '正常', type: 'success' },
    exception: { label: '异常', type: 'danger' },
    maintenance: { label: '维护中', type: 'warning' },
    stopped: { label: '已停止', type: 'info' }
  }

  const getStatusInfo = (status: string) => {
    return statusMap[status] || { label: status || '未知', type: 'info' }
  }

  const isExpired = (dateStr: string) => {
    if (!dateStr) return false
    return new Date(dateStr).getTime() < new Date().getTime()
  }

  const showSearch = ref(true)

  const selectedRows = ref<OfferingVO[]>([])

  const searchForm = ref({
    name: undefined,
    supplierId: undefined,
    serviceNumber: undefined,
    startDate: undefined,
    endDate: undefined,
    cost: undefined,
    notes: undefined,
    offeringStatus: undefined,
    targetType: undefined,
    targetId: undefined,
    deleted: undefined,
    exceptionStartDate: undefined,
    exceptionEndDate: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '服务名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入服务名称',
      clearable: true
    },
    {
      label: '供应商',
      key: 'supplierId',
      slot: true
    },
    {
      label: '服务单号',
      key: 'serviceNumber',
      type: 'input',
      placeholder: '请输入服务单号',
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
      apiFn: pageOffering,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'id', label: 'Id', minWidth: 120 },
        { prop: 'name', label: '服务名称', minWidth: 120 },
        { prop: 'targetName', label: '关联设备', minWidth: 150, useSlot: true },
        { prop: 'offeringStatus', label: '服务状态', minWidth: 120, useSlot: true },
        { prop: 'supplierName', label: '供应商', minWidth: 120 },
        { prop: 'serviceNumber', label: '服务单号', minWidth: 120 },
        { prop: 'startDate', label: '购入日期', minWidth: 120 },
        { prop: 'endDate', label: '过保日期', minWidth: 120, useSlot: true },
        { prop: 'cost', label: '费用', minWidth: 120 },
        { prop: 'notes', label: '备注', minWidth: 120 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
          useSlot: true,
          minWidth: 280,
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

  const form = ref<OfferingEntity>({
    id: null,
    name: '',
    supplierId: null,
    serviceNumber: '',
    startDate: '',
    endDate: '',
    cost: 0,
    notes: '',
    offeringStatus: ''
  })

  const formRules = ref({
    name: [
      {
        required: true,
        message: '服务名称不能为空',
        trigger: 'blur'
      }
    ],
    supplierId: [
      {
        required: true,
        message: '供应商不能为空',
        trigger: 'blur'
      }
    ],
    serviceNumber: [
      {
        required: true,
        message: '服务单号不能为空',
        trigger: 'blur'
      }
    ]
  })

  const offeringRef = useTemplateRef('offeringRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: null,
      name: '',
      supplierId: null,
      serviceNumber: '',
      startDate: '',
      endDate: '',
      cost: 0,
      notes: '',
      offeringStatus: ''
    }
    resetFormRef(offeringRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加服务'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: OfferingVO) => {
    reset()
    if (row.id) {
      getOffering(row.id).then((data: OfferingVO | any) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改服务'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    offeringRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateOffering(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addOffering(form.value)
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
      .then(() => delOffering(ids))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.deleteSuccess'))
      })
      .catch(() => {})
  }

  const assignToDeviceModalRef = useTemplateRef('assignToDeviceModalRef')

  const handleAssignToDevice = (row: OfferingVO) => {
    assignToDeviceModalRef.value?.open(row)
  }

  const serviceExceptionModalRef = useTemplateRef('serviceExceptionModalRef')

  const handleReportException = (row: OfferingVO) => {
    serviceExceptionModalRef.value?.open(row)
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
          const response = await exportOffering(searchParams)
          download(response, '服务数据')
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
