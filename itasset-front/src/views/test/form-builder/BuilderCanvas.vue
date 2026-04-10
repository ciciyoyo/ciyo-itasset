<template>
  <div class="builder-canvas">
    <!-- 统一 VueDraggable 容器（始终存在，避免因 v-if/v-else 销毁重建导致 Sortablejs group 失联） -->
    <div
      class="canvas-inner"
      :class="{ 'is-empty': widgets.length === 0 }"
    >
      <!-- 空状态引导（不是拖拽项，pointer-events:none 避免干扰放置） -->
      <transition name="hint-fade">
        <div v-if="widgets.length === 0" class="canvas-empty-hint">
          <div class="empty-icon-wrap">
            <i class="i-ri-cursor-line empty-icon" />
          </div>
          <p class="empty-title">拖拽控件到此处</p>
          <p class="empty-desc">从左侧控件库将组件拖拽至画布开始设计表单</p>
        </div>
      </transition>

      <!-- 表单区域 -->
      <el-form
        :label-width="formSchema.labelWidth"
        class="canvas-form"
        @submit.prevent
      >
        <!--
          单一 VueDraggable，始终挂载在 DOM 上。
          put:true 允许从外部拖入；pull:false 不允许从此拖出。
          v-model 直接操作 formSchema.widgets（reactive 对象属性），
          不经过 emit → props 的替换，避免 Sortablejs 内部数组引用失效。
        -->
        <VueDraggable
          v-model="formSchema.widgets"
          :group="{ name: 'form-widgets', pull: false, put: true }"
          ghost-class="canvas-ghost"
          chosen-class="canvas-chosen"
          :animation="200"
          class="canvas-draggable-zone"
          handle=".drag-handle"
          @add="onAdd"
        >
          <el-col
            v-for="widget in formSchema.widgets"
            :key="widget.id"
            :span="widget.span"
          >
            <!-- 单个控件容器 -->
            <div
              class="widget-wrapper"
              :class="{
                'is-active': selectedId === widget.id,
                'is-divider': widget.type === 'divider'
              }"
              @click.stop="selectWidget(widget.id)"
            >
              <!-- 操作按钮浮层 -->
              <div class="widget-actions">
                <el-tooltip content="拖拽排序" placement="top" :show-after="400">
                  <button class="action-btn drag-handle" @click.stop>
                    <i class="i-ri-drag-move-2-line" />
                  </button>
                </el-tooltip>
                <el-tooltip content="复制" placement="top" :show-after="400">
                  <button class="action-btn" @click.stop="copyWidget(widget)">
                    <i class="i-ri-file-copy-line" />
                  </button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top" :show-after="400">
                  <button class="action-btn danger" @click.stop="deleteWidget(widget.id)">
                    <i class="i-ri-delete-bin-line" />
                  </button>
                </el-tooltip>
              </div>

              <!-- 分组标题：与项目弹窗中 "基本信息" 等标题样式完全一致 -->
              <template v-if="widget.type === 'divider'">
                <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-2">
                  {{ widget.title || '分组标题' }}
                </h3>
              </template>

              <!-- 两列布局容器 -->
              <template v-else-if="widget.type === 'grid'">
                <div class="grid-placeholder-label">
                  <i class="i-ri-layout-column-line mr-1" />两列布局容器
                </div>
                <!-- grid 内部也用独立 VueDraggable，直接操作 widget.children -->
                <VueDraggable
                  v-model="widget.children"
                  :group="{ name: 'form-widgets', pull: false, put: true }"
                  ghost-class="canvas-ghost"
                  :animation="200"
                  class="canvas-draggable-zone grid-inner"
                  handle=".drag-handle"
                >
                  <el-col
                    v-for="child in widget.children"
                    :key="child.id"
                    :span="12"
                  >
                    <div
                      class="widget-wrapper"
                      :class="{ 'is-active': selectedId === child.id }"
                      @click.stop="selectWidget(child.id)"
                    >
                      <div class="widget-actions">
                        <button class="action-btn drag-handle" @click.stop>
                          <i class="i-ri-drag-move-2-line" />
                        </button>
                        <button class="action-btn" @click.stop="copyWidget(child)">
                          <i class="i-ri-file-copy-line" />
                        </button>
                        <button class="action-btn danger" @click.stop="deleteWidgetFromGrid(widget, child.id)">
                          <i class="i-ri-delete-bin-line" />
                        </button>
                      </div>
                      <FormItemRenderer :widget="child" :preview="false" />
                    </div>
                  </el-col>
                  <!-- grid 内无控件时的占位提示 -->
                  <div v-if="!widget.children?.length" class="grid-empty-hint">
                    <i class="i-ri-arrow-down-line mr-1" />拖入控件到此区域
                  </div>
                </VueDraggable>
              </template>

              <!-- 普通表单控件 -->
              <template v-else>
                <FormItemRenderer :widget="widget" :preview="false" />
              </template>
            </div>
          </el-col>
        </VueDraggable>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { VueDraggable } from 'vue-draggable-plus'
  import { nanoid } from 'nanoid'
  import type { FormWidget, FormSchema } from './types'
  import FormItemRenderer from './FormItemRenderer.vue'

  const props = defineProps<{
    /** 直接传入 reactive 的 formSchema，子组件可安全 mutate 其属性（不替换引用） */
    formSchema: FormSchema
    selectedId: string | null
  }>()

  const emit = defineEmits<{
    (e: 'update:selectedId', id: string | null): void
  }>()

  /** widgets 快捷访问 */
  const widgets = computed(() => props.formSchema.widgets)

  /** 选中控件 */
  const selectWidget = (id: string) => {
    emit('update:selectedId', id)
  }

  /** 拖入后自动选中最新控件 */
  const onAdd = () => {
    setTimeout(() => {
      const ws = props.formSchema.widgets
      if (ws.length) emit('update:selectedId', ws[ws.length - 1].id)
    }, 50)
  }

  /** 复制控件（原地 splice，不替换数组引用） */
  const copyWidget = (widget: FormWidget) => {
    const newWidget: FormWidget = {
      ...JSON.parse(JSON.stringify(widget)),
      id: nanoid(8),
      prop: `${widget.prop}_copy_${nanoid(4)}`
    }
    const ws = props.formSchema.widgets
    const idx = ws.findIndex((w) => w.id === widget.id)
    ws.splice(idx + 1, 0, newWidget)
    emit('update:selectedId', newWidget.id)
  }

  /** 删除控件（原地 splice，不替换数组引用） */
  const deleteWidget = (id: string) => {
    const ws = props.formSchema.widgets
    const idx = ws.findIndex((w) => w.id === id)
    if (idx !== -1) ws.splice(idx, 1)
    if (props.selectedId === id) emit('update:selectedId', null)
  }

  /** 删除 grid 内子控件 */
  const deleteWidgetFromGrid = (gridWidget: FormWidget, childId: string) => {
    if (!gridWidget.children) return
    const idx = gridWidget.children.findIndex((c) => c.id === childId)
    if (idx !== -1) gridWidget.children.splice(idx, 1)
    if (props.selectedId === childId) emit('update:selectedId', null)
  }
