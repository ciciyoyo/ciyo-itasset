<template>
  <div class="builder-toolbar">
    <!-- 左侧 Logo 区域 -->
    <div class="toolbar-left">
      <div class="toolbar-logo">
        <i class="i-ri-stack-line text-primary" />
        <span>Form Builder</span>
      </div>
      <div class="toolbar-divider" />
      <span class="toolbar-hint">
        <i class="i-ri-lightbulb-line mr-1 text-amber-500" />
        拖拽控件到画布，点击控件编辑属性
      </span>
    </div>

    <!-- 右侧工具按钮 -->
    <div class="toolbar-right">
      <!-- 全局表单配置 -->
      <el-popover placement="bottom-end" :width="240" trigger="click">
        <template #reference>
          <el-button size="small" class="toolbar-btn">
            <i class="i-ri-settings-line mr-1.5" />
            表单配置
          </el-button>
        </template>
        <div class="form-config-popover">
          <div class="config-item">
            <label>Label 宽度</label>
            <el-input v-model="formSchema.labelWidth" size="small" placeholder="100px" />
          </div>
          <div class="config-item">
            <label>栅格间距</label>
            <el-input-number
              v-model="formSchema.gutter"
              :min="0"
              :max="40"
              :controls="false"
              size="small"
              style="width: 100%"
            />
          </div>
        </div>
      </el-popover>

      <el-divider direction="vertical" />

      <!-- 导入 JSON -->
      <el-button size="small" class="toolbar-btn" @click="emit('import-json')">
        <i class="i-ri-upload-line mr-1.5" />
        导入
      </el-button>

      <!-- 导出 JSON -->
      <el-button size="small" class="toolbar-btn" @click="emit('export-json')">
        <i class="i-ri-code-s-slash-line mr-1.5" />
        JSON
      </el-button>

      <el-divider direction="vertical" />

      <!-- 清空 -->
      <el-popconfirm
        title="确定清空画布中的所有控件吗？"
        confirm-button-text="确定清空"
        cancel-button-text="取消"
        confirm-button-type="danger"
        :width="220"
        @confirm="emit('clear')"
      >
        <template #reference>
          <el-button size="small" class="toolbar-btn" :disabled="widgetCount === 0">
            <i class="i-ri-delete-bin-line mr-1.5 text-red-400" />
            清空
          </el-button>
        </template>
      </el-popconfirm>

      <!-- 预览 -->
      <el-button size="small" type="primary" class="toolbar-btn preview-btn" @click="emit('preview')">
        <i class="i-ri-eye-line mr-1.5" />
        预览表单
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormSchema } from './types'

  const props = defineProps<{
    formSchema: FormSchema
    widgetCount: number
  }>()

  const emit = defineEmits<{
    (e: 'preview'): void
    (e: 'export-json'): void
    (e: 'import-json'): void
    (e: 'clear'): void
  }>()
</script>

<style scoped lang="scss">
  .builder-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 52px;
    padding: 0 16px;
    background: var(--art-bg-color, #fff);
    border-bottom: 1px solid var(--art-border-color, #e5e7eb);
    flex-shrink: 0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  }

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .toolbar-logo {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 700;
    color: var(--art-text-gray-800);
    letter-spacing: -0.3px;
  }

  .toolbar-divider {
    width: 1px;
    height: 18px;
    background: var(--art-border-color, #e5e7eb);
  }

  .toolbar-hint {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: var(--art-text-gray-400);

    @media (max-width: 900px) {
      display: none;
    }
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .toolbar-btn {
    font-size: 12px;
    border-radius: 6px;
    display: flex;
    align-items: center;
  }

  .preview-btn {
    font-weight: 600;
    letter-spacing: 0.3px;
  }

  /* 表单配置弹出层 */
  .form-config-popover {
    padding: 4px 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .config-item {
    label {
      display: block;
      font-size: 12px;
      font-weight: 500;
      color: var(--art-text-gray-600);
      margin-bottom: 6px;
    }
  }

  /* 暗黑模式 */
  [data-theme='dark'] {
    .builder-toolbar {
      background: var(--art-bg-color);
      border-bottom-color: var(--art-border-color);
    }
  }
</style>
