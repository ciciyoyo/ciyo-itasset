import { AppRouteRecordRaw } from '@/utils/router'

/**
 * 静态路由配置 所有人都有的路由
 *
 * 属性说明：
 * ignoreAuth: true 表示不需要登录即可访问
 * isHideTab: true 表示不在标签页中显示
 *
 * 注意事项：
 * 1、path、name 不要和动态路由冲突，否则会导致路由冲突无法访问
 * 2、静态路由不管是否登录都可以访问
 */
export const staticRoutes: AppRouteRecordRaw[] = [
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@views/auth/login/index.vue'),
    meta: { title: 'menu.login.title', isHideTab: true }
  },
  {
    path: '/403',
    name: 'Exception403',
    component: () => import('@views/exception/403/index.vue'),
    meta: { title: '403', isHideTab: true, ignoreAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'Exception404',
    component: () => import('@views/exception/404/index.vue'),
    meta: { title: '404', isHideTab: true, ignoreAuth: true }
  },
  {
    path: '/500',
    name: 'Exception500',
    component: () => import('@views/exception/500/index.vue'),
    meta: { title: '500', isHideTab: true, ignoreAuth: true }
  },
  {
    path: '/test',
    name: 'TestPage',
    component: () => import('@views/test/index.vue'),
    meta: {
      title: '测试',
      ignoreAuth: true,
      isHideTab: true
    }
  },
  {
    path: '/a/:number',
    name: 'DeviceMobileDetail',
    component: () => import('@views/itam/device/mobile-detail.vue'),
    meta: {
      title: '设备详情',
      ignoreAuth: true,
      isHideTab: true
    }
  },
  {
    path: '/',
    component: () => import('@views/index/index.vue'),
    redirect: '/home',
    name: 'UserCenterStatic',
    meta: { title: 'menu.menuName.userCenter' },
    children: [
      {
        path: '/home',
        name: 'HomePage',
        component: () => import('@views/home/index.vue'),
        meta: {
          title: '首页',
          fixedTab: true,
          icon: 'ri:home-3-line'
        }
      },
      {
        path: '/user-center',
        name: 'UserCenterPage',
        component: () => import('@views/system/user/profile/index.vue'),
        meta: {
          title: 'menu.menuName.userCenter',
          isHide: true,
          isHideTab: false
        }
      },
      {
        path: '/itam/requests',
        name: 'AssetRequests',
        component: () => import('@views/itam/requests/index.vue'),
        meta: {
          title: '资产申请管理',
          icon: 'ri:list-check'
        }
      }
    ]
  }
]
