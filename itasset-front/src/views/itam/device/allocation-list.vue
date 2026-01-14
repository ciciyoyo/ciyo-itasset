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
                        <el-button v-if="row.status === 'active'" link type="primary" @click="handleDeallocate(row)">
                            取消分配
                        </el-button>
                    </template>
                </ArtTable>
            </ElCard>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { pageAllocation, deallocateDevice } from '@/api/itam/device'
import { useTable } from '@/hooks/core/useTable'
import { ElMessageBox } from 'element-plus'
import { MessageUtil } from '@/utils/messageUtil'
import { useI18n } from 'vue-i18n'

defineOptions({
    name: 'AllocationList'
})

const { t } = useI18n()

const showSearch = ref(true)

const searchForm = ref({
    itemName: undefined,
    ownerName: undefined
})

// 表单配置
const searchFormItems = computed(() => [
    {
        label: '设备名称',
        key: 'itemName',
        type: 'input',
        placeholder: '请输入设备名称',
        clearable: true
    },
    {
        label: '使用人',
        key: 'ownerName',
        type: 'input',
        placeholder: '请输入使用人',
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
        apiFn: pageAllocation,
        apiParams: {
            current: 1,
            size: 10,
            ...searchForm.value
        },
        columnsFactory: () => [
            { prop: 'itemName', label: '设备名称', minWidth: 150 },
            { prop: 'ownerName', label: '使用人', minWidth: 120 },
            { prop: 'ownerTypeDesc', label: '使用人类型', minWidth: 100 },
            { prop: 'statusDesc', label: '状态', minWidth: 100 },
            { prop: 'assignDate', label: '分配时间', minWidth: 160 },
            { prop: 'note', label: '备注', minWidth: 200 },
            {
                prop: 'operation',
                label: '操作',
                useSlot: true,
                width: 120,
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

const handleDeallocate = (row: any) => {
    ElMessageBox.confirm(`确认要取消对 "${row.itemName}" 的分配吗？`, t('common.warning'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
    })
        .then(() => deallocateDevice(row.id))
        .then(() => {
            MessageUtil.success('取消分配成功')
            refreshData()
        })
        .catch(() => { })
}
</script>

<style scoped lang="scss">
.log-page {
    padding: 16px;
}
</style>
