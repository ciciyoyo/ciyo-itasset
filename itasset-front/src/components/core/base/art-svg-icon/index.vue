<template>
  <!-- 直接利用 UnoCSS 的图标类名，格式如: i-ri-home-line -->
  <i v-if="icon" :class="['art-svg-icon inline-block', unoIconClass]" v-bind="$attrs" />
</template>

<script setup lang="ts">
  const props = defineProps<{ icon?: string }>()

  const unoIconClass = computed(() => {
    if (!props.icon) return ''
    // 将 "ri:home-line" 转换为 "i-ri-home-line"
    return `i-${props.icon.replace(':', '-')}`
  })
</script>
