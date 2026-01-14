<template>
    <div class="stats-page p-4">
        <el-row :gutter="16">
            <!-- 统计卡片 -->
            <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon primary">
                            <el-icon :size="24">
                                <Grid />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-label">配件总数</div>
                            <div class="stats-value">{{ summary.total }}</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon danger">
                            <el-icon :size="24">
                                <WarningFilled />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-label">已过保</div>
                            <div class="stats-value">{{ summary.expired }}</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon warning">
                            <el-icon :size="24">
                                <Clock />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-label">即将过保</div>
                            <div class="stats-value">{{ summary.soonToExpire }}</div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon info">
                            <el-icon :size="24">
                                <Box />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-label">低库存</div>
                            <div class="stats-value">{{ summary.lowStock }}</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 月度价值趋势图 -->
        <div class="art-card p-5 mt-4">
            <div class="art-card-header">
                <div class="title">
                    <h4>月度价值趋势</h4>
                </div>
            </div>
            <ArtLineChart height="400px" :data="chartData" :xAxisData="xAxisData" :showLegend="false"
                :showAxisLabel="true" :showAxisLine="false" :showSplitLine="true" :axisLabelRotate="45" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAccessoriesStatsSummary, getAccessoriesMonthlyValue, AccessoryStatsSummary, MonthlyValueStat } from '@/api/itam/accessories'
import { Grid, WarningFilled, Clock, Box } from '@element-plus/icons-vue'

defineOptions({
    name: 'AccessoriesStats'
})

const summary = ref<AccessoryStatsSummary>({
    total: 0,
    expired: 0,
    lowStock: 0,
    soonToExpire: 0
})

const chartData = ref<{ name: string; data: number[] }[]>([])
const xAxisData = ref<string[]>([])

// 加载统计摘要
const loadSummary = async () => {
    try {
        const data = await getAccessoriesStatsSummary()
        summary.value = data
    } catch (error) {
        console.error('加载统计摘要失败:', error)
    }
}

// 加载月度数据
const loadMonthlyData = async () => {
    try {
        const data = await getAccessoriesMonthlyValue()
        if (data && Array.isArray(data)) {
            xAxisData.value = data.map((item: MonthlyValueStat) => item.statsMonth)
            chartData.value = [
                {
                    name: '配件价值',
                    data: data.map((item: MonthlyValueStat) => item.totalValue)
                }
            ]
        }
    } catch (error) {
        console.error('加载月度数据失败:', error)
    }
}

onMounted(() => {
    loadSummary()
    loadMonthlyData()
})
</script>

<style scoped lang="scss">
.stats-page {
    .stats-card {
        .stats-content {
            display: flex;
            align-items: center;
            gap: 16px;

            .stats-icon {
                width: 60px;
                height: 60px;
                border-radius: 12px;
                display: flex;
                align-items: center;
                justify-content: center;

                &.primary {
                    background-color: rgba(64, 158, 255, 0.1);
                    color: #409EFF;
                }

                &.success {
                    background-color: rgba(103, 194, 58, 0.1);
                    color: #67C23A;
                }

                &.warning {
                    background-color: rgba(230, 162, 60, 0.1);
                    color: #E6A23C;
                }

                &.danger {
                    background-color: rgba(245, 108, 108, 0.1);
                    color: #F56C6C;
                }

                &.info {
                    background-color: rgba(144, 147, 153, 0.1);
                    color: #909399;
                }
            }

            .stats-info {
                flex: 1;

                .stats-label {
                    font-size: 14px;
                    color: #909399;
                    margin-bottom: 8px;
                }

                .stats-value {
                    font-size: 24px;
                    font-weight: bold;
                    color: #303133;
                }
            }
        }
    }
}
</style>
