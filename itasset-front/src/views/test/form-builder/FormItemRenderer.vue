<template>
  <!-- 渲染表单控件，preview=true 完整可交互，preview=false 设计态（文本框 readonly，选择类可展开） -->
  <el-form-item
    v-if="widget.type !== 'divider' && widget.type !== 'grid'"
    :label="widget.label"
    :prop="widget.prop"
    :required="widget.required"
  >

    <!-- ==================== 文本类（设计态 readonly） ==================== -->

    <!-- 单行文本 -->
    <el-input
      v-if="widget.type === 'input'"
      v-model="modelData[widget.prop]"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :readonly="!preview || widget.readonly"
      :clearable="preview && widget.clearable"
      style="width: 100%"
    />

    <!-- 多行文本 -->
    <el-input
      v-else-if="widget.type === 'textarea'"
      v-model="modelData[widget.prop]"
      type="textarea"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :readonly="!preview || widget.readonly"
      :rows="3"
      style="width: 100%"
    />

    <!-- 密码框 -->
    <el-input
      v-else-if="widget.type === 'password'"
      v-model="modelData[widget.prop]"
      type="password"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :readonly="!preview || widget.readonly"
      :clearable="preview && widget.clearable"
      show-password
      style="width: 100%"
    />

    <!-- ==================== 数字（无 readonly，设计态 disabled） ==================== -->

    <el-input-number
      v-else-if="widget.type === 'number'"
      v-model="modelData[widget.prop]"
      :disabled="widget.disabled || !preview"
      :min="widget.min"
      :max="widget.max"
      :precision="widget.precision"
      style="width: 100%"
    />

    <!-- ==================== 选择类（设计态可展开查看选项） ==================== -->

    <!-- 下拉选择 -->
    <el-select
      v-else-if="widget.type === 'select'"
      v-model="modelData[widget.prop]"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :clearable="widget.clearable"
      filterable
      style="width: 100%"
    >
      <el-option
        v-for="opt in widget.options"
        :key="opt.value"
        :label="opt.label"
        :value="opt.value"
      />
    </el-select>

    <!-- 单选组 -->
    <el-radio-group
      v-else-if="widget.type === 'radio'"
      v-model="modelData[widget.prop]"
      :disabled="widget.disabled"
    >
      <el-radio
        v-for="opt in widget.options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </el-radio>
    </el-radio-group>

    <!-- 多选组 -->
    <el-checkbox-group
      v-else-if="widget.type === 'checkbox'"
      v-model="modelData[widget.prop]"
      :disabled="widget.disabled"
    >
      <el-checkbox
        v-for="opt in widget.options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </el-checkbox>
    </el-checkbox-group>

    <!-- 树形选择 -->
    <el-tree-select
      v-else-if="widget.type === 'tree-select'"
      v-model="modelData[widget.prop]"
      :data="treeData(widget)"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :clearable="widget.clearable"
      check-strictly
      :render-after-expand="false"
      node-key="value"
      :props="{ label: 'label', children: 'children' }"
      style="width: 100%"
    />

    <!-- 字典选择（使用项目业务组件 DictSelect） -->
    <DictSelect
      v-else-if="widget.type === 'dict-select'"
      v-model="modelData[widget.prop]"
      :dict-type="widget.dictType || 'sys_yes_no'"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      style="width: 100%"
    />

    <!-- ==================== 日期时间类（设计态可展开） ==================== -->

    <!-- 日期选择 -->
    <el-date-picker
      v-else-if="widget.type === 'date'"
      v-model="modelData[widget.prop]"
      type="date"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :clearable="widget.clearable"
      :value-format="widget.valueFormat || 'YYYY-MM-DD'"
      style="width: 100%"
    />

    <!-- 日期时间 -->
    <el-date-picker
      v-else-if="widget.type === 'datetime'"
      v-model="modelData[widget.prop]"
      type="datetime"
      :placeholder="widget.placeholder"
      :disabled="widget.disabled"
      :clearable="widget.clearable"
      :value-format="widget.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
      style="width: 100%"
    />

    <!-- 日期范围 -->
    <el-date-picker
      v-else-if="widget.type === 'date-range'"
      v-model="modelData[widget.prop]"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      :disabled="widget.disabled"
      :clearable="widget.clearable"
      :value-format="widget.valueFormat || 'YYYY-MM-DD'"
      style="width: 100%"
    />

    <!-- ==================== 开关 ==================== -->
    <el-switch
      v-else-if="widget.type === 'switch'"
      v-model="modelData[widget.prop]"
      :disabled="widget.disabled"
    />

    <!-- ==================== 媒体 & 富文本（设计态 pointer-events:none 遮罩，避免误操作） ==================== -->

    <!-- 图片上传（使用项目业务组件 ImageUpload） -->
    <div
      v-else-if="widget.type === 'image-upload'"
      :class="{ 'design-mask': !preview }"
      style="width: 100%"
    >
      <ImageUpload v-model:value="modelData[widget.prop]" :limit="widget.limit || 1" />
    </div>

    <!-- 富文本（Tinymce，设计态用占位块代替避免初始化多个编辑器） -->
    <template v-else-if="widget.type === 'rich-text'">
      <div v-if="!preview" class="rich-text-placeholder">
        <i class="i-ri-file-text-line" />
        <span>富文本编辑器（预览时可用）</span>
      </div>
      <div v-else :class="{ 'design-mask': !preview }" style="width: 100%">
        <Tinymce v-model:value="modelData[widget.prop]" :height="200" />
      </div>
    </template>

  </el-form-item>
</template>

<script setup lang="ts">
  import { reactive } from 'vue'
  import type { FormWidget } from './types'
  import DictSelect from '@/components/business/dict-select/index.vue'
  import ImageUpload from '@/components/business/image-upload/index.vue'
  import Tinymce from '@/components/business/tinymce/index.vue'

  const props = defineProps<{
    /** 要渲染的控件配置 */
    widget: FormWidget
    /** true = 预览模式（完整可交互），false = 设计模式 */
    preview: boolean
    /** 外部传入的表单数据对象（预览模式使用） */
    modelValue?: Record<string, any>
  }>()

  /**
   * 数据绑定对象：
   * - 预览模式：使用父组件传入的 modelValue
   * - 设计模式：使用内部独立 dummy 对象
   */
  const modelData = props.modelValue ?? reactive<Record<string, any>>({})

  /** 将 options 适配为树形选择格式 */
  const treeData = (widget: FormWidget) => {
    return (widget.options ?? []) as any[]
  }
</script>

<style scoped>
  /** 媒体/上传类控件在设计模式下的遮罩，防止误操作但保持外观正常 */
  .design-mask {
    pointer-events: none;
    user-select: none;
  }

  /** 富文本在设计模式下的占位块 */
  .rich-text-placeholder {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    height: 80px;
    border: 1px dashed #d1d5db;
    border-radius: 6px;
    justify-content: center;
    font-size: 13px;
    color: #9ca3af;
    background: #f9fafb;
    cursor: default;
  }
</style>
