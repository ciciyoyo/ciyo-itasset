<template>
  <div class="icon-select-container">
    <div class="icon-body">
      <el-input
        v-model="name"
        style="position: relative"
        clearable
        :placeholder="$t('system.menu.enterIconName')"
        @input="filterIcons"
      >
        <template #suffix>
          <el-icon class="el-input__icon">
            <ele-Search />
          </el-icon>
        </template>
      </el-input>
      <div class="icon-list">
        <div v-for="(v, index) in tempIconList" :key="index" @click="selectedIcon(v)" class="icon-item">
          <ArtSvgIcon :icon="v" class="icon-display" />
          <span class="icon-name">{{ v }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, ref } from 'vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { allIcons } from '@/utils/ui/icons'

  const name = ref('')
  const iconList = ref<string[]>([])
  const tempIconList = ref<string[]>([])

  onMounted(() => {
    iconList.value = allIcons
    tempIconList.value = allIcons
  })

  function filterIcons() {
    tempIconList.value = iconList.value.filter((item) => item.includes(name.value))
  }

  // 定义props
  const emit = defineEmits(['selected'])

  const selectedIcon = (iconName: string) => {
    emit('selected', iconName)
    document.body.click()
  }

  const reset = () => {
    name.value = ''
    tempIconList.value = iconList.value
  }

  defineExpose({
    reset
  })
</script>

<style lang="scss" scoped>
  .icon-select-container {
    .icon-body {
      width: 100%;
      padding: 10px;

      .icon-list {
        height: 300px;
        overflow-y: scroll;
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .icon-item {
          width: calc(33.33% - 6px);
          height: 70px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          padding: 8px;
          border-radius: 4px;
          transition: all 0.3s;

          &:hover {
            background-color: var(--el-fill-color-light);
          }

          .icon-display {
            font-size: 24px;
            margin-bottom: 4px;
          }

          .icon-name {
            font-size: 12px;
            text-align: center;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            width: 100%;
          }
        }
      }
    }
  }
</style>