</script>

<style scoped lang="scss">
  .builder-canvas {
    flex: 1;
    height: 100%;
    overflow-y: auto;
    background: #f5f6f8;
    padding: 20px;

    &::-webkit-scrollbar {
      width: 5px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 4px;
    }
  }

  .canvas-inner {
    min-height: 100%;
    position: relative;
    display: flex;
    flex-direction: column;

    &.is-empty {
      /* 空状态时撑满高度，方便拖入 */
      min-height: calc(100vh - 92px);
    }
  }

  /* 空状态引导 */
  .canvas-empty-hint {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    pointer-events: none; /* 不干扰拖拽放置 */
    z-index: 1;
  }

  .empty-icon-wrap {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: #f3f4f6;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
  }

  .empty-icon {
    font-size: 28px;
    color: #9ca3af;
  }

  .empty-title {
    font-size: 16px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 6px;
  }

  .empty-desc {
    font-size: 13px;
    color: #9ca3af;
  }

  /* 表单区域 */
  .canvas-form {
    flex: 1;
  }

  /* 统一拖拽区域样式 */
  .canvas-draggable-zone {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
    min-height: 80px; /* 确保即使为空也有足够的放置区域 */
    padding: 6px;
    border-radius: 8px;
    background: #fff;
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    transition: border-color 0.25s, box-shadow 0.25s;

    /* 可接受时的高亮（Sortablejs 拖入时自动添加 .sortable-drag-over 类） */
    &.sortable-drag-over {
      border-color: var(--theme-color);
      box-shadow: 0 0 0 2px color-mix(in srgb, var(--theme-color) 15%, transparent);
    }
  }

  /* 单个控件容器 */
  .widget-wrapper {
    position: relative;
    border-radius: 6px;
    margin-bottom: 8px;
    padding: 10px 10px 4px;
    border: 1px solid transparent;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: var(--theme-color);
      box-shadow: 0 0 0 2px color-mix(in srgb, var(--theme-color) 12%, transparent);

      .widget-actions {
        opacity: 1;
        transform: translateY(0);
      }
    }

    &.is-active {
      border-color: var(--theme-color);
      box-shadow: 0 0 0 2px color-mix(in srgb, var(--theme-color) 18%, transparent);
      background: color-mix(in srgb, var(--theme-color) 2%, transparent);

      .widget-actions {
        opacity: 1;
        transform: translateY(0);
      }
    }

    &.is-divider {
      padding: 6px 10px;
      margin-bottom: 4px;
    }
  }

  /* 操作按钮浮层 */
  .widget-actions {
    position: absolute;
    top: -14px;
    right: 6px;
    display: flex;
    align-items: center;
    gap: 2px;
    background: var(--theme-color);
    border-radius: 4px;
    padding: 2px 3px;
    opacity: 0;
    transform: translateY(4px);
    transition: all 0.18s;
    z-index: 10;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 3px;
    border: none;
    background: transparent;
    color: rgba(255, 255, 255, 0.85);
    font-size: 13px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }

    &.danger:hover {
      background: rgba(239, 68, 68, 0.6);
    }

    &.drag-handle {
      cursor: grab;

      &:active {
        cursor: grabbing;
      }
    }
  }

  /* 分组标题 */
  .form-section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 700;
    color: var(--theme-color);
    margin: 0;
    padding: 2px 0;
  }

  .title-bar {
    display: inline-block;
    width: 4px;
    height: 16px;
    border-radius: 2px;
    background: var(--theme-color);
    flex-shrink: 0;
  }

  /* grid 内部 */
  .grid-placeholder-label {
    display: flex;
    align-items: center;
    font-size: 11px;
    color: var(--art-text-gray-400);
    margin-bottom: 6px;
    padding: 0 2px;
  }

  .grid-inner {
    min-height: 60px;
    border: 1px dashed #d1d5db;
    border-radius: 6px;
    background: #f9fafb;
    padding: 4px;
    box-shadow: none;
  }

  .grid-empty-hint {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50px;
    font-size: 12px;
    color: #9ca3af;
    pointer-events: none;
  }

  /* 拖拽幽灵 */
  .canvas-ghost {
    border: 2px dashed var(--theme-color) !important;
    background: color-mix(in srgb, var(--theme-color) 6%, transparent) !important;
    opacity: 0.6;
    border-radius: 6px;
  }

  .canvas-chosen {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
    opacity: 0.9;
  }

  /* 空状态过渡动画 */
  .hint-fade-enter-active,
  .hint-fade-leave-active {
    transition: opacity 0.3s;
  }

  .hint-fade-enter-from,
  .hint-fade-leave-to {
    opacity: 0;
  }

  /* 暗黑模式 */
  [data-theme='dark'] {
    .builder-canvas {
      background: #1a1d23;
    }

    .canvas-draggable-zone {
      background: var(--art-bg-color);
      border-color: var(--art-border-color);
    }

    .empty-icon-wrap {
      background: var(--art-gray-800);
    }

    .empty-title {
      color: var(--art-text-gray-200);
    }

    .grid-inner {
      background: var(--art-main-bg-color);
      border-color: var(--art-border-color);
    }
  }
</style>
