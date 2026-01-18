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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:licenses:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:licenses:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>

              <el-button v-hasPermi="['itam:licenses:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button link type="primary" @click="handleAssignToDevice(row)" v-hasPermi="['itam:licenses:update']">
              分配设备
            </el-button>
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:licenses:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:licenses:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改软件对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="720px" append-to-body>
      <el-form ref="licensesRef" :model="form" :rules="formRules" label-width="120px">
        <!-- 关键信息 -->
        <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-2">关键信息</h3>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="软件名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入软件名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="序列号/密钥" prop="licenseKey">
              <el-input v-model="form.licenseKey" type="textarea" placeholder="请输入序列号或密钥" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                check-strictly
                :render-after-expand="false"
                placeholder="请选择分类"
                clearable
                node-key="id"
                :props="{ label: 'name', children: 'children', value: 'id' }"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总许可数" prop="totalSeats">
              <el-input-number
                v-model="form.totalSeats"
                placeholder="请输入总许可数"
                controls-position="right"
                :min="0"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="厂商" prop="manufacturerId">
              <el-select v-model="form.manufacturerId" placeholder="请选择厂商" clearable filterable class="!w-full">
                <el-option v-for="item in manufacturerOptions" :key="item.id!" :label="item.name" :value="item.id!" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 更多信息 -->
        <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-6">更多信息</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="许可人名字" prop="licensedToName">
              <el-input v-model="form.licensedToName" placeholder="请输入许可人名字" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="许可人邮箱" prop="licensedToEmail">
              <el-input v-model="form.licensedToEmail" placeholder="请输入许可人电子邮件" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable class="!w-full">
                <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id!" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最少数量" prop="minQty">
              <el-input-number
                v-model="form.minQty"
                placeholder="请输入最少数量"
                controls-position="right"
                :min="0"
                class="!w-full"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="请输入订单号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购订单号" prop="purchaseOrderNumber">
              <el-input v-model="form.purchaseOrderNumber" placeholder="请输入采购订单号码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价格" prop="purchaseCost">
              <el-input v-model="form.purchaseCost" placeholder="请输入采购价格" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买日期" prop="purchaseDate">
              <el-date-picker
                clearable
                v-model="form.purchaseDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择购买日期"
                class="!w-full"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="expirationDate">
              <el-date-picker
                clearable
                v-model="form.expirationDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择到期日期"
                class="!w-full"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="终止日期" prop="terminationDate">
              <el-date-picker
                clearable
                v-model="form.terminationDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择终止日期"
                class="!w-full"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="notes">
              <el-input v-model="form.notes" type="textarea" placeholder="请输入内容" />
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
    <AssignToDeviceModal ref="assignToDeviceModalRef" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, useTemplateRef } from 'vue'
  import {
    addLicenses,
    delLicenses,
    exportLicenses,
    getLicenses,
    LicensesEntity,
    pageLicenses,
    updateLicenses
  } from '@/api/itam/licenses'
  import { CategoriesEntity, getCategoriesTree } from '@/api/itam/categories'
  import { listManufacturers, ManufacturersEntity } from '@/api/itam/manufacturers'
  import { listSuppliers, SuppliersEntity } from '@/api/itam/suppliers'
  import { AssetType } from '@/api/itam/enums'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import AssignToDeviceModal from './components/AssignToDeviceModal.vue'

  defineOptions({
    name: 'LicensesList'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<LicensesEntity[]>([])

  // 分类树数据
  const categoryTree = ref<CategoriesEntity[]>([])

  // 加载分类数据
  const loadCategories = async () => {
    try {
      const data = await getCategoriesTree(AssetType.LICENSE)
      categoryTree.value = data || []
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  // 厂商选项
  const manufacturerOptions = ref<ManufacturersEntity[]>([])
  const loadManufacturers = async () => {
    try {
      manufacturerOptions.value = (await listManufacturers()) || []
    } catch (error) {
      console.error('加载厂商失败:', error)
    }
  }

  // 供应商选项
  const supplierOptions = ref<SuppliersEntity[]>([])
  const loadSuppliers = async () => {
    try {
      supplierOptions.value = (await listSuppliers()) || []
    } catch (error) {
      console.error('加载供应商失败:', error)
    }
  }

  const searchForm = ref({
    name: undefined,
    licenseKey: undefined,
    totalSeats: undefined,
    manufacturerId: undefined,
    categoryId: undefined,
    minQty: undefined,
    licensedToName: undefined,
    licensedToEmail: undefined,
    supplierId: undefined,
    orderNumber: undefined,
    purchaseCost: undefined,
    purchaseDate: undefined,
    expirationDate: undefined,
    terminationDate: undefined,
    purchaseOrderNumber: undefined,
    notes: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '软件名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入软件名称',
      clearable: true
    },
    {
      label: '序列号/密钥',
      key: 'licenseKey',
      type: 'input',
      placeholder: '请输入序列号/密钥',
      clearable: true,
      labelWidth: '120px'
    },
    {
      label: '许可人名字',
      key: 'licensedToName',
      type: 'input',
      labelWidth: '120px',
      placeholder: '请输入许可人名字',
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
      apiFn: pageLicenses,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'name', label: '软件名称', minWidth: 150 },
        { prop: 'licenseKey', label: '序列号/密钥', minWidth: 180 },
        { prop: 'totalSeats', label: '总许可数', minWidth: 100 },
        { prop: 'licensedToName', label: '许可人名字', minWidth: 120 },
        { prop: 'licensedToEmail', label: '许可人电子邮件', minWidth: 180 },
        { prop: 'purchaseDate', label: '购买日期', minWidth: 160 },
        { prop: 'expirationDate', label: '到期日期', minWidth: 160 },
        { prop: 'purchaseCost', label: '采购价格', minWidth: 120 },
        { prop: 'notes', label: '备注', minWidth: 150 },
        {
          prop: 'operation',
          label: t('system.noticeLog.operation'),
          useSlot: true,
          minWidth: 240,
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

  const form = ref<Partial<LicensesEntity>>({
    id: undefined,
    deleted: undefined,
    name: '',
    licenseKey: '',
    totalSeats: undefined,
    manufacturerId: undefined,
    createTime: '',
    updateTime: '',
    categoryId: undefined,
    minQty: undefined,
    licensedToName: '',
    licensedToEmail: '',
    supplierId: undefined,
    orderNumber: '',
    purchaseCost: '',
    purchaseDate: '',
    expirationDate: '',
    terminationDate: '',
    purchaseOrderNumber: '',
    notes: '',
    createBy: undefined,
    updateBy: undefined
  })

  const formRules = ref({
    name: [
      {
        required: true,
        message: '软件名称不能为空',
        trigger: 'blur'
      }
    ],
    licenseKey: [
      {
        required: true,
        message: '序列号/密钥不能为空',
        trigger: 'blur'
      }
    ]
  })

  const licensesRef = useTemplateRef('licensesRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: undefined,
      deleted: undefined,
      name: '',
      licenseKey: '',
      totalSeats: undefined,
      manufacturerId: undefined,
      createTime: '',
      updateTime: '',
      categoryId: undefined,
      minQty: undefined,
      licensedToName: '',
      licensedToEmail: '',
      supplierId: undefined,
      orderNumber: '',
      purchaseCost: '',
      purchaseDate: '',
      expirationDate: '',
      terminationDate: '',
      purchaseOrderNumber: '',
      notes: '',
      createBy: undefined,
      updateBy: undefined
    }
    resetFormRef(licensesRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加软件'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: LicensesEntity) => {
    reset()
    if (row.id) {
      getLicenses(row.id).then((data: LicensesEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改软件'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    licensesRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateLicenses(form.value as LicensesEntity)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addLicenses(form.value as LicensesEntity)
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

  const assignToDeviceModalRef = useTemplateRef('assignToDeviceModalRef')

  const handleAssignToDevice = (row: LicensesEntity) => {
    assignToDeviceModalRef.value?.open(row)
  }

  const handleDelete = (row?: any) => {
    const ids = row?.id || selectedRows.value.map((item) => item.id).join(',')
    ElMessageBox.confirm(t('system.noticeLog.confirmDelete') + ids + t('system.noticeLog.dataItem'), t('common.warning'))
      .then(() => delLicenses(ids))
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
          const response = await exportLicenses(searchParams)
          download(response, '软件数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  onMounted(() => {
    loadCategories()
    loadManufacturers()
    loadSuppliers()
  })
</script>

<style scoped lang="scss"></style>
