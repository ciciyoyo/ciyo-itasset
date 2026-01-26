<template>
  <div class="page-container p-4">
    <el-card shadow="never" class="art-card !rounded-4">
      <div class="card-header mb-2">
        <div class="flex-cb">
          <div class="flex-c gap-2">
            <div class="w-1.5 h-6 bg-primary rounded-full"></div>
            <h2 class="text-4 font-bold text-g-800 dark:text-g-100">配件管理</h2>
          </div>
        </div>
      </div>
      <SegmentedTabs v-model="activeTab" :options="tabOptions">
        <template #list>
          <AccessoriesList />
        </template>

        <template #category>
          <AccessoriesCategory />
        </template>

        <template #allocations>
          <AccessoriesAllocations />
        </template>

        <template #stats>
          <AccessoriesStats />
        </template>
      </SegmentedTabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import SegmentedTabs from '@/components/business/segmented-tabs/index.vue'
  import AccessoriesList from './accessories-list.vue'
  import AccessoriesCategory from './accessories-category.vue'
  import AccessoriesAllocations from './accessories-allocations.vue'
  import AccessoriesStats from './accessories-stats.vue'
  import { ref } from 'vue'

  const activeTab = ref('list')

  const tabOptions = [
    { label: '配件列表', value: 'list', icon: 'ri:list-check' },
    { label: '配件分类', value: 'category', icon: 'ri:folder-line' },
    { label: '关联设备', value: 'allocations', icon: 'ri:link' },
    { label: '配件统计', value: 'stats', icon: 'ri:bar-chart-line' }
  ]
</script>
