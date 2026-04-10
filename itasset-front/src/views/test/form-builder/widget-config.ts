/**
 * 表单生成器 - 控件默认配置（图标全部使用项目白名单 ri: 系列）
 */
import type { WidgetGroup } from './types'

export const widgetGroups: WidgetGroup[] = [
  {
    groupName: '基础控件',
    icon: 'i-ri-pencil-line',
    widgets: [
      {
        type: 'input',
        name: '单行文本',
        icon: 'i-ri-edit-2-line',
        defaultWidget: () => ({
          type: 'input',
          label: '单行文本',
          prop: 'field',
          placeholder: '请输入',
          required: false,
          span: 12,
          clearable: true
        })
      },
      {
        type: 'textarea',
        name: '多行文本',
        icon: 'i-ri-file-text-line',
        defaultWidget: () => ({
          type: 'textarea',
          label: '多行文本',
          prop: 'field',
          placeholder: '请输入内容',
          required: false,
          span: 24,
          clearable: false
        })
      },
      {
        type: 'number',
        name: '数字输入',
        icon: 'i-ri-code-line',
        defaultWidget: () => ({
          type: 'number',
          label: '数字',
          prop: 'field',
          placeholder: '请输入数字',
          required: false,
          span: 12,
          min: 0,
          precision: 0
        })
      },
      {
        type: 'password',
        name: '密码框',
        icon: 'i-ri-lock-line',
        defaultWidget: () => ({
          type: 'password',
          label: '密码',
          prop: 'password',
          placeholder: '请输入密码',
          required: false,
          span: 12,
          clearable: true
        })
      }
    ]
  },
  {
    groupName: '选择控件',
    icon: 'i-ri-list-check',
    widgets: [
      {
        type: 'select',
        name: '下拉选择',
        icon: 'i-ri-arrow-drop-down-line',
        defaultWidget: () => ({
          type: 'select',
          label: '下拉选择',
          prop: 'field',
          placeholder: '请选择',
          required: false,
          span: 12,
          clearable: true,
          options: [
            { label: '选项一', value: '1' },
            { label: '选项二', value: '2' },
            { label: '选项三', value: '3' }
          ]
        })
      },
      {
        type: 'radio',
        name: '单选组',
        icon: 'i-ri-radio-button-line',
        defaultWidget: () => ({
          type: 'radio',
          label: '单选',
          prop: 'field',
          required: false,
          span: 12,
          options: [
            { label: '选项一', value: '0' },
            { label: '选项二', value: '1' }
          ]
        })
      },
      {
        type: 'checkbox',
        name: '多选组',
        icon: 'i-ri-checkbox-multiple-line',
        defaultWidget: () => ({
          type: 'checkbox',
          label: '多选',
          prop: 'field',
          required: false,
          span: 24,
          options: [
            { label: '选项一', value: '1' },
            { label: '选项二', value: '2' },
            { label: '选项三', value: '3' }
          ]
        })
      },
      {
        type: 'tree-select',
        name: '树形选择',
        icon: 'i-ri-organization-chart',
        defaultWidget: () => ({
          type: 'tree-select',
          label: '树形选择',
          prop: 'field',
          placeholder: '请选择',
          required: false,
          span: 12,
          clearable: true,
          options: [
            {
              label: '节点一',
              value: '1',
              children: [
                { label: '子节点1-1', value: '1-1' },
                { label: '子节点1-2', value: '1-2' }
              ]
            },
            { label: '节点二', value: '2' }
          ]
        })
      },
      {
        type: 'dict-select',
        name: '字典选择',
        icon: 'i-ri-book-open-line',
        defaultWidget: () => ({
          type: 'dict-select',
          label: '字典选择',
          prop: 'field',
          placeholder: '请选择',
          required: false,
          span: 12,
          clearable: true,
          dictType: 'sys_yes_no'
        })
      },
      {
        type: 'switch',
        name: '开关',
        icon: 'i-ri-toggle-line',
        defaultWidget: () => ({
          type: 'switch',
          label: '开关',
          prop: 'enabled',
          required: false,
          span: 12
        })
      }
    ]
  },
  {
    groupName: '日期时间',
    icon: 'i-ri-calendar-line',
    widgets: [
      {
        type: 'date',
        name: '日期选择',
        icon: 'i-ri-calendar-event-line',
        defaultWidget: () => ({
          type: 'date',
          label: '日期',
          prop: 'date',
          placeholder: '请选择日期',
          required: false,
          span: 12,
          clearable: true,
          valueFormat: 'YYYY-MM-DD'
        })
      },
      {
        type: 'date-range',
        name: '日期范围',
        icon: 'i-ri-calendar-check-line',
        defaultWidget: () => ({
          type: 'date-range',
          label: '日期范围',
          prop: 'dateRange',
          placeholder: '请选择日期范围',
          required: false,
          span: 24,
          clearable: true,
          valueFormat: 'YYYY-MM-DD'
        })
      },
      {
        type: 'datetime',
        name: '日期时间',
        icon: 'i-ri-time-line',
        defaultWidget: () => ({
          type: 'datetime',
          label: '日期时间',
          prop: 'datetime',
          placeholder: '请选择日期时间',
          required: false,
          span: 12,
          clearable: true,
          valueFormat: 'YYYY-MM-DD HH:mm:ss'
        })
      }
    ]
  },
  {
    groupName: '媒体 & 富文本',
    icon: 'i-ri-image-line',
    widgets: [
      {
        type: 'image-upload',
        name: '图片上传',
        icon: 'i-ri-image-add-line',
        defaultWidget: () => ({
          type: 'image-upload',
          label: '图片上传',
          prop: 'imageUrl',
          required: false,
          span: 24,
          limit: 1
        })
      },
      {
        type: 'rich-text',
        name: '富文本',
        icon: 'i-ri-file-text-line',
        defaultWidget: () => ({
          type: 'rich-text',
          label: '富文本内容',
          prop: 'content',
          required: false,
          span: 24
        })
      }
    ]
  },
  {
    groupName: '布局控件',
    icon: 'i-ri-layout-grid-line',
    widgets: [
      {
        type: 'divider',
        name: '分组标题',
        icon: 'i-ri-subtract-line',
        defaultWidget: () => ({
          type: 'divider',
          label: '',
          prop: '',
          title: '分组标题',
          required: false,
          span: 24
        })
      },
      {
        type: 'grid',
        name: '两列布局',
        icon: 'i-ri-layout-masonry-line',
        defaultWidget: () => ({
          type: 'grid',
          label: '',
          prop: '',
          required: false,
          span: 24,
          children: []
        })
      }
    ]
  }
]
