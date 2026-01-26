/**
 * 快速入口配置
 * 包含：应用列表、快速链接等配置
 */
import type { FastEnterConfig } from '@/types/config'

const fastEnterConfig: FastEnterConfig = {
  // 显示条件（屏幕宽度）
  minWidth: 1200,
  // 应用列表
  applications: [
    {
      name: '工作台',
      description: '',
      icon: 'ri:pie-chart-line',
      iconColor: '#377dff',
      enabled: true,
      order: 1,
      routeName: 'Console'
    }
  ],
  // 快速链接
  quickLinks: [
    {
      name: '个人中心',
      enabled: true,
      order: 5,
      routeName: 'UserCenterPage'
    }
  ]
}

export default Object.freeze(fastEnterConfig)
