/**
 * 离线图标加载器
 *
 * 用于在内网环境下支持 Iconify 图标的离线加载。
 * 通过预加载图标集数据，避免运行时从 CDN 获取图标。
 *
 * 使用方式：
 * 1. 安装所需图标集：pnpm add -D @iconify-json/[icon-set-name]
 * 2. 在此文件中导入并注册图标集
 * 3. 在组件中使用：<ArtSvgIcon icon="ri:home-line" />
 *
 * @module utils/ui/iconify-loader
 * @author Art Design Pro Team
 */

import {addCollection} from '@iconify/vue/offline'

// 使用 @iconify/vue/offline 模块，完全禁用 API 请求
// 只有通过 addCollection 注册的图标才会生效，不会发起任何网络请求
// 导入离线图标数据
// Remix Icon - 系统主要图标库
import riIcons from '@iconify-json/ri/icons.json'
// Iconamoon - 用于箭头等图标
import iconamoonIcons from '@iconify-json/iconamoon/icons.json'

// 注册离线图标集
addCollection(riIcons)
addCollection(iconamoonIcons)
