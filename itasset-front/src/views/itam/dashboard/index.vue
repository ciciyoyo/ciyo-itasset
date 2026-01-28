<template>
  <div class="dashboard-container">
    <!-- 概览卡片 -->
    <div class="overview-cards">
      <div class="stat-card primary" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Goods />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">资产总数</span>
          <span class="card-value">{{ summary?.totalAssetCount || 0 }}</span>
          <span class="card-trend">
            <el-icon><ele-InfoFilled /></el-icon>
            累计统计
          </span>
        </div>
      </div>

      <div class="stat-card success" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Wallet />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">资产总值</span>
          <span class="card-value">¥{{ formatNumber(summary?.totalAssetAmount) }}</span>
          <span class="card-trend">
            <el-icon><ele-InfoFilled /></el-icon>
            市场价值
          </span>
        </div>
      </div>

      <div class="stat-card warning" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Monitor />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">设备数量</span>
          <span class="card-value">{{ summary?.totalDevices || 0 }}</span>
          <span class="card-trend">硬件设备</span>
        </div>
      </div>

      <div class="stat-card info" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Box />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">软件许可</span>
          <span class="card-value">{{ summary?.totalSoftware || 0 }}</span>
          <span class="card-trend">授权软件</span>
        </div>
      </div>

      <div class="stat-card purple" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Connection />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">配件</span>
          <span class="card-value">{{ summary?.totalAccessories || 0 }}</span>
          <span class="card-trend">配套配件</span>
        </div>
      </div>

      <div class="stat-card cyan" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Service />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">服务</span>
          <span class="card-value">{{ summary?.totalServices || 0 }}</span>
          <span class="card-trend">维护服务</span>
        </div>
      </div>

      <div class="stat-card orange" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-Coordinate />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">耗材</span>
          <span class="card-value">{{ summary?.totalConsumables || 0 }}</span>
          <span class="card-trend">日常耗材</span>
        </div>
      </div>

      <div class="stat-card gradient" v-loading="summaryLoading">
        <div class="card-icon">
          <el-icon :size="32">
            <ele-DataLine />
          </el-icon>
        </div>
        <div class="card-content">
          <span class="card-label">资产类型</span>
          <span class="card-value">{{ assetTypesCount }}</span>
          <span class="card-trend">覆盖范围</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 年度资产价值趋势 -->
      <div class="chart-card large">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><ele-TrendCharts /></el-icon>
            年度资产价值趋势
          </h3>
          <el-tag type="info" effect="plain">近12个月</el-tag>
        </div>
        <ArtLineChart
          :data="yearlyChartData"
          :x-axis-data="yearlyMonths"
          :loading="yearlyLoading"
          :show-area-color="true"
          :smooth="true"
          :colors="chartColors"
          :show-legend="true"
          height="350px"
        />
      </div>

      <!-- 月度趋势 -->
      <div class="chart-card medium">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><ele-Histogram /></el-icon>
            月度资产变动
          </h3>
          <el-select v-model="selectedAssetsType" @change="handleAssetTypeChange" class="asset-type-select" size="default">
            <el-option label="设备" value="DEVICE" />
            <el-option label="配件" value="ACCESSORY" />
            <el-option label="耗材" value="CONSUMABLE" />
            <el-option label="软件授权" value="LICENSE" />
            <el-option label="服务" value="SERVICE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </div>
        <ArtLineChart
          :data="monthlyChartData"
          :x-axis-data="monthlyMonths"
          :loading="monthlyLoading"
          :show-area-color="true"
          :smooth="true"
          :colors="chartColors"
          height="350px"
        />
      </div>

      <!-- 资产分布 -->
      <div class="chart-card medium">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><ele-PieChart /></el-icon>
            资产类型分布
          </h3>
        </div>
        <ArtRingChart
          :data="distributionChartData"
          :loading="distributionLoading"
          :show-legend="true"
          legend-position="right"
          :colors="chartColors"
          :show-label="true"
          :radius="['45%', '70%']"
          :border-radius="8"
          center-text="资产分布"
          height="350px"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import {
    getYearlyAssetsValue,
    getMonthlyTrend,
    getDistribution,
    getSummary,
    type YearlyAssetsValueData,
    type MonthlyTrendData,
    type DistributionData,
    type SummaryData
  } from '@/api/itam/reports'
  import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'
  import ArtRingChart from '@/components/core/charts/art-ring-chart/index.vue'

  const summaryLoading = ref(false)
  const yearlyLoading = ref(false)
  const monthlyLoading = ref(false)
  const distributionLoading = ref(false)

  const summary = ref<SummaryData | null>(null)
  const yearlyData = ref<YearlyAssetsValueData | null>(null)
  const monthlyData = ref<MonthlyTrendData | null>(null)
  const distributionData = ref<DistributionData | null>(null)

  // 资产类型选择 - 默认为设备
  const selectedAssetsType = ref<string>('DEVICE')

  // 图表自定义颜色 - 确保所有颜色在白色背景上都清晰可见
  const chartColors = [
    '#3b82f6', // 蓝色 - 设备
    '#10b981', // 绿色 - 配件
    '#f59e0b', // 橙色 - 耗材
    '#8b5cf6', // 紫色 - 软件授权
    '#ec4899', // 粉色 - 服务
    '#06b6d4' // 青色 - 其他
  ]

  // 格式化数字
  const formatNumber = (num: number | undefined) => {
    if (!num) return '0'
    return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
  }

  // 年度图表数据 - 需要将API返回的数组转换为图表格式
  const yearlyMonths = computed(() => {
    if (!yearlyData.value?.length) return []
    // 获取所有唯一的月份
    const months = [...new Set(yearlyData.value.map((item) => item.statsMonth))]
    return months.sort()
  })

  const yearlyChartData = computed(() => {
    if (!yearlyData.value?.length) return []

    // 按资产类型分组
    const typeMap = new Map<string, { name: string; data: number[] }>()

    yearlyData.value.forEach((item) => {
      if (!typeMap.has(item.assetsType)) {
        typeMap.set(item.assetsType, {
          name: item.assetsTypeDesc,
          data: []
        })
      }
    })

    // 为每个类型填充数据
    const months = yearlyMonths.value
    typeMap.forEach((typeData, type) => {
      months.forEach((month) => {
        const item = yearlyData.value?.find((d) => d.statsMonth === month && d.assetsType === type)
        typeData.data.push(item?.totalValue || 0)
      })
    })

    return Array.from(typeMap.values())
  })

  // 月度图表数据 - 将API返回的数组转换为图表格式
  const monthlyMonths = computed(() => {
    if (!monthlyData.value?.length) return []
    return monthlyData.value.map((item) => item.statsMonth).sort()
  })

  // 月度折线图数据 - 显示选定资产类型的价值趋势
  const monthlyChartData = computed(() => {
    if (!monthlyData.value?.length) return []
    return monthlyData.value.map((item) => item.totalValue)
  })

  // 分布图表数据 - 将API返回的数组转换为环形图格式
  const distributionChartData = computed(() => {
    if (!distributionData.value?.length) return []
    return distributionData.value.map((item) => ({
      name: item.assetsTypeDesc,
      value: item.totalValue
    }))
  })

  // 计算有多少种资产类型
  const assetTypesCount = computed(() => {
    if (!summary.value) return 0
    let count = 0
    if (summary.value.totalDevices > 0) count++
    if (summary.value.totalSoftware > 0) count++
    if (summary.value.totalAccessories > 0) count++
    if (summary.value.totalServices > 0) count++
    if (summary.value.totalConsumables > 0) count++
    return count
  })

  // 加载摘要数据
  const loadSummary = async () => {
    summaryLoading.value = true
    try {
      const data = await getSummary()
      summary.value = data
    } catch (error) {
      console.error('加载摘要数据失败:', error)
    } finally {
      summaryLoading.value = false
    }
  }

  // 加载年度数据
  const loadYearlyData = async () => {
    yearlyLoading.value = true
    try {
      const data = await getYearlyAssetsValue()
      yearlyData.value = data
    } catch (error) {
      console.error('加载年度数据失败:', error)
    } finally {
      yearlyLoading.value = false
    }
  }

  // 加载月度数据
  const loadMonthlyData = async () => {
    monthlyLoading.value = true
    try {
      const data = await getMonthlyTrend(selectedAssetsType.value || undefined)
      monthlyData.value = data
    } catch (error) {
      console.error('加载月度数据失败:', error)
    } finally {
      monthlyLoading.value = false
    }
  }

  // 资产类型改变时重新加载
  const handleAssetTypeChange = () => {
    loadMonthlyData()
  }

  // 加载分布数据
  const loadDistributionData = async () => {
    distributionLoading.value = true
    try {
      const data = await getDistribution()
      distributionData.value = data
    } catch (error) {
      console.error('加载分布数据失败:', error)
    } finally {
      distributionLoading.value = false
    }
  }

  // 刷新所有数据
  const refreshAll = () => {
    loadSummary()
    loadYearlyData()
    loadMonthlyData()
    loadDistributionData()
  }

  onMounted(() => {
    refreshAll()
  })
