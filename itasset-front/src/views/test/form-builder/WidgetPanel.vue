<template>
  <div class="widget-panel">
    <!-- 头部搜索 -->
    <div class="panel-header">
      <div class="panel-title">
        <i class="i-ri-apps-line text-primary" />
        <span>控件库</span>
      </div>
      <el-input
        v-model="searchText"
        placeholder="搜索控件..."
        size="small"
        clearable
        class="search-input"
      >
        <template #prefix>
          <i class="i-ri-search-line text-g-400" />
        </template>
      </el-input>
    </div>

    <!-- 控件分组列表 -->
    <div class="widget-groups">
      <div
        v-for="group in filteredGroups"
        :key="group.groupName"
        class="widget-group"
      >
        <div class="group-title">
          <i :class="group.icon" class="group-icon" />
          <span>{{ group.groupName }}</span>
        </div>
        <!-- 可拖拽控件列表（v-model 绑定, clone 属性克隆, sort=false 禁止自身排序） -->
        <VueDraggable
          v-model="group.widgets"
          :group="{ name: 'form-widgets', pull: 'clone', put: false }"
          :sort="false"
          :clone="cloneWidget"
          ghost-class="widget-ghost"
          class="widget-grid"
        >
          <div
            v-for="widget in group.widgets"
            :key="widget.type"
            class="widget-item"
            :title="widget.name"
          >
            <div class="widget-icon-wrap">
              <i :class="widget.icon" class="widget-icon" />
            </div>
            <span class="widget-name">{{ widget.name }}</span>
          </div>
        </VueDraggable>
      </div>

      <!-- 没有搜索结果 -->
      <div v-if="filteredGroups.length === 0" class="empty-tip">
        <i class="i-ri-search-line text-g-300 text-24" />
        <span>未找到匹配的控件</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { VueDraggable } from 'vue-draggable-plus'
  import { nanoid } from 'nanoid'
  import { widgetGroups } from './widget-config'
  import type { WidgetConfig } from './types'

  const searchText = ref('')

  /** 过滤后的控件分组 */
  const filteredGroups = computed(() => {
    if (!searchText.value.trim()) return widgetGroups
    const keyword = searchText.value.trim().toLowerCase()
    return widgetGroups
      .map((group) => ({
        ...group,
        widgets: group.widgets.filter((w) => w.name.includes(keyword) || w.type.includes(keyword))
      }))
      .filter((g) => g.widgets.length > 0)
  })

  /**
   * 克隆控件时生成唯一 ID
   * vue-draggable-plus 的 clone 函数在 pull:'clone' 时自动调用
   */
  const cloneWidget = (widget: WidgetConfig) => {
    const defaultWidget = widget.defaultWidget()
    return {
      ...defaultWidget,
      id: nanoid(8),
      // 字段名自动加序号避免重复
      prop: `field_${nanoid(4)}`
    }
  }
</script>

<style scoped lang="scss">
  .widget-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: var(--art-bg-color, #fff);
    border-right: 1px solid var(--art-border-color, #e5e7eb);
    overflow: hidden;
  }

  .panel-header {
    padding: 14px 12px 10px;
    border-bottom: 1px solid var(--art-border-color, #e5e7eb);
    flex-shrink: 0;
    background: var(--art-bg-color, #fff);
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 700;
    color: var(--art-text-gray-800);
    margin-bottom: 10px;
    letter-spacing: 0.3px;
  }

  .search-input {
    width: 100%;
  }

  .widget-groups {
    flex: 1;
    overflow-y: auto;
    padding: 8px 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--art-border-color, #e5e7eb);
      border-radius: 4px;
    }
  }

  .widget-group {
    margin-bottom: 4px;
  }

  .group-title {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px 6px;
    font-size: 11px;
    font-weight: 600;
    color: var(--art-text-gray-500);
    text-transform: uppercase;
    letter-spacing: 0.8px;
  }

  .group-icon {
    font-size: 13px;
    color: var(--art-text-gray-400);
  }

  .widget-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
    padding: 0 10px 6px;
  }

  .widget-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    padding: 10px 6px;
    border-radius: 6px;
    border: 1px solid var(--art-border-color, #e5e7eb);
    background: var(--art-bg-color, #fff);
    cursor: grab;
    transition: all 0.2s;
    user-select: none;

    &:hover {
      border-color: var(--theme-color);
      background: color-mix(in srgb, var(--theme-color) 5%, transparent);
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .widget-icon-wrap {
        background: color-mix(in srgb, var(--theme-color) 15%, transparent);
      }

      .widget-icon {
        color: var(--theme-color);
      }
    }

    &:active {
      cursor: grabbing;
      transform: scale(0.96);
    }
  }

  .widget-icon-wrap {
    width: 30px;
    height: 30px;
    border-radius: 6px;
    background: var(--art-gray-100);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
  }

  .widget-icon {
    font-size: 15px;
    color: var(--art-text-gray-500);
    transition: color 0.2s;
  }

  .widget-name {
    font-size: 11px;
    font-weight: 500;
    color: var(--art-text-gray-600);
    text-align: center;
    line-height: 1.2;
  }

  /* 拖拽幽灵样式 */
  .widget-ghost {
    opacity: 0.4;
    border: 2px dashed var(--theme-color) !important;
    background: color-mix(in srgb, var(--theme-color) 8%, transparent) !important;
  }

  /* 空结果提示 */
  .empty-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 40px 20px;
    font-size: 12px;
    color: var(--art-text-gray-400);
  }

  /* 暗黑模式 */
  [data-theme='dark'] {
    .widget-panel {
      background: var(--art-bg-color);
      border-right-color: var(--art-border-color);
    }

    .widget-item {
      background: var(--art-main-bg-color);
    }

    .widget-icon-wrap {
      background: var(--art-gray-800);
    }
  }
</style>
