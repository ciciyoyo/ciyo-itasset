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
                </ArtTableHeader>

                <!-- Table -->
                <ArtTable class="flex-1" :loading="loading" :data="data" :columns="columns" :pagination="pagination"
                    @pagination:size-change="handleSizeChange" @pagination:current-change="handleCurrentChange">
                    <template #operation="{ row }">
                        <el-button link type="warning" @click="handleReport(row)"
                            v-hasPermi="['itam:accessories:report']">
                            报告异常
                        </el-button>
                        <el-button link type="primary" @click="handleDeallocate(row)"
                            v-hasPermi="['itam:accessories:deallocate']">
                            解除关联
                        </el-button>
                    </template>
                </ArtTable>
            </ElCard>
        </div>

        <!-- 报告异常对话框 -->
        <el-dialog title="报告异常" v-model="reportDialogVisible" width="500px" append-to-body @close="resetReportForm">
            <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="100px">
                <el-form-item label="故障名称" prop="failureName">
                    <el-input v-model="reportForm.failureName" placeholder="请输入故障名称" clearable />
                </el-form-item>
                <el-form-item label="故障描述" prop="failureDescription">
                    <el-input v-model="reportForm.failureDescription" type="textarea" :rows="3" placeholder="请输入故障描述" />
                </el-form-item>
                <el-form-item label="故障时间" prop="failureDate">
                    <el-date-picker v-model="reportForm.failureDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="请选择故障时间" class="!w-full" clearable />
                </el-form-item>
                <el-form-item label="备注" prop="notes">
                    <el-input v-model="reportForm.notes" type="textarea" :rows="2" placeholder="请输入备注" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="reportDialogVisible = false">取消</el-button>
                    <el-button :loading="reportLoading" type="primary" @click="submitReport">提交</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, useTemplateRef } from 'vue'
import { pageAccessoryAllocations, deallocateAccessory, reportAccessoryFailure, AccessoryAllocationEntity } from '@/api/itam/accessories'
import { ElMessageBox } from 'element-plus'
import { MessageUtil } from '@/utils/messageUtil'
import { resetFormRef } from '@/utils/business'
import { useTable } from '@/hooks/core/useTable'
import { useI18n } from 'vue-i18n'

defineOptions({
    name: 'AccessoriesAllocations'
})

const { t } = useI18n()

const showSearch = ref(true)

const searchForm = ref({
    itemName: undefined,
    ownerName: undefined,
    status: undefined
})

// 表单配置
const searchFormItems = computed(() => [
    {
        label: '配件名称',
        key: 'itemName',
        type: 'input',
        placeholder: '请输入配件名称',
        clearable: true
    },
    {
        label: '设备名称',
        key: 'ownerName',
        type: 'input',
        placeholder: '请输入设备名称',
        clearable: true
    },
    {
        label: '状态',
        key: 'status',
        type: 'select',
        placeholder: '请选择状态',
        clearable: true,
        options: [
            { label: '占用中', value: 'active' },
            { label: '已归还', value: 'returned' }
        ]
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
        apiFn: pageAccessoryAllocations,
        apiParams: {
            current: 1,
            size: 10,
            ...searchForm.value
        },
        columnsFactory: () => [
            { prop: 'id', label: 'ID', minWidth: 80 },
            { prop: 'itemName', label: '配件名称', minWidth: 150 },
            { prop: 'ownerName', label: '设备名称', minWidth: 150 },
            { prop: 'quantity', label: '数量', minWidth: 100 },
            { prop: 'statusDesc', label: '状态', minWidth: 100 },
            { prop: 'assignDate', label: '分配时间', minWidth: 160 },
            { prop: 'returnDate', label: '归还时间', minWidth: 160 },
            { prop: 'note', label: '备注', minWidth: 200 },
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

// 解除关联
const handleDeallocate = (row: AccessoryAllocationEntity) => {
    ElMessageBox.confirm(
        `确认解除配件「${row.itemName}」与设备「${row.ownerName}」的关联吗？`,
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    )
        .then(() => deallocateAccessory(row.id))
        .then(() => {
            MessageUtil.success('解除关联成功')
            refreshData()
        })
        .catch(() => { })
}

// 报告异常相关
const reportDialogVisible = ref(false)
const reportLoading = ref(false)
const reportFormRef = useTemplateRef('reportFormRef')
const currentAllocation = ref<AccessoryAllocationEntity | null>(null)

const reportForm = ref({
    failureName: '',
    failureDescription: '',
    failureDate: '',
    notes: ''
})

const reportRules = {
    failureName: [
        { required: true, message: '请输入故障名称', trigger: 'blur' }
    ]
}

const resetReportForm = () => {
    reportForm.value = {
        failureName: '',
        failureDescription: '',
        failureDate: '',
        notes: ''
    }
    currentAllocation.value = null
    resetFormRef(reportFormRef)
}

const handleReport = (row: AccessoryAllocationEntity) => {
    resetReportForm()
    currentAllocation.value = row
    reportDialogVisible.value = true
}

const submitReport = () => {
    reportFormRef.value!.validate((valid: boolean) => {
        if (valid && currentAllocation.value) {
            reportLoading.value = true
            reportAccessoryFailure({
                targetId: currentAllocation.value.id,
                failureName: reportForm.value.failureName,
                failureDescription: reportForm.value.failureDescription || undefined,
                failureDate: reportForm.value.failureDate || undefined,
                notes: reportForm.value.notes || undefined
            })
                .then(() => {
                    MessageUtil.success('报告异常成功')
                    reportDialogVisible.value = false
                    refreshData()
                })
                .finally(() => {
                    reportLoading.value = false
                })
        }
    })
}
</script>

<style scoped lang="scss"></style>
