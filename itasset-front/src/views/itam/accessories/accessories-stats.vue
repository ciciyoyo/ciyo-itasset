<template>
  <div class="stats-page p-4">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <ArtStatsCard title="配件总数" :count="summary.total" icon="ri:box-3-line" icon-style="bg-blue-500" />
      <ArtStatsCard title="已过保" :count="summary.expired" icon="ri:alarm-warning-line" icon-style="bg-red-500" />
      <ArtStatsCard title="即将过保" :count="summary.soonToExpire" icon="ri:time-line" icon-style="bg-orange-500" />
      <ArtStatsCard title="低库存" :count="summary.lowStock" icon="ri:inbox-line" icon-style="bg-gray-500" />
    </div>

    <!-- 月度价值趋势图 -->
    <div class="art-card p-5 mt-4">
      <div class="art-card-header">
        <div class="title">
          <h4>月度价值趋势</h4>
        </div>
      </div>
      <ArtLineChart
        height="400px"
        :data="chartData"
        :xAxisData="xAxisData"
        :showLegend="false"
        :showAxisLabel="true"
        :showAxisLine="false"
        :showSplitLine="true"
        :axisLabelRotate="45"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import {
    getAccessoriesStatsSummary,
    getAccessoriesMonthlyValue,
    AccessoryStatsSummary,
    MonthlyValueStat
  } from '@/api/itam/accessories'
  import ArtStatsCard from '@/components/core/cards/art-stats-card/index.vue'

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
    background-color: var(--el-bg-color);
  }
</style>
