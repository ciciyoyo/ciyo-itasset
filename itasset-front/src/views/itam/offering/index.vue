<template>
  <div class="page-container p-4">
    <el-card shadow="never" class="art-card !rounded-4">
      <div class="card-header mb-2">
        <div class="flex-cb">
          <div class="flex-c gap-2">
            <div class="w-1.5 h-6 bg-primary rounded-full"></div>
            <h2 class="text-4 font-bold text-g-800 dark:text-g-100">服务管理</h2>
          </div>
        </div>
      </div>
      <SegmentedTabs v-model="activeTab" :options="tabOptions">
        <template #list>
          <OfferingList />
        </template>

        <template #belong>
          <OfferingBelong />
        </template>

        <template #error>
          <OfferingError />
        </template>

        <template #statistic>
          <OfferingStatistic :is-active="activeTab === 'statistic'" />
        </template>
      </SegmentedTabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import SegmentedTabs from '@/components/business/segmented-tabs/index.vue'
  import OfferingList from './offering-list.vue'
  import OfferingBelong from './offering-belong.vue'
  import OfferingError from './offering-error.vue'
  import OfferingStatistic from './offering-statistic.vue'
  import { ref } from 'vue'

  const activeTab = ref('list')

  const tabOptions = [
    { label: '服务列表', value: 'list', icon: 'ri:list-check' },
    { label: '关联设备', value: 'belong', icon: 'ri:user-3-line' },
    { label: '异常服务', value: 'error', icon: 'ri:alarm-warning-line' },
    { label: '服务统计', value: 'statistic', icon: 'ri:pie-chart-2-line' }
  ]
</script>
