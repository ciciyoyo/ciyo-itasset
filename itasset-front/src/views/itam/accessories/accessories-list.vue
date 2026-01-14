<template>
    <div class="log-page art-full-height">
        <div class="flex flex-col h-full">
            <ArtSearchBar v-if="showSearch" ref="searchBarRef" v-model="searchForm" :items="searchFormItems"
                @reset="resetSearchParams" @search="handleSearch">
            </ArtSearchBar>
            <ElCard class="art-table-card flex flex-col flex-1 min-h-0" shadow="never"
                :style="{ 'margin-top': showSearch ? '12px' : '0' }">
                <ArtTableHeader v-model:columns="columnChecks" v-model:showSearchBar="showSearch" :loading="loading"
                    @refresh="refreshData">
                    <template #left>
                        <ElSpace wrap>
                            <el-button type="primary" icon="ele-Plus" @click="handleAdd" v-ripple
                                v-hasPermi="['itam:accessories:add']">
                                {{ $t('common.add') }}
                            </el-button>
                            <el-button type="danger" v-ripple icon="ele-Delete" @click="handleDelete"
                                v-hasPermi="['itam:accessories:delete']">
                                {{ $t('common.delete') }}
                            </el-button>
                            <el-button v-hasPermi="['itam:accessories:export']" icon="ele-Download" v-ripple
                                @click="handleExport">
                                {{ $t('common.export') }}
                            </el-button>
                        </ElSpace>
                    </template>
                </ArtTableHeader>

                <!-- Table -->
                <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
                    @selection-change="handleSelectionChange" @pagination:size-change="handleSizeChange"
                    @pagination:current-change="handleCurrentChange">
                    <template #operation="{ row }">
                        <el-button link type="primary" @click="handleUpdate(row)"
                            v-hasPermi="['itam:accessories:update']">
                            {{ $t('system.roleManagement.edit') }}
                        </el-button>
                        <el-button link type="primary" @click="handleAllocate(row)"
                            v-hasPermi="['itam:accessories:allocate']">
                            分配设备
                        </el-button>
                        <el-button link type="danger" @click="handleDelete(row)"
                            v-hasPermi="['itam:accessories:delete']">
                            {{ $t('system.roleManagement.delete') }}
                        </el-button>
                    </template>
                </ArtTable>
            </ElCard>
        </div>
        <!-- 添加或修改配件对话框 -->
        <el-dialog :title="dialogTitle" v-model="open" width="720px" append-to-body>
            <el-form ref="accessoriesRef" :model="form" :rules="formRules" label-width="120px">
                <!-- 基本信息 -->
                <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-2">基本信息</h3>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="配件名称" prop="name">
                            <el-input v-model="form.name" placeholder="请输入配件名称" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="资产编号" prop="assetNumber">
                            <el-input v-model="form.assetNumber" placeholder="请输入资产编号" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="分类" prop="categoryId">
                            <el-tree-select v-model="form.categoryId" :data="categoryTree" check-strictly
                                :render-after-expand="false" placeholder="请选择分类" clearable node-key="id"
                                :props="{ label: 'name', children: 'children', value: 'id' }" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="数量" prop="quantity">
                            <el-input-number v-model="form.quantity" placeholder="请输入数量" controls-position="right"
                                :min="0" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="规格" prop="specifications">
                            <el-input v-model="form.specifications" placeholder="请输入规格" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="配件序列号" prop="serialNumber">
                            <el-input v-model="form.serialNumber" placeholder="请输入配件序列号" clearable />
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 更多信息 -->
                <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-6">更多信息</h3>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="供应商" prop="supplierId">
                            <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable
                                class="!w-full">
                                <el-option v-for="item in supplierList" :key="item.id" :label="item.name"
                                    :value="item.id!" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="制造商" prop="manufacturerId">
                            <el-select v-model="form.manufacturerId" placeholder="请选择制造商" clearable filterable
                                class="!w-full">
                                <el-option v-for="item in manufacturerList" :key="item.id" :label="item.name"
                                    :value="item.id!" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="存放位置" prop="locationId">
                            <el-tree-select v-model="form.locationId" :data="locationTree" check-strictly
                                :render-after-expand="false" placeholder="请选择存放位置" clearable node-key="id"
                                :props="{ label: 'name', children: 'children', value: 'id' }" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="最小库存预警" prop="minQuantity">
                            <el-input-number v-model="form.minQuantity" placeholder="请输入最小库存预警"
                                controls-position="right" :min="0" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="购买日期" prop="purchaseDate">
                            <el-date-picker clearable v-model="form.purchaseDate" type="datetime"
                                value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择购买日期" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="过保日期" prop="warrantyExpirationDate">
                            <el-date-picker clearable v-model="form.warrantyExpirationDate" type="datetime"
                                value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择过保日期" class="!w-full" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="购买成本" prop="purchaseCost">
                            <el-input v-model="form.purchaseCost" placeholder="请输入购买成本" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="订单号" prop="orderNumber">
                            <el-input v-model="form.orderNumber" placeholder="请输入订单号" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="折旧规则" prop="depreciationId">
                            <el-select v-model="form.depreciationId" placeholder="请选择折旧规则" clearable filterable
                                class="!w-full">
                                <el-option v-for="item in depreciationList" :key="item.id" :label="item.name"
                                    :value="item.id!" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="描述" prop="description">
                            <el-input v-model="form.description" type="textarea" placeholder="请输入描述" :rows="3" />
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="cancel">{{ $t('common.cancel') }}</el-button>
                    <el-button :loading="saveLoading" type="primary" @click="submitForm">{{ $t('common.submit') }}
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 分配设备对话框 -->
        <AssignToDeviceModal ref="assignToDeviceModalRef" @success="refreshData" />
    </div>
