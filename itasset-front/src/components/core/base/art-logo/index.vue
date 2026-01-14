<!-- 系统logo -->
<template>
  <div class="flex-cc">
    <img :style="logoStyle" :src="logoImg" alt="logo" class="w-full h-full" />
  </div>
</template>

<script setup lang="ts">
  import { useSystemStore } from '@/store/modules/system'

  defineOptions({ name: 'ArtLogo' })

  interface Props {
    /** logo 大小 */
    size?: number | string
  }

  const props = withDefaults(defineProps<Props>(), {
    size: 36
  })

  const logoStyle = computed(() => ({ width: `${props.size}px` }))

  const systemStore = useSystemStore()

  const { systemInfo } = storeToRefs(systemStore)

  const logoImg = computed(() => systemInfo.value.logoImg)
</script>
