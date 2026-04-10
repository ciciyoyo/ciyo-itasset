<template>
  <div class="property-panel">
    <!-- 无选中状态 -->
    <div v-if="!selectedWidget" class="property-empty">
      <div class="property-empty-content">
        <i class="i-ri-cursor-line empty-icon" />
        <p class="empty-title">未选中控件</p>
        <p class="empty-desc">点击画布中的控件<br />即可编辑其属性</p>
      </div>
    </div>

    <!-- 有选中控件时显示属性编辑器 -->
    <template v-else>
      <!-- 属性面板头部 -->
      <div class="panel-header">
        <div class="widget-type-tag">
          <i :class="getWidgetIcon(selectedWidget.type)" class="mr-1.5" />
          <span>{{ getWidgetName(selectedWidget.type) }}</span>
        </div>
        <button class="close-btn" title="取消选中" @click="emit('deselect')">
          <i class="i-ri-close-line" />
        </button>
      </div>

      <!-- 属性表单 -->
      <div class="property-body">
        <!-- ======== 通用属性 ======== -->
        <div class="prop-section">
          <div class="section-title">基础属性</div>

          <!-- 分组标题控件特殊处理 -->
          <template v-if="selectedWidget.type === 'divider'">
            <div class="prop-item">
              <label class="prop-label">标题文字</label>
              <el-input v-model="selectedWidget.title" placeholder="请输入分组标题" size="small" />
            </div>
          </template>

          <!-- grid 容器 -->
          <template v-else-if="selectedWidget.type === 'grid'">
            <div class="prop-item">
              <label class="prop-label">说明</label>
              <div class="prop-desc-tip">拖入控件到画布中的两列容器内即可</div>
            </div>
          </template>

          <!-- 普通控件 -->
          <template v-else>
            <div class="prop-item">
              <label class="prop-label">
                标签名
                <span class="required-star">*</span>
              </label>
              <el-input v-model="selectedWidget.label" placeholder="请输入标签名" size="small" />
            </div>
            <div class="prop-item">
              <label class="prop-label">
                字段名 (prop)
                <span class="required-star">*</span>
              </label>
              <el-input
                v-model="selectedWidget.prop"
                placeholder="英文字段名，如 userName"
                size="small"
              />
            </div>
            <div class="prop-item" v-if="hasPlaceholder">
              <label class="prop-label">占位文字</label>
              <el-input
                v-model="selectedWidget.placeholder"
                placeholder="请输入占位提示文字"
                size="small"
              />
            </div>
          </template>

          <!-- 宽度设置（除除分割操作/grid，以及尶0大的控件） -->
          <template v-if="selectedWidget.type !== 'divider' && selectedWidget.type !== 'grid'">
            <div class="prop-item">
              <label class="prop-label">宽度</label>
              <el-radio-group v-model="selectedWidget.span" size="small">
                <el-radio-button :value="12">半宽 (1/2)</el-radio-button>
                <el-radio-button :value="24">全宽 (1/1)</el-radio-button>
              </el-radio-group>
            </div>

            <div class="prop-item prop-item-inline">
              <label class="prop-label">必填</label>
              <el-switch v-model="selectedWidget.required" size="small" />
            </div>

            <div
              v-if="hasClearable"
              class="prop-item prop-item-inline"
            >
              <label class="prop-label">可清除</label>
              <el-switch v-model="selectedWidget.clearable" size="small" />
            </div>

            <div class="prop-item prop-item-inline">
              <label class="prop-label">禁用</label>
              <el-switch v-model="selectedWidget.disabled" size="small" />
            </div>
          </template>
        </div>

        <!-- ======== number 特有属性 ======== -->
        <div v-if="selectedWidget.type === 'number'" class="prop-section">
          <div class="section-title">数字属性</div>
          <div class="prop-item">
            <label class="prop-label">最小值</label>
            <el-input-number
              v-model="selectedWidget.min"
              :controls="false"
              size="small"
              style="width: 100%"
            />
          </div>
          <div class="prop-item">
            <label class="prop-label">最大值</label>
            <el-input-number
              v-model="selectedWidget.max"
              :controls="false"
              size="small"
              style="width: 100%"
            />
          </div>
          <div class="prop-item">
            <label class="prop-label">小数位数</label>
            <el-input-number
              v-model="selectedWidget.precision"
              :min="0"
              :max="10"
              :controls="false"
              size="small"
              style="width: 100%"
            />
          </div>
        </div>

        <!-- ======== date / date-range / datetime 特有属性 ======== -->
        <div
          v-if="['date','date-range','datetime'].includes(selectedWidget.type)"
          class="prop-section"
        >
          <div class="section-title">日期属性</div>
          <div class="prop-item">
            <label class="prop-label">输出格式</label>
            <el-select v-model="selectedWidget.valueFormat" size="small" style="width: 100%">
              <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
              <el-option label="YYYY/MM/DD" value="YYYY/MM/DD" />
              <el-option label="YYYY-MM-DD HH:mm:ss" value="YYYY-MM-DD HH:mm:ss" />
              <el-option label="时间戳 (毫秒)" value="x" />
            </el-select>
          </div>
        </div>

        <!-- ======== select 特有属性 ======== -->
        <div v-if="selectedWidget.type === 'select'" class="prop-section">
          <div class="section-title">
            <span>选项配置</span>
            <button class="section-btn" @click="addOption">
              <i class="i-ri-add-line mr-0.5" />添加
            </button>
          </div>
          <div class="options-list">
            <div
              v-for="(opt, idx) in selectedWidget.options"
              :key="idx"
              class="option-item"
            >
              <el-input v-model="opt.label" placeholder="显示文字" size="small" class="flex-1" />
              <el-input v-model="opt.value" placeholder="值" size="small" class="flex-1" />
              <button class="opt-del-btn" @click="removeOption(idx)">
                <i class="i-ri-delete-bin-line" />
              </button>
            </div>
            <div v-if="!selectedWidget.options?.length" class="options-empty">
              暂无选项，点击上方"添加"
            </div>
          </div>
        </div>

        <!-- ======== radio 单选组特有属性 ======== -->
        <div v-if="selectedWidget.type === 'radio'" class="prop-section">
          <div class="section-title">
            <span>选项配置</span>
            <button class="section-btn" @click="addOption">
              <i class="i-ri-add-line mr-0.5" />添加
            </button>
          </div>
          <div class="options-list">
            <div v-for="(opt, idx) in selectedWidget.options" :key="idx" class="option-item">
              <el-input v-model="opt.label" placeholder="显示文字" size="small" class="flex-1" />
              <el-input v-model="opt.value" placeholder="值" size="small" class="flex-1" />
              <button class="opt-del-btn" @click="removeOption(idx)">
                <i class="i-ri-delete-bin-line" />
              </button>
            </div>
            <div v-if="!selectedWidget.options?.length" class="options-empty">暂无选项</div>
          </div>
        </div>

        <!-- ======== checkbox 多选组特有属性 ======== -->
        <div v-if="selectedWidget.type === 'checkbox'" class="prop-section">
          <div class="section-title">
            <span>选项配置</span>
            <button class="section-btn" @click="addOption">
              <i class="i-ri-add-line mr-0.5" />添加
            </button>
          </div>
          <div class="options-list">
            <div v-for="(opt, idx) in selectedWidget.options" :key="idx" class="option-item">
              <el-input v-model="opt.label" placeholder="显示文字" size="small" class="flex-1" />
              <el-input v-model="opt.value" placeholder="值" size="small" class="flex-1" />
              <button class="opt-del-btn" @click="removeOption(idx)">
                <i class="i-ri-delete-bin-line" />
              </button>
            </div>
            <div v-if="!selectedWidget.options?.length" class="options-empty">暂无选项</div>
          </div>
        </div>

        <!-- ======== dict-select 字典选择特有属性 ======== -->
        <div v-if="selectedWidget.type === 'dict-select'" class="prop-section">
          <div class="section-title">字典配置</div>
          <div class="prop-item">
            <label class="prop-label">字典类型 (dictType)</label>
            <el-input
              v-model="selectedWidget.dictType"
              placeholder="如 sys_yes_no"
              size="small"
            />
          </div>
          <div class="prop-desc-tip">
            <i class="i-ri-information-line mr-1" />
            字典类型对应系统字典配置表中的 dict_type 字段
          </div>
        </div>

        <!-- ======== image-upload 特有属性 ======== -->
        <div v-if="selectedWidget.type === 'image-upload'" class="prop-section">
          <div class="section-title">上传配置</div>
          <div class="prop-item">
            <label class="prop-label">最多上传数量</label>
            <el-input-number
              v-model="selectedWidget.limit"
              :min="1"
              :max="10"
              size="small"
              style="width: 100%"
            />
          </div>
        </div>

        <!-- ======== tree-select 特有属性 ======== -->
        <div v-if="selectedWidget.type === 'tree-select'" class="prop-section">
          <div class="section-title">树节点配置</div>
          <div class="prop-item">
            <div class="prop-desc-tip">
              <i class="i-ri-information-line mr-1" />
              实际使用时请通过 API 动态加载选项数据，此处为静态预览配置
            </div>
          </div>
          <div class="options-list">
            <div
              v-for="(opt, idx) in selectedWidget.options"
              :key="idx"
              class="option-item"
            >
              <el-input v-model="opt.label" placeholder="节点名" size="small" class="flex-1" />
              <el-input v-model="opt.value" placeholder="值" size="small" class="flex-1" />
              <button class="opt-del-btn" @click="removeOption(idx)">
                <i class="i-ri-delete-bin-line" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import type { FormWidget, WidgetType } from './types'
  import { widgetGroups } from './widget-config'

  const props = defineProps<{
    selectedWidget: FormWidget | null
  }>()

  const emit = defineEmits<{
    (e: 'deselect'): void
  }>()

  /** 是否有占位文字属性 */
  const hasPlaceholder = computed(() => {
    const noPlaceholder: WidgetType[] = ['switch', 'grid', 'divider', 'radio', 'checkbox', 'image-upload', 'rich-text']
    return !noPlaceholder.includes(props.selectedWidget?.type as WidgetType)
  })

  /** 是否有可清除属性（radio/checkbox/switch/upload/rich-text 无清除） */
  const hasClearable = computed(() => {
    const noClearable: WidgetType[] = ['switch', 'grid', 'divider', 'radio', 'checkbox', 'image-upload', 'rich-text', 'number']
    return !noClearable.includes(props.selectedWidget?.type as WidgetType)
  })

  /** 根据类型获取控件图标 */
  const getWidgetIcon = (type: WidgetType) => {
    for (const group of widgetGroups) {
      const found = group.widgets.find((w) => w.type === type)
      if (found) return found.icon
    }
    return 'i-ri-checkbox-blank-line'
  }

  /** 根据类型获取控件名称 */
  const getWidgetName = (type: WidgetType) => {
    for (const group of widgetGroups) {
      const found = group.widgets.find((w) => w.type === type)
      if (found) return found.name
    }
    return type
  }

  /** 新增选项（select 用） */
  const addOption = () => {
    if (!props.selectedWidget) return
    if (!props.selectedWidget.options) props.selectedWidget.options = []
    props.selectedWidget.options.push({ label: '新选项', value: `opt_${Date.now()}` })
  }

  /** 删除选项 */
  const removeOption = (idx: number) => {
    if (!props.selectedWidget?.options) return
    props.selectedWidget.options.splice(idx, 1)
  }
