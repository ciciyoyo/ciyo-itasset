<template>
  <div class="consumables-statistic p-4">
    <!-- 概览统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4 mb-6">
      <ArtStatsCard title="耗材种类" :count="overviewData.skuCount" icon="ri:box-3-line" icon-style="bg-blue-500" />
      <ArtStatsCard title="库存总量" :count="overviewData.totalQuantity" icon="ri:stack-line" icon-style="bg-green-500" />
      <ArtStatsCard title="总金额" :count="overviewData.totalAmount" icon="ri:money-cny-circle-line" icon-style="bg-purple-500" />
      <ArtStatsCard title="低库存预警" :count="overviewData.lowStockCount" icon="ri:alarm-warning-line" icon-style="bg-red-500" />
      <ArtStatsCard
        title="本月入库"
        :count="overviewData.monthStockIn"
        icon="ri:arrow-down-circle-line"
        icon-style="bg-cyan-500"
      />
      <ArtStatsCard
        title="本月出库"
        :count="overviewData.monthStockOut"
        icon="ri:arrow-up-circle-line"
        icon-style="bg-orange-500"
      />
    </div>

    <!-- 图表区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card h-100 p-5 mb-5 max-sm:mb-4">
        <div class="art-card-header">
          <div class="title">
            <h4>月度出入库趋势</h4>
          </div>
        </div>
        <ArtLineChart
          height="calc(100% - 30px)"
          :data="chartData"
          :xAxisData="xAxisData"
          :showLegend="true"
          :showAxisLabel="true"
          :showAxisLine="false"
          :showSplitLine="true"
          :axisLabelRotate="45"
        />
      </div>

      <div class="art-card h-100 p-5 mb-5 max-sm:mb-4">
        <div class="art-card-header">
          <div class="title">
            <h4>耗材分类分布</h4>
          </div>
        </div>

        <ArtRingChart
          :data="categoryDistributionData"
          :colors="['#4C87F3', '#93F1B4', '#8BD8FC', '#FFB84D', '#FF6B9D']"
          :radius="['46%', '60%']"
          :showLabel="true"
          :showLegend="true"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import ArtStatsCard from '@/components/core/cards/art-stats-card/index.vue'
  import {
    type ConsumablesMonthlyVO,
    type ConsumablesOverviewVO,
    getConsumablesCategory,
    getConsumablesMonthly,
    getConsumablesOverview
  } from '@/api/itam/consumables'

  const categoryDistributionData = ref<{ value: number; name: string }[]>([])

  const chartData = ref<{ name: string; data: number[] }[]>([])
  const xAxisData = ref<string[]>([])

  // 概览数据
  const overviewData = ref<ConsumablesOverviewVO>({
    totalAmount: 0,
    monthStockIn: 0,
    totalQuantity: 0,
    monthStockOut: 0,
    lowStockCount: 0,
    skuCount: 0
  })

  const props = defineProps({
    isActive: {
      type: Boolean,
      default: false
    }
  })

  const loadStatistics = async () => {
    try {
      // 加载概览数据
      const overview = await getConsumablesOverview()
      if (overview) {
        overviewData.value = {
          totalAmount: overview.totalAmount || 0,
          monthStockIn: overview.monthStockIn || 0,
          totalQuantity: overview.totalQuantity || 0,
          monthStockOut: overview.monthStockOut || 0,
          lowStockCount: overview.lowStockCount || 0,
          skuCount: overview.skuCount || 0
        }
      }

      // 加载月度数据
      const monthlyData = await getConsumablesMonthly()
      if (monthlyData && Array.isArray(monthlyData)) {
        xAxisData.value = monthlyData.map((item: ConsumablesMonthlyVO) => item.month)
        chartData.value = [
          {
            name: '入库数量',
            data: monthlyData.map((item: ConsumablesMonthlyVO) => item.stockIn)
          },
          {
            name: '出库数量',
            data: monthlyData.map((item: ConsumablesMonthlyVO) => item.stockOut)
          }
        ]
      }

      // 加载分类分布数据
      const categoryData = await getConsumablesCategory()
      if (categoryData && Array.isArray(categoryData)) {
        categoryDistributionData.value = categoryData
      }
    } catch (error) {
      console.error('加载统计数据失败:', error)
    }
  }

  watch(
    () => props.isActive,
    async () => {
      if (props.isActive) {
        await loadStatistics()
      }
    },
    {
      immediate: true
    }
  )
</script>

<style scoped>
  .consumables-statistic {
    background-color: var(--el-bg-color);
  }
</style>
