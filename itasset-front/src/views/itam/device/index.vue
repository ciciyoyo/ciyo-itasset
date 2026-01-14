<template>
  <div class="page-container p-4">
    <el-card shadow="never" class="art-card !rounded-4">
      <div class="card-header mb-2">
        <div class="flex-cb">
          <div class="flex-c gap-2">
            <div class="w-1.5 h-6 bg-primary rounded-full"></div>
            <h2 class="text-4 font-bold text-g-800 dark:text-g-100">设备管理</h2>
          </div>
        </div>
      </div>
      <SegmentedTabs v-model="activeTab" :options="tabOptions">
        <template #list>
          <DeviceList />
        </template>
        <template #allocation>
          <AllocationList />
        </template>
        <template #failure>
          <DeviceFailure />
        </template>
        <template #stats>
          <DeviceStats :is-active="activeTab === 'stats'" />
        </template>
      </SegmentedTabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import SegmentedTabs from '@/components/business/segmented-tabs/index.vue'
import DeviceList from './device-list.vue'
import AllocationList from './allocation-list.vue'
import DeviceFailure from './device-failure.vue'
import DeviceStats from './device-stats.vue'

import { ref } from 'vue'

const activeTab = ref('list')

const tabOptions = [
  { label: '设备列表', value: 'list', icon: 'ri:list-check' },
  { label: '分配记录', value: 'allocation', icon: 'ri:history-line' },
  { label: '异常记录', value: 'failure', icon: 'ri:error-warning-line' },
  { label: '统计信息', value: 'stats', icon: 'ri:pie-chart-line' }
]
</script>
