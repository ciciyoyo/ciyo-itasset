<template>
  <div class="device-stats p-4">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <ArtStatsCard title="设备总数" :count="summary.total" icon="ri:device-line" icon-style="bg-blue-500" />
      <ArtStatsCard title="已到期" :count="summary.expired" icon="ri:alarm-warning-line" icon-style="bg-red-500" />
      <ArtStatsCard title="即将到期" :count="summary.soonToExpire" icon="ri:error-warning-line" icon-style="bg-orange-500" />
      <ArtStatsCard title="已报废" :count="summary.scrapped" icon="ri:delete-bin-line" icon-style="bg-gray-500" />
    </div>

    <div class="grid grid-cols-1 gap-4">
      <div class="art-card h-100 p-5 mb-5 max-sm:mb-4">
        <div class="art-card-header">
          <div class="title">
            <h4>设备资产价值趋势</h4>
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
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, onMounted } from 'vue'
  import { getDeviceSummary, getDeviceMonthlyValue, type DeviceSummaryVO } from '@/api/itam/device'
  import ArtStatsCard from '@/components/core/cards/art-stats-card/index.vue'
  import ArtLineChart from '@/components/core/charts/art-line-chart/index.vue'

  interface Props {
    isActive?: boolean
  }

  const props = defineProps<Props>()

  const summary = ref<DeviceSummaryVO>({
    total: 0,
    expired: 0,
    scrapped: 0,
    soonToExpire: 0
  })

  const chartData = ref<{ name: string; data: number[] }[]>([])
  const xAxisData = ref<string[]>([])

  const loadData = async () => {
    console.log('DeviceStats: 执行 loadData...')
    // 获取概要统计
    getDeviceSummary()
      .then((res) => {
        console.log('DeviceStats: 概要统计成功', res)
        if (res) {
          summary.value = res
        }
      })
      .catch((err) => {
        console.error('DeviceStats: 概要统计失败', err)
      })

    // 获取月度趋势统计
    getDeviceMonthlyValue()
      .then((monthlyRes) => {
        console.log('DeviceStats: 趋势统计成功', monthlyRes)
        if (monthlyRes && monthlyRes.length > 0) {
          monthlyRes.sort((a, b) => a.statsMonth.localeCompare(b.statsMonth))
          xAxisData.value = monthlyRes.map((item) => item.statsMonth)
          chartData.value = [
            {
              name: '资产价值',
              data: monthlyRes.map((item) => item.totalValue)
            }
          ]
        }
      })
      .catch((err) => {
        console.error('DeviceStats: 趋势统计失败', err)
      })
  }

  onMounted(() => {
    console.log('DeviceStats: 组件已挂载, isActive:', props.isActive)
    if (props.isActive) {
      loadData()
    }
  })

  watch(
    () => props.isActive,
    (val) => {
      console.log('DeviceStats: isActive watch 触发:', val)
      if (val) {
        loadData()
      }
    }
  )
</script>

<style scoped lang="scss">
  .device-stats {
    background-color: var(--el-bg-color);
  }
</style>