</script>

<style scoped lang="scss">
  .property-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: var(--art-bg-color, #fff);
    border-left: 1px solid var(--art-border-color, #e5e7eb);
    overflow: hidden;
  }

  /* 未选中提示 */
  .property-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .property-empty-content {
    text-align: center;
  }

  .empty-icon {
    font-size: 36px;
    color: #d1d5db;
    margin-bottom: 14px;
    display: block;
  }

  .empty-title {
    font-size: 14px;
    font-weight: 600;
    color: #9ca3af;
    margin-bottom: 6px;
  }

  .empty-desc {
    font-size: 12px;
    color: #c0c4cc;
    line-height: 1.7;
  }

  /* 头部 */
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-bottom: 1px solid var(--art-border-color, #e5e7eb);
    flex-shrink: 0;
    background: var(--art-bg-color, #fff);
  }

  .widget-type-tag {
    display: flex;
    align-items: center;
    font-size: 12px;
    font-weight: 600;
    color: var(--theme-color);
    background: color-mix(in srgb, var(--theme-color) 10%, transparent);
    padding: 4px 10px;
    border-radius: 20px;
    border: 1px solid color-mix(in srgb, var(--theme-color) 20%, transparent);
  }

  .close-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 4px;
    border: none;
    background: transparent;
    color: var(--art-text-gray-400);
    cursor: pointer;
    font-size: 14px;
    transition: all 0.15s;

    &:hover {
      background: #f3f4f6;
      color: #374151;
    }
  }

  /* 属性主体 */
  .property-body {
    flex: 1;
    overflow-y: auto;
    padding: 12px 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #e5e7eb;
      border-radius: 4px;
    }
  }

  /* 属性分区 */
  .prop-section {
    padding: 0 14px 12px;
    border-bottom: 1px solid #f3f4f6;
    margin-bottom: 4px;
  }

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 11px;
    font-weight: 700;
    color: var(--art-text-gray-400);
    text-transform: uppercase;
    letter-spacing: 0.8px;
    margin-bottom: 10px;
    padding-top: 10px;
  }

  .section-btn {
    display: flex;
    align-items: center;
    font-size: 11px;
    font-weight: 500;
    color: var(--theme-color);
    background: color-mix(in srgb, var(--theme-color) 8%, transparent);
    border: none;
    border-radius: 4px;
    padding: 2px 7px;
    cursor: pointer;
    transition: all 0.15s;

    &:hover {
      background: color-mix(in srgb, var(--theme-color) 15%, transparent);
    }
  }

  /* 单个属性项 */
  .prop-item {
    margin-bottom: 10px;
  }

  .prop-item-inline {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .prop-label {
    display: block;
    font-size: 12px;
    font-weight: 500;
    color: var(--art-text-gray-600);
    margin-bottom: 5px;
    line-height: 1;

    .prop-item-inline & {
      margin-bottom: 0;
    }
  }

  .required-star {
    color: #ef4444;
    margin-left: 2px;
  }

  .prop-desc-tip {
    font-size: 11px;
    color: var(--art-text-gray-400);
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
    padding: 6px 8px;
    line-height: 1.6;
    display: flex;
    align-items: flex-start;
    gap: 2px;
  }

  /* 选项列表 */
  .options-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .option-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .opt-del-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border-radius: 4px;
    border: none;
    background: transparent;
    color: #9ca3af;
    cursor: pointer;
    flex-shrink: 0;
    font-size: 13px;
    transition: all 0.15s;

    &:hover {
      background: #fee2e2;
      color: #ef4444;
    }
  }

  .options-empty {
    font-size: 12px;
    color: #d1d5db;
    text-align: center;
    padding: 12px 0;
  }

  /* 暗黑模式 */
  [data-theme='dark'] {
    .property-panel {
      background: var(--art-bg-color);
      border-left-color: var(--art-border-color);
    }

    .panel-header {
      background: var(--art-bg-color);
      border-bottom-color: var(--art-border-color);
    }

    .close-btn:hover {
      background: var(--art-gray-800);
      color: var(--art-text-gray-200);
    }

    .prop-section {
      border-bottom-color: var(--art-border-color);
    }

    .prop-desc-tip {
      background: var(--art-main-bg-color);
      border-color: var(--art-border-color);
    }

    .opt-del-btn:hover {
      background: rgba(239, 68, 68, 0.15);
    }
  }
</style>
