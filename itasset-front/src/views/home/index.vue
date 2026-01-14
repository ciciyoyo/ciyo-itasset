<template>
  <div class="min-h-full w-full bg-gray-50 text-gray-900 p-6 md:p-8 font-sans overflow-y-auto custom-scrollbar">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-1 text-gray-900">{{ greeting }}，{{ username }}</h1>
      <p class="text-gray-500 text-sm">管理您的IT设备和申请</p>
    </div>

    <!-- Top Stats Row -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <!-- Card 1 -->
      <div
        class="bg-white border border-gray-100 rounded-xl p-5 relative overflow-hidden group hover:shadow-lg transition-all duration-300"
      >
        <div
          class="absolute -right-6 -bottom-6 text-blue-50 opacity-50 transform rotate-12 group-hover:scale-110 group-hover:-rotate-12 transition-all duration-500 ease-out"
        >
          <ArtSvgIcon icon="ri:computer-line" class="text-9xl" />
        </div>
        <div class="relative z-10">
          <div class="flex items-start justify-between mb-4">
            <div
              class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-50 to-blue-100/50 flex items-center justify-center text-blue-600 shadow-sm group-hover:scale-110 transition-transform duration-300"
            >
              <ArtSvgIcon icon="ri:computer-line" class="text-lg" />
            </div>
            <span class="bg-green-50 text-green-600 text-[10px] font-bold px-2 py-0.5 rounded-full border border-green-100"
              >正常</span
            >
          </div>
          <div class="text-gray-500 text-xs mb-1 font-medium title-font">我的设备</div>
          <div class="text-3xl font-bold mb-3 text-gray-900 tracking-tight">2</div>
          <div class="text-xs text-gray-400 mt-2 truncate max-w-[80%]">MacBook Pro, iPhone 15</div>
        </div>
      </div>

      <!-- Card 2 -->
      <div
        class="bg-white border border-gray-100 rounded-xl p-5 relative overflow-hidden group hover:shadow-lg transition-all duration-300"
      >
        <div
          class="absolute -right-6 -bottom-6 text-amber-50 opacity-50 transform rotate-12 group-hover:scale-110 group-hover:-rotate-12 transition-all duration-500 ease-out"
        >
          <ArtSvgIcon icon="ri:timer-line" class="text-9xl" />
        </div>
        <div class="relative z-10">
          <div class="flex items-start justify-between mb-4">
            <div
              class="w-10 h-10 rounded-lg bg-gradient-to-br from-amber-50 to-amber-100/50 flex items-center justify-center text-amber-600 shadow-sm group-hover:scale-110 transition-transform duration-300"
            >
              <ArtSvgIcon icon="ri:timer-line" class="text-lg" />
            </div>
            <span
              class="bg-amber-50 text-amber-600 text-[10px] font-bold px-2 py-0.5 rounded-full border border-amber-100"
              >审核中</span
            >
          </div>
          <div class="text-gray-500 text-xs mb-1 font-medium">待处理申请</div>
          <div class="text-3xl font-bold mb-3 text-gray-900 tracking-tight">1</div>
          <div class="text-xs text-gray-400 mt-2">设备申请 - 2026/01/09</div>
        </div>
      </div>

      <!-- Card 3 -->
      <div
        class="bg-white border border-gray-100 rounded-xl p-5 relative overflow-hidden group hover:shadow-lg transition-all duration-300"
      >
        <div
          class="absolute -right-6 -bottom-6 text-cyan-50 opacity-50 transform rotate-12 group-hover:scale-110 group-hover:-rotate-12 transition-all duration-500 ease-out"
        >
          <ArtSvgIcon icon="ri:calendar-check-line" class="text-9xl" />
        </div>
        <div class="relative z-10">
          <div class="flex items-start justify-between mb-4">
            <div
              class="w-10 h-10 rounded-lg bg-gradient-to-br from-cyan-50 to-cyan-100/50 flex items-center justify-center text-cyan-600 shadow-sm group-hover:scale-110 transition-transform duration-300"
            >
              <ArtSvgIcon icon="ri:calendar-check-line" class="text-lg" />
            </div>
          </div>
          <div class="text-gray-500 text-xs mb-1 font-medium">使用天数</div>
          <div class="text-3xl font-bold mb-3 text-gray-900 tracking-tight">156</div>
          <div class="text-xs text-gray-400 mt-2">自 2025-08-05 起算</div>
        </div>
      </div>

      <!-- Card 4 -->
      <div
        class="bg-white border border-gray-100 rounded-xl p-5 relative overflow-hidden group hover:shadow-lg transition-all duration-300"
      >
        <div
          class="absolute -right-6 -bottom-6 text-purple-50 opacity-50 transform rotate-12 group-hover:scale-110 group-hover:-rotate-12 transition-all duration-500 ease-out"
        >
          <ArtSvgIcon icon="ri:hourglass-line" class="text-9xl" />
        </div>
        <div class="relative z-10">
          <div class="flex items-start justify-between mb-4">
            <div
              class="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-50 to-purple-100/50 flex items-center justify-center text-purple-600 shadow-sm group-hover:scale-110 transition-transform duration-300"
            >
              <ArtSvgIcon icon="ri:hourglass-line" class="text-lg" />
            </div>
            <span class="bg-gray-50 text-gray-400 text-[10px] font-bold px-2 py-0.5 rounded-full border border-gray-100"
              >无需续借</span
            >
          </div>
          <div class="text-gray-500 text-xs mb-1 font-medium">即将到期</div>
          <div class="text-3xl font-bold mb-3 text-gray-900 tracking-tight">0</div>
          <div class="text-xs text-gray-400 mt-2">暂无设备即将到期</div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div
        class="bg-white border border-gray-100 rounded-xl p-6 flex flex-col items-center justify-center gap-3 hover:shadow-md hover:-translate-y-1 transition-all duration-300 cursor-pointer"
      >
        <div
          class="w-12 h-12 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center transition-transform duration-300 group-hover:scale-110"
        >
          <ArtSvgIcon icon="ri:add-circle-line" class="text-2xl" />
        </div>
        <span class="text-sm font-medium text-gray-700">设备申请</span>
      </div>

      <div
        class="bg-white border border-gray-100 rounded-xl p-6 flex flex-col items-center justify-center gap-3 hover:shadow-md hover:-translate-y-1 transition-all duration-300 cursor-pointer"
      >
        <div
          class="w-12 h-12 rounded-full bg-amber-50 text-amber-600 flex items-center justify-center transition-transform duration-300 group-hover:scale-110"
        >
          <ArtSvgIcon icon="ri:tools-line" class="text-2xl" />
        </div>
        <span class="text-sm font-medium text-gray-700">故障报修</span>
      </div>

      <div
        class="bg-white border border-gray-100 rounded-xl p-6 flex flex-col items-center justify-center gap-3 hover:shadow-md hover:-translate-y-1 transition-all duration-300 cursor-pointer"
      >
        <div
          class="w-12 h-12 rounded-full bg-green-50 text-green-600 flex items-center justify-center transition-transform duration-300 group-hover:scale-110"
        >
          <ArtSvgIcon icon="ri:exchange-line" class="text-2xl" />
        </div>
        <span class="text-sm font-medium text-gray-700">设备归还</span>
      </div>

      <div
        class="bg-white border border-gray-100 rounded-xl p-6 flex flex-col items-center justify-center gap-3 hover:shadow-md hover:-translate-y-1 transition-all duration-300 cursor-pointer"
      >
        <div
          class="w-12 h-12 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center transition-transform duration-300 group-hover:scale-110"
        >
          <ArtSvgIcon icon="ri:customer-service-2-line" class="text-2xl" />
        </div>
        <span class="text-sm font-medium text-gray-700">联系客服</span>
      </div>
    </div>

    <!-- Requests List -->
    <div class="bg-white border border-gray-100 rounded-xl p-6 min-h-[400px]">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h2 class="text-lg font-bold mb-1 text-gray-900">我的申请</h2>
          <p class="text-xs text-gray-500">查看您提交的申请记录</p>
        </div>
        <button class="text-xs text-gray-400 hover:text-blue-600 flex items-center gap-1 transition-colors">
          查看全部 <ArtSvgIcon icon="ri:arrow-right-line" />
        </button>
      </div>

      <div class="flex flex-col gap-6">
        <div v-for="req in requestList" :key="req.id" class="border-b border-gray-100 last:border-0 pb-6 last:pb-0">
          <div class="flex flex-col md:flex-row md:items-start justify-between gap-4 mb-2">
            <div class="flex items-center gap-3">
              <span class="text-xs font-mono text-gray-400 uppercase">{{ req.id }}</span>
              <span class="text-gray-300">|</span>
              <span class="text-sm font-medium text-gray-700">{{ req.type }}</span>
              <div
                class="flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-medium border"
                :class="getStatusClass(req.status)"
              >
                <ArtSvgIcon :icon="getStatusIcon(req.status)" />
                <span>{{ req.statusText }}</span>
              </div>
            </div>
            <!-- Date for Mobile -->
            <div class="text-xs text-gray-400 md:hidden">
              <div class="mb-1 text-gray-500">申请日期</div>
              {{ req.date }}
            </div>
          </div>

          <div class="flex justify-between items-start">
            <div class="space-y-2">
              <div>
                <div class="text-xs text-gray-400 mb-1">设备</div>
                <div class="text-sm font-medium text-gray-900">{{ req.device }}</div>
              </div>
              <div>
                <div class="text-xs text-gray-400 mb-1">申请原因</div>
                <div class="text-sm text-gray-500">{{ req.reason }}</div>
              </div>
            </div>

            <!-- Date for Desktop -->
            <div class="hidden md:block text-right">
              <div class="text-xs text-gray-400 mb-1">申请日期</div>
              <div class="text-sm font-medium text-gray-900">{{ req.date }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { useUserStore } from '@/store/modules/user'

  const userStore = useUserStore()
  const username = computed(() => userStore.getUserInfo.userName || '用户')

  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return '夜深了'
    if (hour < 12) return '早上好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
  })

  const requestList = ref([
    {
      id: 'REQ-015',
      type: '设备申请',
      status: 'pending',
      statusText: '待处理',
      device: 'MacBook Pro 16"',
      date: '2026-01-09',
      reason: '开发工作需要更高性能设备'
    },
    {
      id: 'REQ-012',
      type: '设备归还',
      status: 'approved',
      statusText: '已批准',
      device: 'iPhone 14 Pro',
      date: '2026-01-05',
      reason: '升级至新设备'
    },
    {
      id: 'REQ-008',
      type: '设备维修',
      status: 'approved',
      statusText: '已批准',
      device: 'MacBook Pro 14',
      date: '2026-01-02',
      reason: '键盘故障'
    },
    {
      id: 'REQ-005',
      type: '设备续借',
      status: 'rejected',
      statusText: '已拒绝',
      device: 'iPad Air',
      date: '2025-12-28',
      reason: '使用频率较低'
    }
  ])

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'pending':
        return 'bg-yellow-500/10 border-yellow-500/20 text-yellow-500'
      case 'approved':
        return 'bg-green-500/10 border-green-500/20 text-green-500'
      case 'rejected':
        return 'bg-red-500/10 border-red-500/20 text-red-500'
      default:
        return 'bg-gray-500/10 border-gray-500/20 text-gray-500'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'pending':
        return 'ri:time-line'
      case 'approved':
        return 'ri:checkbox-circle-line'
      case 'rejected':
        return 'ri:close-circle-line'
      default:
        return 'ri:question-fill'
    }
  }
</script>

<style scoped>
  .custom-scrollbar::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  .custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
  }
  .custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background-color: rgba(0, 0, 0, 0.2);
  }
</style>
