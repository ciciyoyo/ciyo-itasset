<template>
  <div class="test-page-container p-8 min-h-screen bg-g-50 dark:bg-g-950">
    <!-- 头部区域 -->
    <header class="mb-8">
      <div class="flex-cb mb-1">
        <h1 class="text-22 font-bold text-g-900 dark:text-g-100 tracking-tight">组件预览 & 测试</h1>
        <div class="flex-c gap-3">
          <span class="px-2.5 py-0.5 bg-primary/10 text-primary text-11 rounded-full font-medium border border-primary/20"
            >v1.2.0-beta</span
          >
          <div class="w-7 h-7 rounded-full bg-g-200 dark:bg-g-800 border border-g-300 dark:border-g-700"></div>
        </div>
      </div>
      <p class="text-13 text-g-500 dark:text-g-400 max-w-2xl leading-relaxed">
        用于测试业务级封装组件的动态效果和样式表现。当前正在调试：
        <code class="px-1.5 py-0.5 bg-g-100 dark:bg-g-800 rounded text-primary font-mono text-12">ArtSegmentedTabs</code>
      </p>
    </header>

    <!-- 主展示区域 -->
    <div class="max-w-1000 mx-auto">
      <div
        class="content-card bg-white dark:bg-g-900 rounded-5 shadow-xl shadow-g-200/50 dark:shadow-none border border-g-100 dark:border-g-800 p-8"
      >
        <!-- 标签控制栏 -->
        <div class="mb-10 flex-cb">
          <ArtSegmentedTabs v-model="activeTab" :options="tabOptions" @change="handleTabChange" />

          <div class="hidden md:flex flex-c gap-4">
            <div class="text-13 text-g-400">
              当前状态: <span class="text-primary font-bold italic">{{ activeTab.toUpperCase() }}</span>
            </div>
            <div class="w-[1px] h-4 bg-g-200 dark:bg-g-800"></div>
            <button class="text-14 text-g-500 hover:text-primary tad-300 flex-c gap-1">
              <i class="i-lucide-refresh-cw text-14"></i>
              <span>重置状态</span>
            </button>
          </div>
        </div>

        <!-- 内容渲染容器 -->
        <div class="tab-content-wrapper relative min-h-[400px]">
          <ArtSegmentedTabs v-model="activeTab" :options="tabOptions">
            <!-- 1. 服务列表 插槽 -->
            <template #list>
              <div class="grid gap-5">
                <div
                  v-for="(item, index) in mockServices"
                  :key="index"
                  class="group service-item p-5 rounded-4 border border-g-100 dark:border-g-800 flex-cb hover:border-primary/30 hover:bg-primary/[0.02] dark:hover:bg-primary/[0.05] tad-300"
                >
                  <div class="flex-c gap-5">
                    <div
                      class="w-12 h-12 rounded-3 flex-cc bg-g-50 dark:bg-g-800 group-hover:scale-110 tad-300"
                      :class="item.active ? 'text-primary' : 'text-g-400'"
                    >
                      <i :class="[item.icon, 'text-24']"></i>
                    </div>
                    <div>
                      <div class="flex-c gap-2 mb-1.5">
                        <span class="font-bold text-16 text-g-800 dark:text-g-100">{{ item.name }}</span>
                        <span v-if="item.isNew" class="px-1.5 py-0.5 bg-red-500 text-white text-10 rounded-1 font-bold">NEW</span>
                      </div>
                      <div class="text-13 text-g-500 flex-c gap-4">
                        <span class="flex-c gap-1"><i class="i-lucide-cpu text-14"></i> {{ item.cpu }}</span>
                        <span class="flex-c gap-1"><i class="i-lucide-database text-14"></i> {{ item.ram }}</span>
                        <span class="flex-c gap-1"><i class="i-lucide-clock text-14"></i> {{ item.uptime }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex-c gap-4">
                    <div class="text-right hidden sm:block">
                      <div class="text-12 text-g-400 mb-1">负载状态</div>
                      <div class="w-24 h-1.5 bg-g-100 dark:bg-g-800 rounded-full overflow-hidden">
                        <div class="h-full bg-primary tad-300" :style="{ width: item.load + '%' }"></div>
                      </div>
                    </div>
                    <button class="w-10 h-10 rounded-full flex-cc hover:bg-primary/10 text-g-400 hover:text-primary tad-300">
                      <i class="i-lucide-chevron-right text-18"></i>
                    </button>
                  </div>
                </div>
              </div>
            </template>

            <!-- 2. 归属信息 插槽 -->
            <template #belong>
              <div class="p-2">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-8 bg-g-50/50 dark:bg-g-800/30 p-8 rounded-4">
                  <div v-for="info in ownerInfo" :key="info.label" class="border-b border-g-100 dark:border-g-800 pb-4">
                    <div class="text-12 text-g-400 mb-2 flex-c gap-1">
                      <i :class="[info.icon, 'text-14']"></i>
                      {{ info.label }}
                    </div>
                    <div class="text-16 font-semibold text-g-800 dark:text-g-200">{{ info.value }}</div>
                  </div>
                </div>

                <div class="mt-8 flex gap-4">
                  <button class="px-6 py-2.5 bg-primary text-white rounded-2.5 text-14 font-medium hover:opacity-90 tad-300"
                    >导出资产报告</button
                  >
                  <button
                    class="px-6 py-2.5 border border-g-200 dark:border-g-700 text-g-600 dark:text-g-300 rounded-2.5 text-14 font-medium hover:bg-g-100 dark:hover:bg-g-800 tad-300"
                    >修改所属权</button
                  >
                </div>
              </div>
            </template>

            <!-- 3. 异常监控 插槽 -->
            <template #error>
              <div class="space-y-6">
                <div class="flex-cb bg-red-500/5 border border-red-500/20 p-5 rounded-4">
                  <div class="flex-c gap-4">
                    <div class="w-12 h-12 rounded-full bg-red-500/20 flex-cc text-red-500">
                      <i class="i-lucide-shield-alert text-24"></i>
                    </div>
                    <div>
                      <div class="text-16 font-bold text-red-600 dark:text-red-400">检测到严重性能瓶颈</div>
                      <div class="text-13 text-red-500/80">核心数据库（Node-01）响应时间超过 500ms</div>
                    </div>
                  </div>
                  <button class="bg-red-500 text-white px-5 py-2 rounded-2 text-13 font-bold shadow-lg shadow-red-500/20"
                    >立即处理</button
                  >
                </div>

                <div class="bg-white dark:bg-g-900 rounded-4 border border-g-100 dark:border-g-800 overflow-hidden">
                  <div class="px-5 py-4 bg-g-50 dark:bg-g-800/50 border-b border-g-100 dark:border-g-800 font-bold text-14"
                    >历史告警记录</div
                  >
                  <div class="divide-y divide-g-100 dark:divide-g-800">
                    <div
                      v-for="(log, idx) in errorLogs"
                      :key="idx"
                      class="p-5 flex-cb hover:bg-g-50/50 dark:hover:bg-g-800/20 tad-200"
                    >
                      <div class="flex-c gap-4">
                        <div class="w-2 h-2 rounded-full" :class="log.level === 'error' ? 'bg-red-500' : 'bg-orange-400'"></div>
                        <span class="text-14 text-g-700 dark:text-g-300">{{ log.message }}</span>
                      </div>
                      <span class="text-12 text-g-400 tabular-nums">{{ log.time }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </ArtSegmentedTabs>
        </div>
      </div>
    </div>

    <footer class="mt-12 text-center text-12 text-g-400"> Design System &copy; 2024 Ciyo CRM Infrastructure </footer>
  </div>
</template>

<script setup lang="ts">
  import ArtSegmentedTabs from '@/components/business/segmented-tabs/index.vue'
  import { ref } from 'vue'

  const activeTab = ref('list')

  const tabOptions = [
    { label: '服务列表', value: 'list' },
    { label: '归属信息', value: 'belong' },
    { label: '故障监控', value: 'error' }
  ]

  const mockServices = [
    {
      name: 'Gateway API Cluster',
      icon: 'i-lucide-cable',
      cpu: '12%',
      ram: '4.2GB',
      uptime: '45d',
      load: 35,
      active: true,
      isNew: true
    },
    {
      name: 'Payment Microservice',
      icon: 'i-lucide-credit-card',
      cpu: '8%',
      ram: '1.8GB',
      uptime: '12d',
      load: 15,
      active: true
    },
    { name: 'ElasticSearch Indexer', icon: 'i-lucide-search', cpu: '64%', ram: '16GB', uptime: '124d', load: 82, active: true },
    { name: 'Legacy Auth Provider', icon: 'i-lucide-key-round', cpu: '0%', ram: '0.2GB', uptime: '0d', load: 0, active: false }
  ]

  const ownerInfo = [
    { label: '所属项目', value: 'CIYO 核心云平台', icon: 'i-lucide-folder' },
    { label: '主要负责人', value: '张晓明 (Technical Director)', icon: 'i-lucide-user' },
    { label: '通讯地址', value: 'zhangxm@ciyo.io', icon: 'i-lucide-mail' },
    { label: '部署可用区', value: '华为云-上海', icon: 'i-lucide-map-pin' },
    { label: '最后同步时间', value: '2023-12-01 10:24', icon: 'i-lucide-rotate-cw' },
    { label: '关联工单', value: 'ORD-998273', icon: 'i-lucide-ticket' }
  ]

  const errorLogs = [
    { message: 'SSL Certificate expiring in 3 days', time: '12:45:01', level: 'warning' },
    { message: 'Database connection spike detected', time: '11:20:54', level: 'error' },
    { message: 'Cache re-indexing completed with minor errors', time: '09:12:33', level: 'warning' },
    { message: 'Unauthorized access attempt from 192.168.1.1', time: 'Yesterday', level: 'error' }
  ]

  const handleTabChange = (val: string) => {
    console.log('[Test] Tab changed to:', val)
  }
</script>

<style scoped lang="scss">
  .test-page-container {
    animation: fade-in 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  }

  .content-card {
    transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
    }
  }

  @keyframes fade-in {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
