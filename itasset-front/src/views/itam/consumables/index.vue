<template>
  <div class="page-container p-4">
    <el-card shadow="never" class="art-card !rounded-4">
      <div class="card-header mb-2">
        <div class="flex-cb">
          <div class="flex-c gap-2">
            <div class="w-1.5 h-6 bg-primary rounded-full"></div>
            <h2 class="text-4 font-bold text-g-800 dark:text-g-100">耗材管理</h2>
          </div>
        </div>
      </div>
      <SegmentedTabs v-model="activeTab" :options="tabOptions">
        <template #list>
          <ConsumablesList />
        </template>

        <template #category>
          <ConsumablesCategory />
        </template>

        <template #transactions>
          <ConsumablesTransactions />
        </template>

        <template #statistic>
          <ConsumablesStatistic :is-active="activeTab === 'statistic'" />
        </template>
      </SegmentedTabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import SegmentedTabs from '@/components/business/segmented-tabs/index.vue'
  import ConsumablesList from './consumables-list.vue'
  import ConsumablesCategory from './consumables-category.vue'
  import ConsumablesTransactions from './consumables-transactions.vue'
  import ConsumablesStatistic from './consumables-statistic.vue'
  import { ref } from 'vue'

  const activeTab = ref('list')

  const tabOptions = [
    { label: '耗材列表', value: 'list', icon: 'ri:list-check' },
    { label: '耗材分类', value: 'category', icon: 'ri:folder-line' },
    { label: '出入库明细', value: 'transactions', icon: 'ri:file-list-3-line' },
    { label: '耗材统计', value: 'statistic', icon: 'ri:pie-chart-2-line' }
  ]
</script>
