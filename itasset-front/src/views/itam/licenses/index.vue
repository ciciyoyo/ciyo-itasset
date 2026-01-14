<template>
  <div class="page-container p-4">
    <el-card shadow="never" class="art-card !rounded-4">
      <div class="card-header mb-2">
        <div class="flex-cb">
          <div class="flex-c gap-2">
            <div class="w-1.5 h-6 bg-primary rounded-full"></div>
            <h2 class="text-4 font-bold text-g-800 dark:text-g-100">软件管理</h2>
          </div>
        </div>
      </div>
      <SegmentedTabs v-model="activeTab" :options="tabOptions">
        <template #list>
          <LicensesList />
        </template>

        <template #belong>
          <LicensesBelong />
        </template>

        <template #statistic>
          <LicensesStatistic :is-active="activeTab === 'statistic'" />
        </template>

        <template #category>
          <LicensesCategory />
        </template>
      </SegmentedTabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import SegmentedTabs from '@/components/business/segmented-tabs/index.vue'
  import LicensesList from './licenses-list.vue'
  import LicensesBelong from './licenses-belong.vue'
  import LicensesStatistic from './licenses-statistic.vue'
  import LicensesCategory from './licenses-category.vue'
  import { ref } from 'vue'

  const activeTab = ref('list')

  const tabOptions = [
    { label: '软件列表', value: 'list', icon: 'ri:list-check' },
    { label: '软件分类', value: 'category', icon: 'ri:folder-line' },
    { label: '关联设备', value: 'belong', icon: 'ri:link' },
    { label: '软件统计', value: 'statistic', icon: 'ri:pie-chart-2-line' }
  ]
</script>
