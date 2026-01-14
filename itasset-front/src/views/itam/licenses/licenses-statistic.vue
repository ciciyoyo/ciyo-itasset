<template>
  <div class="licenses-statistic p-4">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <ArtStatsCard title="软件总数" :count="statistics.totalCount" icon="ri:key-2-line" icon-style="bg-blue-500" />
      <ArtStatsCard title="库存不足" :count="statistics.insufficientCount" icon="ri:alert-line" icon-style="bg-orange-500" />
      <ArtStatsCard title="即将过期" :count="statistics.expiringSoonCount" icon="ri:time-line" icon-style="bg-yellow-500" />
      <ArtStatsCard title="已过期" :count="statistics.expiredCount" icon="ri:close-circle-line" icon-style="bg-red-500" />
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="art-card h-100 p-5 mb-5 max-sm:mb-4">
        <div class="art-card-header">
          <div class="title">
            <h4>月度价值趋势</h4>
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
            <h4>软件分类分布</h4>
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
    getLicenseCategory,
    getLicenseIndicators,
    getLicenseMonthlyValue,
    LicenseCategoryVO,
    LicenseIndicatorsVO,
    LicenseMonthlyValueVO
  } from '@/api/itam/licenses'

  const statistics = ref<LicenseIndicatorsVO>({
    totalCount: 0,
    insufficientCount: 0,
    expiringSoonCount: 0,
    expiredCount: 0
  })

  const categoryDistributionData = ref<{ value: number; name: string }[]>([])

  const chartData = ref<{ name: string; data: number[] }[]>([])
  const xAxisData = ref<string[]>([])

  const props = defineProps({
    isActive: {
      type: Boolean,
      default: false
    }
  })

  const loadStatistics = async () => {
    try {
      // 加载指标数据
      const indicators = await getLicenseIndicators()
      statistics.value = {
        totalCount: indicators.totalCount || 0,
        insufficientCount: indicators.insufficientCount || 0,
        expiringSoonCount: indicators.expiringSoonCount || 0,
        expiredCount: indicators.expiredCount || 0
      }

      // 加载月度价值数据
      const monthlyData = await getLicenseMonthlyValue()
      if (monthlyData && Array.isArray(monthlyData)) {
        xAxisData.value = monthlyData.map((item: LicenseMonthlyValueVO) => item.statsMonth)
        chartData.value = [
          {
            name: '总价值',
            data: monthlyData.map((item: LicenseMonthlyValueVO) => item.totalValue || 0)
          }
        ]
      }

      // 加载分类分布数据
      const categoryData = await getLicenseCategory()
      if (categoryData && Array.isArray(categoryData)) {
        categoryDistributionData.value = categoryData.map((item: LicenseCategoryVO) => ({
          value: item.count || 0,
          name: item.categoryName || '未知'
        }))
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
  .licenses-statistic {
    background-color: var(--el-bg-color);
  }
</style>
