<template>
  <div class="art-segmented-tabs-container">
    <!-- Tab 头部容器 -->
    <div class="art-segmented-tabs relative flex bg-g-100 dark:bg-g-900 p-1 rounded-3 w-full">
      <!-- 滑动的背景块 -->
      <div
        class="tab-glider absolute top-1 bottom-1 bg-white dark:bg-g-700 rounded-2 shadow-sm transition-all duration-300 ease-spring"
        :style="gliderStyle"
      ></div>

      <div
        v-for="item in options"
        :key="item.value"
        :ref="(el) => setItemRef(el as HTMLElement, item.value)"
        class="tab-item relative z-1 flex-1 flex-cc c-p px-3 py-1 rounded-2 text-3.5 select-none transition-colors duration-200"
        :class="[modelValue === item.value ? 'active-tab' : 'text-g-900 hover:text-g-900 dark:text-g-400 dark:hover:text-g-200']"
        @click="handleToggle(item.value)"
      >
        <el-icon v-if="item.icon" class="mr-1 text-4 font-bold">
          <ArtSvgIcon v-if="typeof item.icon === 'string'" :icon="item.icon" />
          <component v-else :is="item.icon" />
        </el-icon>
        {{ item.label }}
      </div>
    </div>

    <!-- 内容区域：自动匹配插槽 -->
    <div class="art-segmented-content mt-2 relative overflow-hidden">
      <template v-for="item in options" :key="item.value">
        <Transition name="slide-fade">
          <div v-if="$slots[item.value]" v-show="modelValue === item.value">
            <slot :name="item.value" :active="modelValue === item.value"></slot>
          </div>
        </Transition>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { type Component, nextTick, onMounted, onUnmounted, provide, reactive, ref, toRef, watch } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineOptions({ name: 'ArtSegmentedTabs' })

  interface TabOption {
    label: string
    value: string // 增强：插槽名通常为字符串
    icon?: string | Component
  }

  interface Props {
    /** 选中的值 */
    modelValue: string
    /** 选项列表 */
    options: TabOption[]
  }

  const props = defineProps<Props>()
  const emit = defineEmits(['update:modelValue', 'change'])

  // 提供给 hook 使用
  provide('segmented-tabs-active', toRef(props, 'modelValue'))

  const itemRefs = ref<Map<string, HTMLElement>>(new Map())
  const gliderStyle = reactive({
    width: '0px',
    left: '0px'
  })

  const setItemRef = (el: HTMLElement, value: string) => {
    if (el) {
      itemRefs.value.set(value, el)
    }
  }

  const updateGlider = () => {
    const activeEl = itemRefs.value.get(props.modelValue)
    if (activeEl) {
      const { offsetLeft, offsetWidth } = activeEl
      gliderStyle.left = `${offsetLeft}px`
      gliderStyle.width = `${offsetWidth}px`
    }
  }

  const handleToggle = (value: string) => {
    if (props.modelValue === value) return
    emit('update:modelValue', value)
    emit('change', value)
  }

  watch(
    () => props.modelValue,
    () => {
      nextTick(updateGlider)
    }
  )

  onMounted(() => {
    // 稍微延迟以确保布局完成
    setTimeout(updateGlider, 100)

    // 监听窗口大小变化以重新计算位置
    window.addEventListener('resize', updateGlider)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateGlider)
  })
</script>

<style scoped lang="scss">
  .art-segmented-tabs {
    user-select: none;
    border: 1px solid var(--art-gray-200);

    .active-tab {
      color: var(--theme-color) !important;
    }
  }

  [data-theme='dark'] {
    .art-segmented-tabs {
      border-color: var(--art-gray-800);
    }
  }

  .ease-spring {
    transition-timing-function: cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }

  /* 内容切换动画 */
  .slide-fade-enter-active,
  .slide-fade-leave-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .slide-fade-leave-active {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 0;
  }

  .slide-fade-enter-from {
    opacity: 0;
    transform: translateX(20px);
  }

  .slide-fade-leave-to {
    opacity: 0;
    transform: translateX(-20px);
  }
</style>
