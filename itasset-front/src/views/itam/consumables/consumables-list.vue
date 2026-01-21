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
              <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple v-hasPermi="['itam:consumables:add']">
                {{ $t('common.add') }}
              </el-button>
              <el-button
                :disabled="!selectedRows.length"
                type="danger"
                v-ripple
                icon="ele-Delete"
                @click="handleDelete"
                v-hasPermi="['itam:consumables:delete']"
              >
                {{ $t('common.delete') }}
              </el-button>
              <el-button v-hasPermi="['itam:consumables:export']" icon="ele-Download" v-ripple @click="handleExport">
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
            <el-button link type="primary" @click="handleCollect(row)" v-hasPermi="['itam:consumables:update']"> 领取 </el-button>
            <el-button link type="success" @click="handleStockIn(row)" v-hasPermi="['itam:consumables:update']"> 入库 </el-button>
            <el-button link type="primary" @click="handleUpdate(row)" v-hasPermi="['itam:consumables:update']">
              {{ $t('system.roleManagement.edit') }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-hasPermi="['itam:consumables:delete']">
              {{ $t('system.roleManagement.delete') }}
            </el-button>
          </template>
        </ArtTable>
      </ElCard>
    </div>
    <!-- 添加或修改耗材对话框 -->
    <el-dialog :title="dialogTitle" v-model="open" width="500px" append-to-body>
      <el-form ref="consumablesRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="耗材名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入耗材名称" clearable />
        </el-form-item>
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
        <el-form-item label="厂商" prop="manufacturerId">
          <el-select v-model="form.manufacturerId" placeholder="请选择制造商" clearable filterable class="!w-full">
            <el-option v-for="item in manufacturerList" :key="item.id" :label="item.name" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="物品编号/型号" prop="itemNo">
          <el-input v-model="form.itemNo" placeholder="请输入物品编号/型号" clearable />
        </el-form-item>
        <el-form-item label="采购单号" prop="orderNumber">
          <el-input v-model="form.orderNumber" placeholder="请输入采购单号" clearable />
        </el-form-item>
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
        <el-form-item label="购买成本" prop="purchaseCost">
          <el-input v-model="form.purchaseCost" placeholder="请输入购买成本" clearable />
        </el-form-item>
        <el-form-item label="当前库存数量" prop="quantity">
          <el-input-number
            v-model="form.quantity"
            placeholder="请输入当前库存数量"
            controls-position="right"
            :min="0"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="最小库存预警" prop="minQuantity">
          <el-input-number
            v-model="form.minQuantity"
            placeholder="请输入最小库存预警"
            controls-position="right"
            :min="0"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="存放位置" prop="locationId">
          <el-tree-select
            v-model="form.locationId"
            :data="locationTree"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择存放位置"
            clearable
            node-key="id"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            class="!w-full"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
          <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }} </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 领取对话框 -->
    <el-dialog title="耗材领取" v-model="collectDialogVisible" width="400px" append-to-body>
      <el-form ref="collectFormRef" :model="collectForm" :rules="collectRules" label-width="100px">
        <el-form-item label="耗材名称">
          <el-input :value="currentItem?.name" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentItem?.quantity" disabled />
        </el-form-item>
        <el-form-item label="领取数量" prop="quantity">
          <el-input-number
            v-model="collectForm.quantity"
            :min="1"
            :max="Number(currentItem?.quantity) || 0"
            controls-position="right"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="领取人" prop="targetId">
          <el-select
            v-model="collectForm.targetId"
            placeholder="请选择领取人"
            @click="handleOpenCollectUserTable"
            class="!w-full"
          >
            <el-option
              v-if="collectUser"
              :key="collectUser.id"
              :disabled="true"
              :label="collectUser.nickName"
              :value="collectUser.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="collectForm.note" type="textarea" placeholder="请输入备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="collectDialogVisible = false">取消</el-button>
          <el-button :loading="collectLoading" type="primary" @click="submitCollect">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 入库对话框 -->
    <el-dialog title="耗材入库" v-model="stockInDialogVisible" width="400px" append-to-body>
      <el-form ref="stockInFormRef" :model="stockInForm" :rules="stockInRules" label-width="100px">
        <el-form-item label="耗材名称">
          <el-input :value="currentItem?.name" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentItem?.quantity" disabled />
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" controls-position="right" class="!w-full" />
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="stockInForm.note" type="textarea" placeholder="请输入备注" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stockInDialogVisible = false">取消</el-button>
          <el-button :loading="stockInLoading" type="primary" @click="submitStockIn">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户选择对话框 -->
    <user-choose-table ref="collectUserChooseTableRef" :multiple="false" @submit="handleCollectUserTableSubmit" />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, useTemplateRef } from 'vue'
  import {
    addConsumables,
    collectConsumables,
    ConsumablesEntity,
    delConsumables,
    exportConsumables,
    getConsumables,
    pageConsumables,
    stockInConsumables,
    updateConsumables
  } from '@/api/itam/consumables'
  import { getCategoriesTree, CategoriesEntity } from '@/api/itam/categories'
  import { listLocations, LocationsEntity } from '@/api/itam/locations'
  import { listManufacturers, ManufacturersEntity } from '@/api/itam/manufacturers'
  import { AssetType } from '@/api/itam/enums'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { MessageUtil } from '@/utils/messageUtil'
  import { download, resetFormRef } from '@/utils/business'
  import { useI18n } from 'vue-i18n'
  import UserChooseTable from '@/views/system/user/chooseTable.vue'

  defineOptions({
    name: 'ConsumablesList'
  })

  const { t } = useI18n()

  const showSearch = ref(true)

  const selectedRows = ref<ConsumablesEntity[]>([])

  // 分类树数据
  const categoryTree = ref<CategoriesEntity[]>([])
  // 存放位置树
  const locationTree = ref<LocationsEntity[]>([])
  // 制造商列表
  const manufacturerList = ref<ManufacturersEntity[]>([])

  // 加载分类数据
  const loadCategories = async () => {
    try {
      const data = await getCategoriesTree(AssetType.CONSUMABLE)
      categoryTree.value = data || []
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  // 加载存放位置树
  const loadLocations = async () => {
    try {
      const data = await listLocations()
      locationTree.value = data || []
    } catch (error) {
      console.error('加载存放位置失败:', error)
    }
  }

  // 加载制造商列表
  const loadManufacturers = async () => {
    try {
      const data = await listManufacturers()
      manufacturerList.value = data || []
    } catch (error) {
      console.error('加载制造商失败:', error)
    }
  }

  const searchForm = ref({
    name: undefined,
    categoryId: undefined,
    manufacturerId: undefined,
    itemNo: undefined,
    orderNumber: undefined
  })

  // 表单配置
  const searchFormItems = computed(() => [
    {
      label: '耗材名称',
      key: 'name',
      type: 'input',
      placeholder: '请输入耗材名称',
      clearable: true
    },
    {
      label: '物品编号/型号',
      key: 'itemNo',
      type: 'input',
      placeholder: '请输入物品编号/型号',
      labelWidth: '120px',
      clearable: true
    },
    {
      label: '采购单号',
      key: 'orderNumber',
      type: 'input',
      placeholder: '请输入采购单号',
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
      apiFn: pageConsumables,
      apiParams: {
        current: 1,
        size: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        { prop: 'name', label: '耗材名称', minWidth: 150 },
        { prop: 'itemNo', label: '物品编号/型号', minWidth: 150 },
        { prop: 'quantity', label: '当前库存', minWidth: 100 },
        { prop: 'minQuantity', label: '最小库存', minWidth: 100 },
        { prop: 'purchaseCost', label: '购买成本', minWidth: 120 },
        { prop: 'purchaseDate', label: '购买日期', minWidth: 160 },
        { prop: 'orderNumber', label: '采购单号', minWidth: 150 },
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

  const form = ref<Partial<ConsumablesEntity>>({
    id: undefined,
    name: '',
    categoryId: undefined,
    manufacturerId: undefined,
    itemNo: '',
    orderNumber: '',
    purchaseDate: '',
    purchaseCost: '',
    quantity: undefined,
    minQuantity: undefined,
    locationId: undefined
  })

  const formRules = ref({
    name: [
      {
        required: true,
        message: '耗材名称不能为空',
        trigger: 'blur'
      }
    ]
  })

  const consumablesRef = useTemplateRef('consumablesRef')
  const dialogTitle = ref('')
  const saveLoading = ref(false)

  // 表单重置
  const reset = () => {
    form.value = {
      id: undefined,
      name: '',
      categoryId: undefined,
      manufacturerId: undefined,
      itemNo: '',
      orderNumber: '',
      purchaseDate: '',
      purchaseCost: '',
      quantity: undefined,
      minQuantity: undefined,
      locationId: undefined
    }
    resetFormRef(consumablesRef)
  }

  /** 新增按钮操作 */
  const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加耗材'
  }

  /** 修改按钮操作 */
  const handleUpdate = (row: ConsumablesEntity) => {
    reset()
    if (row.id) {
      getConsumables(row.id).then((data: ConsumablesEntity) => {
        form.value = data
        open.value = true
        dialogTitle.value = '修改耗材'
      })
    }
  }

  /** 提交按钮 */
  const submitForm = () => {
    consumablesRef.value!.validate((valid: boolean) => {
      if (valid) {
        saveLoading.value = true
        if (form.value.id != null) {
          updateConsumables(form.value as ConsumablesEntity)
            .then(() => {
              MessageUtil.success(t('common.saveSuccess'))
              open.value = false
              refreshData()
            })
            .finally(() => {
              saveLoading.value = false
            })
        } else {
          addConsumables(form.value as ConsumablesEntity)
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
      .then(() => delConsumables(ids))
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
          const response = await exportConsumables(searchParams)
          download(response, '耗材数据')
        } catch (error) {
          console.error('导出失败:', error)
        } finally {
          exportLoading.value = false
        }
      })
      .catch(() => {})
  }

  // 领取相关
  const collectDialogVisible = ref(false)
  const collectForm = ref({ quantity: 1, targetId: undefined as number | undefined, note: '' })
  const collectFormRef = useTemplateRef('collectFormRef')
  const collectLoading = ref(false)
  const currentItem = ref<ConsumablesEntity | null>(null)
  const collectUser = ref<any>(null)
  const collectUserChooseTableRef = ref()

  const collectRules = {
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
    targetId: [{ required: true, message: '领取人不能为空', trigger: 'blur' }]
  }

  const handleCollectUserTableSubmit = (val: any[]) => {
    if (val && val.length > 0) {
      collectUser.value = val[0]
      collectForm.value.targetId = val[0].id
      // 清除验证错误
      collectFormRef.value?.clearValidate('targetId')
    }
  }

  const handleOpenCollectUserTable = () => {
    if (collectUserChooseTableRef.value) {
      collectUserChooseTableRef.value.showDialog(collectUser.value ? [collectUser.value] : [])
    }
  }

  const handleCollect = (row: ConsumablesEntity) => {
    currentItem.value = row
    collectForm.value = { quantity: 1, targetId: undefined, note: '' }
    collectUser.value = null
    collectDialogVisible.value = true
  }

  const submitCollect = () => {
    collectFormRef.value!.validate((valid: boolean) => {
      if (valid && currentItem.value && collectForm.value.targetId) {
        collectLoading.value = true
        collectConsumables({
          consumableId: currentItem.value.id,
          actionType: 'STOCK_OUT',
          quantity: collectForm.value.quantity,
          remainingQuantity: 0,
          note: collectForm.value.note || '',
          targetType: 'STOCK_OUT',
          targetId: collectForm.value.targetId
        })
          .then(() => {
            MessageUtil.success('领取成功')
            collectDialogVisible.value = false
            refreshData()
          })
          .finally(() => {
            collectLoading.value = false
          })
      }
    })
  }

  // 入库相关
  const stockInDialogVisible = ref(false)
  const stockInForm = ref({ quantity: 1, note: '' })
  const stockInFormRef = useTemplateRef('stockInFormRef')
  const stockInLoading = ref(false)

  const stockInRules = {
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }]
  }

  const handleStockIn = (row: ConsumablesEntity) => {
    currentItem.value = row
    stockInForm.value = { quantity: 1, note: '' }
    stockInDialogVisible.value = true
  }

  const submitStockIn = () => {
    stockInFormRef.value!.validate((valid: boolean) => {
      if (valid && currentItem.value) {
        stockInLoading.value = true
        stockInConsumables({
          consumableId: currentItem.value.id,
          actionType: 'STOCK_IN',
          quantity: stockInForm.value.quantity,
          remainingQuantity: 0,
          note: stockInForm.value.note || ''
        })
          .then(() => {
            MessageUtil.success('入库成功')
            stockInDialogVisible.value = false
            refreshData()
          })
          .finally(() => {
            stockInLoading.value = false
          })
      }
    })
  }

  onMounted(() => {
    loadCategories()
    loadLocations()
    loadManufacturers()
  })
</script>

<style scoped lang="scss"></style>
