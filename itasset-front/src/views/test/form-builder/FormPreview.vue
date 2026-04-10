<template>
  <el-dialog
    v-model="visible"
    title="表单预览"
    width="640px"
    append-to-body
    destroy-on-close
    class="preview-dialog"
  >
    <template #header>
      <div class="preview-dialog-header">
        <div class="preview-title">
          <i class="i-ri-eye-line mr-2 text-primary" />
          <span>表单预览</span>
        </div>
        <span class="preview-tip">以下为实际运行效果，控件完全可交互</span>
      </div>
    </template>

    <div class="preview-body" v-if="schema">
      <el-form
        ref="previewFormRef"
        :model="formData"
        :label-width="schema.labelWidth"
        class="preview-form"
      >
        <el-row :gutter="schema.gutter">
          <template v-for="widget in schema.widgets" :key="widget.id">
            <!-- 分组标题：与项目弹窗中的分组标题样式完全一致 -->
            <el-col v-if="widget.type === 'divider'" :span="24">
              <h3 class="font-bold border-l-4 border-primary pl-3 mb-4 text-sm mt-2">
                {{ widget.title || '分组标题' }}
              </h3>
            </el-col>

            <!-- grid 两列容器 -->
            <el-col v-else-if="widget.type === 'grid'" :span="24">
              <el-row :gutter="schema.gutter">
                <el-col
                  v-for="child in widget.children"
                  :key="child.id"
                  :span="12"
                >
                  <FormItemRenderer :widget="child" :preview="true" :model-value="formData" />
                </el-col>
              </el-row>
            </el-col>

            <!-- 普通控件 -->
            <el-col v-else :span="widget.span">
              <FormItemRenderer :widget="widget" :preview="true" :model-value="formData" />
            </el-col>
          </template>
        </el-row>
      </el-form>
    </div>

    <template #footer>
      <div class="preview-footer">
        <div class="preview-form-data">
          <el-popover placement="top" trigger="click" :width="440">
            <template #reference>
              <el-button size="small" text>
                <i class="i-ri-code-s-slash-line mr-1" />查看表单数据
              </el-button>
            </template>
            <pre class="form-data-pre">{{ JSON.stringify(formData, null, 2) }}</pre>
          </el-popover>
        </div>
        <div class="preview-footer-btns">
          <el-button @click="resetForm">
            <i class="i-ri-loop-left-line mr-1.5" />重置
          </el-button>
          <el-button type="primary" @click="submitForm">
            <i class="i-ri-check-line mr-1.5" />提交（模拟）
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { reactive, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormSchema } from './types'
  import FormItemRenderer from './FormItemRenderer.vue'

  const props = defineProps<{
    schema: FormSchema | null
  }>()

  const visible = defineModel<boolean>('visible', { default: false })

  const previewFormRef = ref()

  /** 表单数据对象，根据 schema 动态生成 */
  const formData = reactive<Record<string, any>>({})

  /** 当弹窗打开时，根据 schema 初始化 formData */
  watch(visible, (val) => {
    if (val && props.schema) {
      // 清空并重建
      Object.keys(formData).forEach((k) => delete formData[k])
      initFormData(props.schema)
    }
  })

  const initFormData = (schema: FormSchema) => {
    schema.widgets.forEach((widget) => {
      if (widget.type === 'grid') {
        widget.children?.forEach((child) => {
          formData[child.prop] = child.type === 'switch' ? false : undefined
        })
      } else if (widget.prop) {
        formData[widget.prop] = widget.type === 'switch' ? false : undefined
      }
    })
  }

  const resetForm = () => {
    previewFormRef.value?.resetFields()
  }

  const submitForm = () => {
    previewFormRef.value?.validate((valid: boolean) => {
      if (valid) {
        ElMessage.success('验证通过！实际业务场景请替换为 API 调用')
      }
    })
  }
</script>

<style lang="scss">
  /* 非 scoped，覆盖 el-dialog */
  .preview-dialog {
    .el-dialog__header {
      padding: 16px 20px 14px;
      border-bottom: 1px solid #f3f4f6;
      margin: 0;
    }

    .el-dialog__body {
      padding: 0;
    }

    .el-dialog__footer {
      padding: 0;
      border-top: 1px solid #f3f4f6;
    }
  }
</style>

<style scoped lang="scss">
  .preview-dialog-header {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .preview-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 700;
  }

  .preview-tip {
    font-size: 12px;
    color: #9ca3af;
  }

  .preview-body {
    padding: 24px 24px 16px;
    max-height: 65vh;
    overflow-y: auto;
  }

  .preview-section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 700;
    color: var(--theme-color);
    margin: 8px 0 16px;
  }

  .title-bar {
    display: inline-block;
    width: 4px;
    height: 16px;
    border-radius: 2px;
    background: var(--theme-color);
    flex-shrink: 0;
  }

  .preview-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 20px;
  }

  .preview-footer-btns {
    display: flex;
    gap: 8px;
  }

  .form-data-pre {
    font-size: 12px;
    font-family: 'Fira Code', 'Courier New', monospace;
    color: #374151;
    max-height: 300px;
    overflow-y: auto;
    white-space: pre-wrap;
    word-break: break-all;
    margin: 0;
    background: #f9fafb;
    border-radius: 6px;
    padding: 12px;
  }
</style>
