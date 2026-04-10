/**
 * 表单生成器 - 类型定义
 */

/** 控件类型枚举 */
export type WidgetType =
  | 'input'
  | 'textarea'
  | 'number'
  | 'password'
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'tree-select'
  | 'dict-select'
  | 'date'
  | 'date-range'
  | 'datetime'
  | 'switch'
  | 'image-upload'
  | 'rich-text'
  | 'grid'
  | 'divider'

/** 下拉选项 */
export interface SelectOption {
  label: string
  value: string | number
}

/** 表单控件实例 */
export interface FormWidget {
  /** 唯一 ID（nanoid 生成） */
  id: string
  /** 控件类型 */
  type: WidgetType
  /** 标签名 */
  label: string
  /** 字段名（prop） */
  prop: string
  /** 占位文字 */
  placeholder?: string
  /** 是否必填 */
  required: boolean
  /** 占用栅格宽度 */
  span: 12 | 24
  /** 是否禁用 */
  disabled?: boolean
  /** 是否只读 */
  readonly?: boolean
  /** 是否可清除 */
  clearable?: boolean
  /** 下拉/树选 选项（select、tree-select 使用） */
  options?: SelectOption[]
  /** 最小值（number 使用） */
  min?: number
  /** 最大值（number 使用） */
  max?: number
  /** 精度（number 使用） */
  precision?: number
  /** 日期格式（date 使用） */
  dateFormat?: string
  /** 日期输出格式（date 使用） */
  valueFormat?: string
  /** 图片上传数量限制（image-upload 使用） */
  limit?: number
  /** 字典类型（dict-select 使用） */
  dictType?: string
  /** 分割线标题（divider 使用） */
  title?: string
  /** grid 内嵌的子控件列表 */
  children?: FormWidget[]
}

/** 整体 Schema */
export interface FormSchema {
  /** 标签宽度 */
  labelWidth: string
  /** 栅格间距 */
  gutter: number
  /** 控件列表 */
  widgets: FormWidget[]
}

/** 左侧面板控件组定义 */
export interface WidgetGroup {
  /** 分组名 */
  groupName: string
  /** 分组图标 */
  icon: string
  /** 控件列表 */
  widgets: WidgetConfig[]
}

/** 控件面板中的控件描述 */
export interface WidgetConfig {
  /** 控件类型 */
  type: WidgetType
  /** 显示名称 */
  name: string
  /** 图标 (lucide / iconify) */
  icon: string
  /** 生成默认 FormWidget 的工厂函数 */
  defaultWidget: () => Omit<FormWidget, 'id'>
}
