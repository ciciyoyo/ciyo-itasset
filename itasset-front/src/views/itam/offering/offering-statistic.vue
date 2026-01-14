<template>
  <div class="offering-statistic p-4">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <ArtStatsCard title="服务总数" :count="statistics.totalCount" icon="ri:server-line" icon-style="bg-blue-500" />
      <ArtStatsCard title="总金额" :count="statistics.totalAmount" icon="ri:money-cny-box-line" icon-style="bg-orange-500" />
      <ArtStatsCard title="正常运行" :count="statistics.normalCount" icon="ri:checkbox-circle-line" icon-style="bg-green-500" />
      <ArtStatsCard title="异常服务" :count="statistics.exceptionCount" icon="ri:alert-line" icon-style="bg-red-500" />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card h-100 p-5 mb-5 max-sm:mb-4">
        <div class="art-card-header">
          <div class="title">
            <h4>服务价值趋势</h4>
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
            <h4>供应商异常统计</h4>
          </div>
        </div>

        <ArtRingChart
          :data="supplierExceptionData"
          :colors="['#4C87F3', '#93F1B4', '#8BD8FC']"
          :radius="['46%', '60%']"
          :showLabel="true"
          :showLegend="true"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import {
    getOfferingStatistics,
    getServiceValueStatistics,
    getSupplierExceptionStatistics,
    type OfferingStatisticsVO,
    type SupplierExceptionStatisticsVO
  } from '@/api/itam/offering'
  import ArtStatsCard from '@/components/core/cards/art-stats-card/index.vue'

  const statistics = ref<OfferingStatisticsVO>({
    totalCount: 0,
    normalCount: 0,
    exceptionCount: 0,
    totalAmount: 0
  })

  const supplierExceptionData = ref<{ value: number; name: string }[]>([])

  const chartData = ref<{ name: string; data: number[] }[]>([])
  const xAxisData = ref<string[]>([])

  const props = defineProps({
    isActive: {
      type: Boolean,
      default: false
    }
  })

  watch(
    () => props.isActive,
    async () => {
      if (props.isActive) {
        const res = await getOfferingStatistics()
        if (res) {
          statistics.value = res
        }
        const stats = await getServiceValueStatistics()
        if (stats && stats.length > 0) {
          stats.sort((a, b) => a.statsMonth.localeCompare(b.statsMonth))
          xAxisData.value = stats.map((item) => item.statsMonth)
          chartData.value = [
            {
              name: '金额',
              data: stats.map((item) => item.totalValue)
            }
          ]
        }
        const supplierExceptionStats = await getSupplierExceptionStatistics()
        if (supplierExceptionStats) {
          supplierExceptionData.value = supplierExceptionStats.map((item: SupplierExceptionStatisticsVO) => ({
            value: item.failureCount,
            name: item.supplierName
          }))
        }
      }
    },
    {
      immediate: true
    }
  )
</script>

<style scoped>
  .offering-statistic {
    background-color: var(--el-bg-color);
  }
</style>
