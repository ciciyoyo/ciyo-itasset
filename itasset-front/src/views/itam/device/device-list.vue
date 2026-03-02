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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:device:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:device:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>

              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Close"
                @click="handleScrap()"
                v-hasPermi="['itam:device:update']"
              >
                批量报废
              </el-button>

              <el-button
                v-hasPermi="['itam:device:query']"
                :disabled="!selectedRows.length"
                icon="ele-Printer"
                @click="handleBatchPrint"
                v-ripple
              >
                打印标签
              </el-button>
              <ExcelImport
                url="/itam/device/importData"
                title="设备导入"
                templateCode="device"
                v-hasPermi="['itam:device:import']"
                @success="refreshData"
              >
              </ExcelImport>
              <el-button v-hasPermi="['itam:device:export']" icon="ele-Download" v-ripple @click="handleExport">
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
          <template #name="{ row }">
            <el-link type="primary" :underline="false" @click="handleViewDetail(row)">
              {{ row.name }}
            </el-link>
          </template>
          <template #imageUrl="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              style="width: 50px; height: 50px; border-radius: 4px"
              fit="cover"
              preview-teleported
            />
            <span v-else class="text-gray-400">暂无图片</span>
          </template>
          <template #assignedToName="{ row }">
            <el-tag v-if="row.assignedToName" type="success">{{ row.assignedToName }}</el-tag>
            <el-tag v-else type="info">未分配</el-tag>
          </template>
          <template #assetsStatus="{ row }">
            <el-tag v-if="row.assetsStatus === 1" type="success">闲置</el-tag>
            <el-tag v-else-if="row.assetsStatus === 2" type="primary">在用</el-tag>
            <el-tag v-else-if="row.assetsStatus === 4" type="warning">故障</el-tag>
            <el-tag v-else-if="row.assetsStatus === 10" type="danger">报废</el-tag>
            <el-tag v-else type="info">未知 ({{ row.assetsStatus }})</el-tag>
          </template>
          <template #operation="{ row }">
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:device:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="primary" @click="handleAssign(row)" v-hasPermi="['itam:device:update']"> 分配 </el-button>

            <el-dropdown trigger="click">
              <el-button link type="primary" style="margin-left: 12px">
                更多 <el-icon class="el-icon--right"><ele-ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="hasPermi(['itam:device:query'])" @click="handlePrintLabel(row)">
                    <span class="text-primary">打印标签</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="hasPermi(['itam:device:update'])" @click="handleReportException(row)">
                    <span class="text-warning">报告异常</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="hasPermi(['itam:device:update']) && row.assetsStatus !== 10" @click="handleScrap(row)">
                    <span class="text-danger">报废</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="row.assetsStatus === 10 && hasPermi(['itam:device:update'])"
                    @click="handleRecover(row)"
                  >
                    <span class="text-success">恢复</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="hasPermi(['itam:device:delete'])" @click="handleDelete(row)">
                    <span class="text-danger">{{ $t('system.roleManagement.delete') }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改设备对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="720px" append-to-body>
      <el-form ref="deviceRef" :model="form" :rules="formRules" label-width="100px">
        <!-- 基本信息 -->
        <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-2">基本信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入设备名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序列号" prop="serial">
              <el-input v-model="form.serial" placeholder="请输入序列号" clearable />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryOptions"
                check-strictly
                :render-after-expand="false"
                placeholder="请选择设备分类"
                clearable
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceNo">
              <el-input v-model="form.deviceNo" placeholder="请输入设备编号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="关联型号" prop="modelId">
              <div class="flex gap-2">
                <el-select v-model="form.modelId" placeholder="请选择型号" clearable filterable class="flex-1">
                  <el-option v-for="item in modelOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
                <el-button type="primary" icon="ele-Plus" @click="handleGoToModels">新建型号</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="设备图片" prop="imageUrl">
              <ImageUpload v-model:value="form.imageUrl" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 更多信息 -->
        <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-6">更多信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物理位置" prop="locationId">
              <el-tree-select
                v-model="form.locationId"
                :data="locationOptions"
                check-strictly
                :render-after-expand="false"
                placeholder="请选择位置"
                clearable
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable class="!w-full">
                <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="折旧规则" prop="depreciationId">
              <el-select v-model="form.depreciationId" placeholder="请选择折旧规则" clearable filterable class="!w-full">
                <el-option v-for="item in depreciationOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买日期" prop="purchaseDate">
              <el-date-picker
                clearable
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择购买日期"
                class="!w-full"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买成本" prop="purchaseCost">
              <el-input-number v-model="form.purchaseCost" :min="0" :precision="2" placeholder="请输入购买成本" class="!w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过保日期" prop="warrantyDate">
              <el-date-picker
                clearable
                v-model="form.warrantyDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择过保日期"
                class="!w-full"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 分配设备对话框 -->
    <AllocateModal ref="allocateModalRef" @success="refreshData" />
    <!-- 报告异常对话框 -->
    <DeviceExceptionModal ref="deviceExceptionModalRef" @success="refreshData" />
    <!-- 设备详情弹窗 -->
    <DeviceDetailDrawer ref="deviceDetailRef" />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, useTemplateRef } from 'vue'
  import { useRouter } from 'vue-router'
  import {
    addDevice,
    delDevice,
    DeviceEntity,
    exportDevice,
    getDevice,
    pageDevice,
    updateDevice,
    scrappedDevice,
    recoverDevice,
    batchPrintDeviceLabels
  } from '@/api/itam/device'
  import { listSuppliers } from '@/api/itam/suppliers'
  import { listLocations } from '@/api/itam/locations'
  import { listDepreciations } from '@/api/itam/depreciations'
  import { listModels } from '@/api/itam/models'
  import { getCategoriesTree, CategoriesEntity } from '@/api/itam/categories'
  import { AssetType } from '@/api/itam/enums'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, downloadPdf, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import ImageUpload from '@/components/business/image-upload/index.vue'
  import AllocateModal from './components/AllocateModal.vue'
  import DeviceExceptionModal from './components/DeviceExceptionModal.vue'
  import DeviceDetailDrawer from './components/DeviceDetailDrawer.vue'
  import ExcelImport from '@/components/business/excel-import/index.vue'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({
    name: 'device'
  })

  const { t } = useI18n()
  const router = useRouter()

  // 权限校验
  const userStore = useUserStore()
  const hasPermi = (value: string[]) => {
    const all_permission = '*:*:*'
    const permissions = userStore.getUserInfo.buttons || []
    return permissions.some((permission) => {
      return all_permission === permission || value.includes(permission)
    })
  }

  const showSearch = ref(true)

  const selectedRows = ref<DeviceEntity[]>([])

  // 下拉选项数据
  const supplierOptions = ref<any[]>([])
  const locationOptions = ref<any[]>([])
  const depreciationOptions = ref<any[]>([])
  const modelOptions = ref<any[]>([])
  const categoryOptions = ref<CategoriesEntity[]>([])

  // 分配设备相关
  const allocateModalRef = useTemplateRef('allocateModalRef')

  // 分配设备操作
  const handleAssign = (row: DeviceEntity) => {
    allocateModalRef.value?.open(row)
  }

  // 报告异常相关
  const deviceExceptionModalRef = useTemplateRef('deviceExceptionModalRef')

  const handleReportException = (row: DeviceEntity) => {
    deviceExceptionModalRef.value?.open(row)
  }

  // 设备详情相关
  const deviceDetailRef = useTemplateRef('deviceDetailRef')

  const handleViewDetail = (row: DeviceEntity) => {
    deviceDetailRef.value?.open(row)
  }

  // 跳转到型号管理页面
  const handleGoToModels = () => {
    open.value = false
    router.push('/itam/models')
  }

  // 加载选项数据
  const loadSuppliers = async () => {
    try {
      const data = await listSuppliers()
      supplierOptions.value = data || []
    } catch (error) {
      console.error('加载供应商失败:', error)
    }
  }

  const loadLocations = async () => {
    try {
      const data = await listLocations()
      locationOptions.value = data || []
    } catch (error) {
      console.error('加载位置失败:', error)
    }
  }

  const loadDepreciations = async () => {
    try {
      const data = await listDepreciations()
      depreciationOptions.value = data || []
    } catch (error) {
      console.error('加载折旧规则失败:', error)
    }
  }

  const loadModels = async () => {
    try {
      const data = await listModels()
      modelOptions.value = data || []
    } catch (error) {
      console.error('加载型号失败:', error)
    }
  }

  const loadCategories = async () => {
    try {
      const data = await getCategoriesTree(AssetType.DEVICE)
      categoryOptions.value = data || []
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  const searchForm = ref({
    serial: undefined,
    name: undefined,
    deviceNo: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '序列号',
      key: 'serial',
      type: 'input',
      placeholder: '请输入序列号',
      clearable: true
    },
    {
      label: '设备名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入设备名称',
      clearable: true
    },
    {
      label: '设备编号',
      key: 'deviceNo',
      type: 'input',
      placeholder: '请输入设备编号',
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
      apiFn: pageDevice,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'deviceNo', label: '设备编号', minWidth: 150 },
        { prop: 'name', label: '设备名称', minWidth: 150, useSlot: true },
        { prop: 'serial', label: '序列号', minWidth: 140 },
        { prop: 'assignedToName', label: '使用人', minWidth: 140, useSlot: true },
        { prop: 'assetsStatus', label: '状态', minWidth: 140, useSlot: true },
        { prop: 'imageUrl', label: '设备图片', minWidth: 120, useSlot: true },
        { prop: 'modelName', label: '型号', minWidth: 150 },
        { prop: 'locationName', label: '存放位置', minWidth: 120 },
        { prop: 'supplierName', label: '供应商', minWidth: 120 },
        { prop: 'depreciationName', label: '折旧规则', minWidth: 120 },
        { prop: 'purchaseCost', label: '购买成本', minWidth: 120 },
        { prop: 'purchaseDate', label: '购买日期', minWidth: 120 },
        { prop: 'warrantyDate', label: '过保日期', minWidth: 120 },
        { prop: 'description', label: '描述', minWidth: 200 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
          useSlot: true,
          minWidth: 180,
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

  const form = ref<DeviceEntity>({
    id: undefined,
    modelId: '',
    serial: '',
    name: '',
    imageUrl: '',
    purchaseDate: '',
    purchaseCost: '',
    warrantyDate: '',
    locationId: '',
    supplierId: '',
    depreciationId: '',
    description: '',
    deviceNo: '',
    categoryId: undefined
  })

  const formRules = ref({
    name: [
      {
        required: true,
        message: '设备名称不能为空',
        trigger: 'blur'
      }
    ],
    modelId: [
      {
        required: true,
        message: '关联型号不能为空',
        trigger: 'change'
      }
    ],
    categoryId: [
      {
        required: true,
        message: '请选择设备分类',
        trigger: 'change'
      }
    ]
  })

  const deviceRef = useTemplateRef('deviceRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: undefined,
      modelId: '',
      serial: '',
      name: '',
      imageUrl: '',
      purchaseDate: '',
      purchaseCost: '',
      warrantyDate: '',
      locationId: '',
      supplierId: '',
      depreciationId: '',
      description: '',
      deviceNo: '',
      categoryId: undefined
    }
    resetFormRef(deviceRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    // 重新加载下拉选项数据，确保数据是最新的
    loadSuppliers()
    loadLocations()
    loadDepreciations()
    loadModels()
    loadCategories()
    open.value = true
    dialogTitle.value = '添加设备'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: DeviceEntity) => {
    reset()
    // 重新加载下拉选项数据，确保数据是最新的
    loadSuppliers()
    loadLocations()
    loadDepreciations()
    loadModels()
    loadCategories()
    if (row.id) {
      getDevice(row.id).then((data: DeviceEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改设备'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    deviceRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id) {
          updateDevice(form.value)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { id, ...data } = form.value
          addDevice(data as DeviceEntity)
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
      .then(() => delDevice(ids))
      .then(() => {
        refreshData()
        MessageUtil.success(t('common.deleteSuccess'))
      })
      .catch(() => {})
  }

  /** 报废按钮操作 */
  const handleScrap = (row?: any) => {
    const ids = row ? [row.id] : selectedRows.value.map((item) => item.id)
    if (ids.length === 0) return
    const name = row ? `设备 "${row.name}"` : '选中的这些设备'

    ElMessageBox.confirm(`确认要报废${name}吗？`, t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => scrappedDevice(ids))
      .then(() => {
        refreshData()
        MessageUtil.success('报废成功')
      })
      .catch(() => {})
  }

  /** 恢复报废设备操作 */
  const handleRecover = (row: any) => {
    const ids = [row.id]
    const name = `设备 "${row.name}"`

    ElMessageBox.confirm(`确认要恢复${name}吗？`, t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
      .then(() => recoverDevice(ids))
      .then(() => {
        refreshData()
        MessageUtil.success('恢复成功')
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
          const response = await exportDevice(searchParams)
          download(response, '设备数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  // 打印标签
  const handlePrintLabel = async (row: DeviceEntity) => {
    try {
      const data = await batchPrintDeviceLabels([row.id!])
      downloadPdf(data, `label-${row.deviceNo}.pdf`)
    } catch (error) {
      console.error('打印失败:', error)
    }
  }

  // 批量打印标签
  const handleBatchPrint = async () => {
    if (selectedRows.value.length === 0) return
    const ids = selectedRows.value.map((item) => item.id!)
    try {
      const data = await batchPrintDeviceLabels(ids)
      downloadPdf(data, `device-labels.pdf`)
    } catch (error) {
      console.error('批量打印失败:', error)
    }
  }
</script>

<style scoped lang="scss">
  .log-page {
    padding: 16px;
  }
</style>
