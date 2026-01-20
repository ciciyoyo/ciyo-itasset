<template>
    <el-drawer v-model="visible" size="100%" direction="rtl" :show-close="true" :destroy-on-close="true"
        class="stocktake-detail-drawer">
        <template #header>
            <div class="drawer-header">
                <div class="header-title">
                    <el-icon :size="24" class="mr-2">
                        <ele-DocumentChecked />
                    </el-icon>
                    <span class="title-text">盘点详情</span>
                    <el-tag v-if="stocktakeData" :type="getStatusType(stocktakeData.status)" class="ml-3">
                        {{ stocktakeData.statusDesc || '未知' }}
                    </el-tag>
                </div>
            </div>
        </template>

        <div v-if="loading" class="loading-container">
            <el-skeleton :rows="10" animated />
        </div>

        <div v-else-if="stocktakeData" class="detail-content">
            <!-- 顶部概览卡片 -->
            <div class="overview-card">
                <div class="overview-icon">
                    <el-icon :size="64">
                        <ele-DocumentChecked />
                    </el-icon>
                </div>
                <div class="stocktake-info">
                    <h2 class="stocktake-name">{{ stocktakeData.name }}</h2>
                    <div class="info-row">
                        <span class="label">盘点位置：</span>
                        <span class="value">{{ stocktakeData.locationName || '-' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">盘点分类：</span>
                        <span class="value">{{ stocktakeData.categoryName || '-' }}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">负责人：</span>
                        <span class="value">{{ stocktakeData.managerName || '-' }}</span>
                    </div>
                </div>
                <!-- 统计信息 -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-value">{{ stocktakeData.totalCount || 0 }}</div>
                        <div class="stat-label">总数</div>
                    </div>
                    <div class="stat-card normal">
                        <div class="stat-value">{{ stocktakeData.normalCount || 0 }}</div>
                        <div class="stat-label">正常</div>
                    </div>
                    <div class="stat-card damaged">
                        <div class="stat-value">{{ stocktakeData.damagedCount || 0 }}</div>
                        <div class="stat-label">损坏</div>
                    </div>
                    <div class="stat-card scrapped">
                        <div class="stat-value">{{ stocktakeData.scrappedCount || 0 }}</div>
                        <div class="stat-label">报废</div>
                    </div>
                    <div class="stat-card unchecked">
                        <div class="stat-value">{{ stocktakeData.uncheckedCount || 0 }}</div>
                        <div class="stat-label">未盘点</div>
                    </div>
                    <div class="stat-card deficit">
                        <div class="stat-value">{{ stocktakeData.deficitCount || 0 }}</div>
                        <div class="stat-label">盘亏</div>
                    </div>
                </div>
            </div>

            <!-- 详细信息区域 -->
            <el-tabs v-model="activeTab" class="detail-tabs">
                <el-tab-pane label="基本信息" name="basic">
                    <div class="info-section">
                        <el-descriptions :column="2" border>
                            <el-descriptions-item label="任务名称">
                                {{ stocktakeData.name || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="任务状态">
                                <el-tag :type="getStatusType(stocktakeData.status)">
                                    {{ stocktakeData.statusDesc || '未知' }}
                                </el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="盘点位置">
                                {{ stocktakeData.locationName || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="盘点分类">
                                {{ stocktakeData.categoryName || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="负责人">
                                {{ stocktakeData.managerName || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="计划开始日期">
                                {{ stocktakeData.startDate || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="计划结束日期">
                                {{ stocktakeData.endDate || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="总数">
                                <span class="stat-number">{{ stocktakeData.totalCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="正常">
                                <span class="stat-number normal-text">{{ stocktakeData.normalCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="损坏">
                                <span class="stat-number damaged-text">{{ stocktakeData.damagedCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="报废">
                                <span class="stat-number scrapped-text">{{ stocktakeData.scrappedCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="盘盈">
                                <span class="stat-number surplus-text">{{ stocktakeData.surplusCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="盘亏">
                                <span class="stat-number deficit-text">{{ stocktakeData.deficitCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="未盘点">
                                <span class="stat-number unchecked-text">{{ stocktakeData.uncheckedCount || 0 }}</span>
                            </el-descriptions-item>
                            <el-descriptions-item label="创建时间">
                                {{ stocktakeData.createTime || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="备注" :span="2">
                                {{ stocktakeData.note || '暂无备注' }}
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="盘点明细" name="items">
                    <div class="info-section">
                        <ArtTable :loading="itemsLoading" :data="itemsData" :columns="itemsColumns"
                            :pagination="pagination" @pagination:size-change="handleSizeChange"
                            @pagination:current-change="handleCurrentChange">
                            <template #status="{ row }">
                                <el-tag :type="getItemStatusType(row.status)">
                                    {{ row.statusDesc || '未知' }}
                                </el-tag>
                            </template>
                            <template #operation="{ row }">
                                <el-button link type="primary" @click="handleProcess(row)">处理盘点</el-button>
                            </template>
                        </ArtTable>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>

        <template #footer>
            <div class="drawer-footer">
                <el-button @click="visible = false">关闭</el-button>
            </div>
        </template>
    </el-drawer>

    <!-- 处理盘点弹窗 -->
    <el-dialog v-model="processDialogVisible" title="处理盘点" width="500px" append-to-body>
        <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
            <el-form-item label="盘点结果" prop="status">
                <el-select v-model="processForm.status" placeholder="请选择盘点结果" class="!w-full">
                    <el-option label="正常" value="normal" />
                    <el-option label="丢失" value="lost" />
                    <el-option label="损坏" value="damaged" />
                    <el-option label="待盘点" value="pending" />
                    <el-option label="报废" value="scrapped" />
                </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="note">
                <el-input v-model="processForm.note" type="textarea" :rows="4" placeholder="请输入备注" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="processDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="processSaving" @click="handleProcessSubmit">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, useTemplateRef } from 'vue'
import { getStocktakes, StocktakesEntity } from '@/api/itam/stocktakes'
import { pageStocktakeItems, editStocktakeItems, StocktakeItemsEntity } from '@/api/itam/stocktakeItems'
import { useTable } from '@/hooks/core/useTable'
import { MessageUtil } from '@/utils/messageUtil'

const visible = ref(false)
const loading = ref(false)
const stocktakeData = ref<StocktakesEntity | null>(null)
const activeTab = ref('basic')

// 明细列表
const {
    columns: itemsColumns,
    data: itemsData,
    loading: itemsLoading,
    getData: getItemsData,
    pagination,
    handleSizeChange,
    handleCurrentChange
} = useTable({
    core: {
        apiFn: pageStocktakeItems,
        apiParams: {
            current: 1,
            size: 10,
            stocktakeId: undefined
        },
        columnsFactory: () => [
            { prop: 'id', label: 'ID', minWidth: 80 },
            { prop: 'assetName', label: '资产名称', minWidth: 150 },
            { prop: 'assetCode', label: '资产编码', minWidth: 140 },
            { prop: 'status', label: '状态', minWidth: 100, useSlot: true },
            { prop: 'expectedLocationName', label: '预期位置', minWidth: 140 },
            { prop: 'scannedByName', label: '盘点人', minWidth: 100 },
            { prop: 'scannedAt', label: '盘点时间', minWidth: 160 },
            { prop: 'note', label: '备注', minWidth: 150 },
            { prop: 'operation', label: '操作', minWidth: 100, useSlot: true, fixed: 'right' }
        ]
    }
})

// 获取盘点任务状态类型
const getStatusType = (status?: string) => {
    switch (status) {
        case 'draft': return 'info'
        case 'processing': return 'primary'
        case 'finished': return 'success'
        case 'canceled': return 'danger'
        default: return 'warning'
    }
}

// 获取盘点明细状态类型
const getItemStatusType = (status?: string) => {
    switch (status) {
        case 'normal': return 'success'      // 正常 - 绿色
        case 'lost': return 'danger'         // 丢失 - 红色
        case 'damaged': return 'warning'     // 损坏 - 橙色
        case 'pending': return 'info'        // 待盘点 - 灰色
        case 'scrapped': return 'danger'     // 报废 - 红色
        default: return 'info'
    }
}

// 加载盘点详情
const loadStocktakeDetail = async (id: number) => {
    loading.value = true
    try {
        const data = await getStocktakes(id)
        stocktakeData.value = data
    } catch (error) {
        console.error('加载盘点详情失败:', error)
    } finally {
        loading.value = false
    }
}

// 加载明细数据
const loadItems = (id: number) => {
    getItemsData({
        current: pagination.current,
        size: pagination.size,
        stocktakeId: String(id)
    })
}

// 打开抽屉
const open = (row: StocktakesEntity) => {
    visible.value = true
    activeTab.value = 'basic'

    if (row.id) {
        // 加载基本信息
        loadStocktakeDetail(row.id)
        // 加载明细列表
        loadItems(row.id)
    }
}

// 处理盘点弹窗
const processDialogVisible = ref(false)
const processSaving = ref(false)
const processFormRef = useTemplateRef('processFormRef')
const processForm = ref({
    id: 0,
    status: '',
    note: ''
})

const processRules = {
    status: [
        { required: true, message: '请选择盘点结果', trigger: 'change' }
    ]
}

// 打开处理盘点弹窗
const handleProcess = (row: StocktakeItemsEntity) => {
    processForm.value = {
        id: row.id,
        status: row.status || '',
        note: row.note || ''
    }
    processDialogVisible.value = true
}

// 提交处理
const handleProcessSubmit = () => {
    processFormRef.value?.validate(async (valid: boolean) => {
        if (valid) {
            processSaving.value = true
            try {
                await editStocktakeItems(processForm.value)
                MessageUtil.success('处理成功')
                processDialogVisible.value = false
                // 重新加载明细列表
                if (stocktakeData.value?.id) {
                    loadItems(stocktakeData.value.id)
                    // 重新加载基本信息（更新统计数据）
                    loadStocktakeDetail(stocktakeData.value.id)
                }
            } catch (error) {
                console.error('处理失败:', error)
            } finally {
                processSaving.value = false
            }
        }
    })
}

defineExpose({
    open
})
</script>

<style scoped lang="scss">
.stocktake-detail-drawer {
    :deep(.el-drawer__header) {
        margin-bottom: 0;
        padding: 16px 20px;
        border-bottom: 1px solid var(--el-border-color-light);
    }

    :deep(.el-drawer__body) {
        padding: 0;
    }

    :deep(.el-drawer__footer) {
        padding: 12px 20px;
        border-top: 1px solid var(--el-border-color-light);
    }
}

.drawer-header {
    .header-title {
        display: flex;
        align-items: center;

        .el-icon {
            display: flex;
            align-items: center;
        }

        .title-text {
            font-size: 18px;
            font-weight: 600;
            color: var(--el-text-color-primary);
            line-height: 1;
        }
    }
}

.loading-container {
    padding: 24px;
}

.detail-content {
    padding: 24px;
    height: calc(100vh - 140px);
    overflow-y: auto;
}

.overview-card {
    display: flex;
    gap: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #3b82f6 0%, #1e40af 100%);
    border-radius: 12px;
    margin-bottom: 24px;
    color: #fff;
    align-items: center;

    .overview-icon {
        flex-shrink: 0;
        width: 120px;
        height: 120px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
        color: rgba(255, 255, 255, 0.95);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .stocktake-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .stocktake-name {
            font-size: 28px;
            font-weight: 600;
            margin: 0 0 16px 0;
        }

        .info-row {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            font-size: 14px;

            .label {
                color: rgba(255, 255, 255, 0.85);
                margin-right: 8px;
            }

            .value {
                color: #fff;
                font-weight: 500;
            }
        }
    }

    .stats-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        margin-left: auto;
        min-width: 420px;

        .stat-card {
            background: rgba(255, 255, 255, 0.2);
            border-radius: 8px;
            padding: 16px;
            text-align: center;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.25);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            .stat-value {
                font-size: 28px;
                font-weight: 700;
                color: #fff;
                margin-bottom: 4px;
            }

            .stat-label {
                font-size: 12px;
                color: rgba(255, 255, 255, 0.9);
                font-weight: 500;
            }

            &.normal {
                background: linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(5, 150, 105, 0.3));
                border-color: rgba(16, 185, 129, 0.4);

                .stat-value {
                    color: #6ee7b7;
                }
            }

            &.damaged {
                background: linear-gradient(135deg, rgba(251, 146, 60, 0.3), rgba(234, 88, 12, 0.3));
                border-color: rgba(251, 146, 60, 0.4);

                .stat-value {
                    color: #fcd34d;
                }
            }

            &.scrapped {
                background: linear-gradient(135deg, rgba(220, 38, 38, 0.3), rgba(153, 27, 27, 0.3));
                border-color: rgba(220, 38, 38, 0.4);

                .stat-value {
                    color: #fca5a5;
                }
            }

            &.surplus {
                background: linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(5, 150, 105, 0.3));
                border-color: rgba(16, 185, 129, 0.4);

                .stat-value {
                    color: #6ee7b7;
                }
            }

            &.deficit {
                background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(220, 38, 38, 0.3));
                border-color: rgba(239, 68, 68, 0.4);

                .stat-value {
                    color: #fca5a5;
                }
            }

            &.unchecked {
                background: linear-gradient(135deg, rgba(251, 146, 60, 0.3), rgba(234, 88, 12, 0.3));
                border-color: rgba(251, 146, 60, 0.4);

                .stat-value {
                    color: #fcd34d;
                }
            }
        }
    }
}

.detail-tabs {
    :deep(.el-tabs__header) {
        margin-bottom: 16px;
    }

    :deep(.el-tabs__item) {
        font-size: 15px;
    }
}

.info-section {
    background: var(--el-bg-color);
    border-radius: 8px;
    padding: 16px;

    :deep(.el-descriptions__label) {
        width: 120px;
        font-weight: 500;
    }

    .stat-number {
        font-size: 18px;
        font-weight: 600;
    }

    .normal-text {
        color: var(--el-color-success);
    }

    .damaged-text {
        color: var(--el-color-warning);
    }

    .scrapped-text {
        color: #dc2626; // 深红色
    }

    .surplus-text {
        color: var(--el-color-success);
    }

    .deficit-text {
        color: var(--el-color-danger);
    }

    .unchecked-text {
        color: var(--el-color-warning);
    }
}

.drawer-footer {
    display: flex;
    justify-content: flex-end;
}

.ml-2 {
    margin-left: 8px;
}

.ml-3 {
    margin-left: 12px;
}

.mr-2 {
    margin-right: 8px;
}
</style>
