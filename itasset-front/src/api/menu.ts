import request from '@/utils/http'
import {AppRouteRecord} from '@/types'
import {formatMenuTitle} from '@/utils'

/**
 * 后端返回的菜单元数据接口
 */
interface ApiMenuMeta {
    title: string
    icon: string
    isKeepAlive: boolean
    isAffix: boolean
    isHide: boolean
    isLink: string | null
    isIframe: boolean
    location: string | null
}

/**
 * 后端返回的菜单数据接口
 */
interface ApiMenuRecord {
    name: string
    path: string
    component?: string
    meta: ApiMenuMeta
    children?: ApiMenuRecord[]
}

/**
 * 转换后端菜单数据为前端路由数据
 */
function transformApiMenuToRoute(apiMenu: ApiMenuRecord): AppRouteRecord {
    const route: AppRouteRecord = {
        name: apiMenu.name,
        path: apiMenu.path,
        meta: {
            title: apiMenu.meta.title,
            icon: apiMenu.meta.icon,
            keepAlive: apiMenu.meta.isKeepAlive,
            fixedTab: apiMenu.meta.isAffix,
            isHide: apiMenu.meta.isHide,
            link: apiMenu.meta.isLink || undefined,
            isIframe: apiMenu.meta.isIframe
        }
    }

    // 如果有 component，则添加
    if (apiMenu.component) {
        route.component = apiMenu.component
    }

    // 递归处理子菜单
    if (apiMenu.children && apiMenu.children.length > 0) {
        route.children = apiMenu.children.map(transformApiMenuToRoute)
    }

    return route
}

// 获取菜单列表
export async function fetchGetMenuList() {
    const response = await request.get<ApiMenuRecord[]>({
        url: '/getRouters'
    })
    // 转换数据结构
    if (response && Array.isArray(response)) {
        return response.map(transformApiMenuToRoute)
    }
    return []
}
