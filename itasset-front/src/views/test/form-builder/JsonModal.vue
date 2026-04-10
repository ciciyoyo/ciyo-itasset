<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'export' ? '生成的 JSON Schema' : '导入 JSON Schema'"
    width="600px"
    append-to-body
    destroy-on-close
  >
    <template #header>
      <div class="json-dialog-header">
        <div class="json-title">
          <i class="i-ri-code-s-slash-line mr-2 text-primary" />
          <span>{{ mode === 'export' ? '生成 JSON Schema' : '导入 JSON Schema' }}</span>
        </div>
        <span class="json-tip">{{ mode === 'export' ? '可复制此配置用于代码集成或存储' : '粘贴 JSON 配置以恢复画布' }}</span>
      </div>
    </template>

    <div class="json-body">
      <!-- 导出模式：只读显示 -->
      <div v-if="mode === 'export'" class="json-export-wrap">
        <div class="json-toolbar">
          <span class="json-stat">
            共 <b>{{ schemaWidgetCount }}</b> 个控件
          </span>
          <el-button size="small" type="primary" text @click="copyJson">
            <i class="i-ri-file-copy-line mr-1" />一键复制
          </el-button>
        </div>
        <pre class="json-pre">{{ formattedJson }}</pre>
      </div>

      <!-- 导入模式：可编辑文本框 -->
      <div v-else class="json-import-wrap">
        <el-alert
          title="注意：导入将覆盖当前画布中的所有控件"
          type="warning"
          :closable="false"
          show-icon
          class="mb-3"
        />
        <el-input
          v-model="importText"
          type="textarea"
          :rows="16"
          placeholder="请粘贴 JSON Schema..."
          class="json-textarea"
          :class="{ 'has-error': parseError }"
        />
        <span v-if="parseError" class="parse-error-tip">
          <i class="i-ri-error-warning-line mr-1" />{{ parseError }}
        </span>
      </div>
    </div>

    <template #footer>
      <div class="json-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button v-if="mode === 'import'" type="primary" @click="handleImport">
          <i class="i-ri-upload-line mr-1.5" />确认导入
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormSchema } from './types'

  const props = defineProps<{
    mode: 'export' | 'import'
    schema: FormSchema | null
  }>()

  const emit = defineEmits<{
    (e: 'import', schema: FormSchema): void
  }>()

  const visible = defineModel<boolean>('visible', { default: false })

  /** 格式化输出的 JSON 字符串 */
  const formattedJson = computed(() => {
    if (!props.schema) return ''
    return JSON.stringify(props.schema, null, 2)
  })

  /** 控件总数（含 grid 内嵌控件） */
  const schemaWidgetCount = computed(() => {
    if (!props.schema) return 0
    return props.schema.widgets.reduce((total, w) => {
      if (w.type === 'grid') return total + (w.children?.length ?? 0)
      return total + 1
    }, 0)
  })

  /** 复制 JSON 到剪贴板 */
  const copyJson = async () => {
    try {
      await navigator.clipboard.writeText(formattedJson.value)
      ElMessage.success('已复制到剪贴板')
    } catch {
      ElMessage.error('复制失败，请手动选中复制')
    }
  }

  /** 导入相关 */
  const importText = ref('')
  const parseError = ref('')

  /** 打开弹窗时清空上次的输入 */
  watch(visible, (val) => {
    if (val && props.mode === 'import') {
      importText.value = ''
      parseError.value = ''
    }
  })

  /** 解析并导入 */
  const handleImport = () => {
    parseError.value = ''
    try {
      const parsed = JSON.parse(importText.value)
      // 简单校验
      if (!parsed.widgets || !Array.isArray(parsed.widgets)) {
        parseError.value = 'JSON 格式无效：缺少 "widgets" 数组'
        return
      }
      emit('import', parsed as FormSchema)
      visible.value = false
      ElMessage.success(`成功导入 ${parsed.widgets.length} 个控件`)
    } catch (e: any) {
      parseError.value = `JSON 解析失败：${e.message}`
    }
  }
</script>

<style lang="scss">
  /* 非 scoped，覆盖 el-dialog */
  .json-dialog-wrap {
    .el-dialog__header {
      margin: 0;
    }
  }
</style>

<style scoped lang="scss">
  .json-dialog-header {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  .json-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 700;
  }

  .json-tip {
    font-size: 12px;
    color: #9ca3af;
  }

  .json-body {
    padding: 16px 20px;
  }

  .json-export-wrap {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .json-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .json-stat {
    font-size: 12px;
    color: #9ca3af;

    b {
      color: var(--theme-color);
      font-weight: 700;
    }
  }

  .json-pre {
    background: #1e1e2e;
    color: #cdd6f4;
    border-radius: 8px;
    padding: 16px;
    font-size: 12px;
    font-family: 'Fira Code', 'Courier New', monospace;
    max-height: 450px;
    overflow-y: auto;
    white-space: pre;
    word-break: normal;
    overflow-x: auto;
    margin: 0;
    line-height: 1.6;
  }

  .json-textarea {
    font-family: 'Fira Code', 'Courier New', monospace;
    font-size: 12px;

    &.has-error :deep(.el-textarea__inner) {
      border-color: #ef4444;
    }
  }

  .parse-error-tip {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #ef4444;
    margin-top: 6px;
  }

  .json-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 12px 20px;
    border-top: 1px solid #f3f4f6;
  }

  .mb-3 {
    margin-bottom: 12px;
  }
</style>
