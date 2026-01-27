import { defineConfig, presetAttributify, presetIcons } from 'unocss'
import presetWind4 from '@unocss/preset-wind4'
import transformerDirectives from '@unocss/transformer-directives'
import { allIcons } from './src/utils/ui/icons'

export default defineConfig({
  safelist: allIcons.map((i) => `i-${i.replace(':', '-')}`),
  transformers: [transformerDirectives()],
  presets: [
    presetAttributify(),
    presetWind4(),
    presetIcons({
      scale: 1,
      warn: true
    })
  ],
  theme: {
    colors: {
      // Box Color (Light: white / Dark: black)
      box: 'var(--default-box-color)',

      // System Theme Color
      theme: 'var(--theme-color)',

      // Hover Color
      'hover-color': 'var(--art-hover-color)',

      // Active Color
      'active-color': 'var(--art-active-color)',

      // Element Plus Active Color
      'el-active-color': 'var(--art-el-active-color)',

      // ElementPlus Theme Colors
      primary: 'var(--art-primary)',
      secondary: 'var(--art-secondary)',
      error: 'var(--art-error)',
      info: 'var(--art-info)',
      success: 'var(--art-success)',
      warning: 'var(--art-warning)',
      danger: 'var(--art-danger)',

      // Gray Scale Colors (Auto-adapts to dark mode)
      'g-100': 'var(--art-gray-100)',
      'g-200': 'var(--art-gray-200)',
      'g-300': 'var(--art-gray-300)',
      'g-400': 'var(--art-gray-400)',
      'g-500': 'var(--art-gray-500)',
      'g-600': 'var(--art-gray-600)',
      'g-700': 'var(--art-gray-700)',
      'g-800': 'var(--art-gray-800)',
      'g-900': 'var(--art-gray-900)'
    },
    borderRadius: {
      'custom-xs': 'calc(var(--custom-radius) / 2)',
      'custom-sm': 'calc(var(--custom-radius) / 2 + 2px)'
    }
  },
  shortcuts: {
    // Flexbox Layout Utilities
    'flex-c': 'flex items-center',
    'flex-b': 'flex justify-between',
    'flex-cc': 'flex items-center justify-center',
    'flex-cb': 'flex items-center justify-between',

    // Transition Utilities
    'tad-200': 'transition-all duration-200',
    'tad-300': 'transition-all duration-300',

    // Border Utilities
    'border-full-d': 'border border-[var(--default-border)]',
    'border-b-d': 'border-b border-[var(--default-border)]',
    'border-t-d': 'border-t border-[var(--default-border)]',
    'border-l-d': 'border-l border-[var(--default-border)]',
    'border-r-d': 'border-r border-[var(--default-border)]',

    // Cursor Utilities
    'c-p': 'cursor-pointer',

    // Art Card Header Utilities
    'art-card-header': 'flex justify-between pr-6 pb-1',
    'art-card-title-h4': 'text-lg font-medium text-g-900',
    'art-card-title-p': 'mt-1 text-sm text-g-600',
    'art-card-title-span': 'ml-2 font-medium'
  },
  rules: [],
  extractors: [
    {
      name: 'art-icon-extractor',
      extract({ code }) {
        // 匹配各种复杂提取场景：
        // 1. 常规属性: icon="ri:user-line"
        // 2. 对象属性: icon: 'ri:user-line'
        // 3. 数组元素: ['ri:user-line']
        // 4. 动态绑定: :icon="'ri:user-line'"
        // 5. 模板字符串: `ri:user-line`
        const matches = Array.from(code.matchAll(/[`'"]([a-z0-9-]+:[a-z0-9-]+)[`'"]/g))
        return matches.map((m) => `i-${m[1].replace(':', '-')}`)
      }
    }
  ]
})