</template>

<script setup lang="ts">
import { computed, ref, useTemplateRef, onMounted } from 'vue'
import {
    AccessoriesEntity,
    addAccessories,
    delAccessories,
    exportAccessories,
    getAccessories,
    pageAccessories,
    updateAccessories
} from '@/api/itam/accessories'
import { getCategoriesTree, CategoriesEntity } from '@/api/itam/categories'
import { listSuppliers, SuppliersEntity } from '@/api/itam/suppliers'
import { listManufacturers, ManufacturersEntity } from '@/api/itam/manufacturers'
import { listLocations, LocationsEntity } from '@/api/itam/locations'
import { listDepreciations, DepreciationsEntity } from '@/api/itam/depreciations'
import { AssetType } from '@/api/itam/enums'
import { ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'
import { download, resetFormRef } from '@/utils/business'
import { useI18n } from 'vue-i18n'
import AssignToDeviceModal from './components/AssignToDeviceModal.vue'

defineOptions({
    name: 'AccessoriesList'
})

const { t } = useI18n()

const showSearch = ref(true)
const selectedRows = ref<AccessoriesEntity[]>([])

// 分类树数据
const categoryTree = ref<CategoriesEntity[]>([])
// 供应商列表
const supplierList = ref<SuppliersEntity[]>([])
// 制造商列表
const manufacturerList = ref<ManufacturersEntity[]>([])
// 存放位置树
const locationTree = ref<LocationsEntity[]>([])
// 折旧规则列表
const depreciationList = ref<DepreciationsEntity[]>([])

// 加载分类数据
const loadCategories = async () => {
    try {
        const data = await getCategoriesTree(AssetType.ACCESSORY)
        categoryTree.value = data || []
    } catch (error) {
        console.error('加载分类失败:', error)
    }
}

// 加载供应商列表
const loadSuppliers = async () => {
    try {
        const data = await listSuppliers()
        supplierList.value = data || []
    } catch (error) {
        console.error('加载供应商失败:', error)
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

// 加载存放位置树
const loadLocations = async () => {
    try {
        const data = await listLocations()
        locationTree.value = data || []
    } catch (error) {
        console.error('加载存放位置失败:', error)
    }
}

// 加载折旧规则列表
const loadDepreciations = async () => {
    try {
        const data = await listDepreciations()
        depreciationList.value = data || []
    } catch (error) {
        console.error('加载折旧规则失败:', error)
    }
}

const searchForm = ref({
    name: undefined,
    assetNumber: undefined,
    serialNumber: undefined
})

// 表单配置
const searchFormItems = computed(() => [
    {
        label: '配件名称',
        key: 'name',
        type: 'input',
        placeholder: '请输入配件名称',
        clearable: true
    },
    {
        label: '资产编号',
        key: 'assetNumber',
        type: 'input',
        placeholder: '请输入资产编号',
        clearable: true
    },
    {
        label: '序列号',
        key: 'serialNumber',
        type: 'input',
        placeholder: '请输入配件序列号',
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
        apiFn: pageAccessories,
        apiParams: {
            current: 1,
            size: 10,
            ...searchForm.value
        },
        columnsFactory: () => [
            { type: 'selection' },
            { prop: 'name', label: '配件名称', minWidth: 150 },
            { prop: 'assetNumber', label: '资产编号', minWidth: 150 },
            { prop: 'specifications', label: '规格', minWidth: 120 },
            { prop: 'quantity', label: '数量', minWidth: 100 },
            { prop: 'minQuantity', label: '最小库存', minWidth: 100 },
            { prop: 'purchaseDate', label: '购买日期', minWidth: 160 },
            { prop: 'warrantyExpirationDate', label: '过保日期', minWidth: 160 },
            { prop: 'purchaseCost', label: '购买成本', minWidth: 120 },
            { prop: 'serialNumber', label: '序列号', minWidth: 150 },
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

const form = ref<Partial<AccessoriesEntity>>({
    id: undefined,
    name: '',
    assetNumber: '',
    categoryId: undefined,
    supplierId: undefined,
    manufacturerId: undefined,
    specifications: '',
    locationId: undefined,
    depreciationId: undefined,
    quantity: undefined,
    minQuantity: undefined,
    purchaseDate: '',
    warrantyExpirationDate: '',
    purchaseCost: '',
    orderNumber: '',
    serialNumber: '',
    description: ''
})

const formRules = ref({
    name: [
        {
            required: true,
            message: '配件名称不能为空',
            trigger: 'blur'
        }
    ],
    categoryId: [
        {
            required: true,
            message: '请选择分类',
            trigger: 'change'
        }
    ],
    quantity: [
        {
            required: true,
            message: '请输入数量',
            trigger: 'blur'
        }
    ]
})

const accessoriesRef = useTemplateRef('accessoriesRef')
const dialogTitle = ref('')
const saveLoading = ref(false)

// 表单重置
const reset = () => {
    form.value = {
        id: undefined,
        name: '',
        assetNumber: '',
        categoryId: undefined,
        supplierId: undefined,
        manufacturerId: undefined,
        specifications: '',
        locationId: undefined,
        depreciationId: undefined,
        quantity: undefined,
        minQuantity: undefined,
        purchaseDate: '',
        warrantyExpirationDate: '',
        purchaseCost: '',
        orderNumber: '',
        serialNumber: '',
        description: ''
    }
    resetFormRef(accessoriesRef)
}

/** 新增按钮操作 */
const handleAdd = () => {
    reset()
    open.value = true
    dialogTitle.value = '添加配件'
}

/** 修改按钮操作 */
const handleUpdate = (row: AccessoriesEntity) => {
    reset()
    if (row.id) {
        getAccessories(row.id).then((data: AccessoriesEntity) => {
            form.value = data
            open.value = true
            dialogTitle.value = '修改配件'
        })
    }
}

/** 提交按钮 */
const submitForm = () => {
    accessoriesRef.value!.validate((valid: boolean) => {
        if (valid) {
            saveLoading.value = true
            if (form.value.id != null) {
                updateAccessories(form.value as AccessoriesEntity)
                    .then(() => {
                        MessageUtil.success(t('common.saveSuccess'))
                        open.value = false
                        refreshData()
                    })
                    .finally(() => {
                        saveLoading.value = false
                    })
            } else {
                addAccessories(form.value as AccessoriesEntity)
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
        .then(() => delAccessories(ids))
        .then(() => {
            refreshData()
            MessageUtil.success(t('common.deleteSuccess'))
        })
        .catch(() => { })
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
                const response = await exportAccessories(searchParams)
                download(response, '配件数据')
            } catch (error) {
                console.error('导出失败:', error)
            } finally {
                exportLoading.value = false
            }
        })
        .catch(() => { })
}

// 分配设备modal引用
const assignToDeviceModalRef = useTemplateRef('assignToDeviceModalRef')

// 分配设备
const handleAllocate = (row: AccessoriesEntity) => {
    assignToDeviceModalRef.value?.open(row)
}

onMounted(() => {
    loadCategories()
    loadSuppliers()
    loadManufacturers()
    loadLocations()
    loadDepreciations()
})
</script>

<style scoped lang="scss"></style>