</script>

<style scoped lang="scss">
  .dashboard-container {
    padding: 24px;
    background: #f0f2f5;
    min-height: 100vh;
  }

  .overview-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 24px;
    margin-bottom: 32px;

    .stat-card {
      position: relative;
      display: flex;
      gap: 20px;
      padding: 28px;
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
        background: linear-gradient(90deg, currentColor, transparent);
        opacity: 0.6;
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }

      .card-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        border-radius: 12px;
        background: rgba(0, 0, 0, 0.05);

        .el-icon {
          color: currentColor;
        }
      }

      .card-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .card-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }

        .card-value {
          font-size: 28px;
          font-weight: 700;
          color: #1f2937;
          margin-bottom: 4px;
        }

        .card-trend {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #666;

          &.positive {
            color: #10b981;
          }

          &.negative {
            color: #ef4444;
          }
        }
      }

      &.primary {
        color: #3b82f6;

        .card-icon {
          background: rgba(59, 130, 246, 0.1);
        }
      }

      &.success {
        color: #10b981;

        .card-icon {
          background: rgba(16, 185, 129, 0.1);
        }
      }

      &.warning {
        color: #f59e0b;

        .card-icon {
          background: rgba(245, 158, 11, 0.1);
        }
      }

      &.danger {
        color: #ef4444;

        .card-icon {
          background: rgba(239, 68, 68, 0.1);
        }
      }

      &.info {
        color: #06b6d4;

        .card-icon {
          background: rgba(6, 182, 212, 0.1);
        }
      }

      &.purple {
        color: #8b5cf6;

        .card-icon {
          background: rgba(139, 92, 246, 0.1);
        }
      }

      &.cyan {
        color: #06b6d4;

        .card-icon {
          background: rgba(6, 182, 212, 0.1);
        }
      }

      &.orange {
        color: #f97316;

        .card-icon {
          background: rgba(249, 115, 22, 0.1);
        }
      }

      &.gradient {
        color: #ec4899;

        .card-icon {
          background: rgba(236, 72, 153, 0.1);
        }
      }
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;

    .chart-card {
      background: #fff;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
      }

      &.large {
        grid-column: 1 / -1;
      }

      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .chart-title {
          display: flex;
          align-items: center;
          gap: 8px;
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #1f2937;

          .el-icon {
            display: flex;
            align-items: center;
            color: #3b82f6;
          }
        }

        .asset-type-select {
          width: 140px;
        }
      }
    }
  }

  @media (max-width: 768px) {
    .charts-container {
      grid-template-columns: 1fr;

      .chart-card.large {
        grid-column: 1;
      }
    }
  }

  // 移动端优化
  @media (max-width: 640px) {
    .dashboard-container {
      padding: 12px;
      background: #f0f2f5;
      min-height: 100vh;
    }

    .overview-cards {
      grid-template-columns: 1fr;
      gap: 12px;
      margin-bottom: 16px;

      .stat-card {
        gap: 12px;
        padding: 16px;

        .card-icon {
          width: 48px;
          height: 48px;

          .el-icon {
            font-size: 24px;
          }
        }

        .card-content {
          .card-label {
            font-size: 12px;
            margin-bottom: 6px;
          }

          .card-value {
            font-size: 20px;
            margin-bottom: 3px;
          }

          .card-trend {
            font-size: 11px;

            .el-icon {
              font-size: 11px;
            }
          }
        }

        &:hover {
          transform: none;
        }
      }
    }

    .charts-container {
      gap: 12px;

      .chart-card {
        padding: 12px;

        .chart-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;
          margin-bottom: 16px;

          .chart-title {
            font-size: 15px;

            .el-icon {
              font-size: 18px;
            }
          }

          .asset-type-select {
            width: 100%;
          }

          .el-tag {
            font-size: 11px;
          }
        }
      }
    }
  }

  // 平板优化 (641px - 768px)
  @media (max-width: 768px) and (min-width: 641px) {
    .overview-cards {
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }
  }
</style>
