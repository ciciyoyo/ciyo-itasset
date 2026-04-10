<template>
  <div class="form-builder-page">
    <!-- 顶部工具栏 -->
    <ToolBar
      :form-schema="formSchema"
      :widget-count="formSchema.widgets.length"
      @preview="showPreview = true"
      @export-json="openJsonModal('export')"
      @import-json="openJsonModal('import')"
      @clear="clearCanvas"
    />

    <!-- 主体三栏布局 -->
    <div class="builder-main">
      <!-- 左侧：控件面板 -->
      <div class="left-panel">
        <WidgetPanel />
      </div>

      <!-- 中间：画布 -->
      <div class="center-canvas" @click.self="deselectWidget">
        <BuilderCanvas
          :form-schema="formSchema"
          :selected-id="selectedId"
          @update:selected-id="selectedId = $event"
        />
      </div>

      <!-- 右侧：属性面板 -->
      <div class="right-panel">
        <PropertyPanel
          :selected-widget="selectedWidget"
          @deselect="deselectWidget"
        />
      </div>
    </div>

    <!-- 预览弹窗 -->
    <FormPreview v-model:visible="showPreview" :schema="formSchema" />

    <!-- JSON 弹窗 -->
    <JsonModal
      v-model:visible="showJsonModal"
      :mode="jsonModalMode"
      :schema="formSchema"
      @import="handleImportSchema"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue'
  import type { FormSchema, FormWidget } from './form-builder/types'
  import ToolBar from './form-builder/ToolBar.vue'
  import WidgetPanel from './form-builder/WidgetPanel.vue'
  import BuilderCanvas from './form-builder/BuilderCanvas.vue'
  import PropertyPanel from './form-builder/PropertyPanel.vue'
  import FormPreview from './form-builder/FormPreview.vue'
  import JsonModal from './form-builder/JsonModal.vue'

  defineOptions({ name: 'FormBuilderPage' })

  // =====================
  // 核心状态
  // =====================

  /** 整个表单的 schema */
  const formSchema = reactive<FormSchema>({
    labelWidth: '100px',
    gutter: 20,
    widgets: []
  })

  /** 当前选中的控件 ID */
  const selectedId = ref<string | null>(null)

  /** 通过 ID 找到选中的控件（含 grid 内部控件） */
  const selectedWidget = computed<FormWidget | null>(() => {
    if (!selectedId.value) return null
    for (const w of formSchema.widgets) {
      if (w.id === selectedId.value) return w
      if (w.type === 'grid' && w.children) {
        const child = w.children.find((c) => c.id === selectedId.value)
        if (child) return child
      }
    }
    return null
  })

  /** 取消选中 */
  const deselectWidget = () => {
    selectedId.value = null
  }

  // =====================
  // 工具栏操作
  // =====================

  /** 清空画布（原地清空，不替换数组引用，保证 Sortablejs 内部引用有效） */
  const clearCanvas = () => {
    formSchema.widgets.splice(0, formSchema.widgets.length)
    selectedId.value = null
  }

  // =====================
  // 弹窗状态
  // =====================

  const showPreview = ref(false)

  const showJsonModal = ref(false)
  const jsonModalMode = ref<'export' | 'import'>('export')

  const openJsonModal = (mode: 'export' | 'import') => {
    jsonModalMode.value = mode
    showJsonModal.value = true
  }

  /** 接收导入的 schema（原地操作，保证 Sortablejs 数组引用有效） */
  const handleImportSchema = (schema: FormSchema) => {
    formSchema.labelWidth = schema.labelWidth || '100px'
    formSchema.gutter = schema.gutter ?? 20
    // 原地替换数组内容（不替换引用）
    formSchema.widgets.splice(0, formSchema.widgets.length, ...(schema.widgets || []))
    selectedId.value = null
  }
</script>

<style scoped lang="scss">
  .form-builder-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
    background: #f5f6f8;
  }

  .builder-main {
    display: flex;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  /* 左侧控件面板 */
  .left-panel {
    width: 200px;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
  }

  /* 中间画布 */
  .center-canvas {
    flex: 1;
    height: 100%;
    overflow: hidden;
    min-width: 0;
  }

  /* 右侧属性面板 */
  .right-panel {
    width: 260px;
    flex-shrink: 0;
    height: 100%;
    overflow: hidden;
  }

  /* 响应式：小屏幕隐藏提示 */
  @media (max-width: 768px) {
    .left-panel {
      width: 160px;
    }

    .right-panel {
      width: 220px;
    }
  }
</style>
