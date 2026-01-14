export const baseUrl = import.meta.env.VITE_API_URL

export const tokenKey = 'X-Admin-Token'

export const userInfoKey = 'userInfo'

export const userBasicInfoKey = 'userBasicInfo'

export function getToken() {
    // 2025-07-12 16:30:15 兼容uniapp中通过链接传参 在ifremame中获取token写入总是各种问题
    return localStorage.getItem(tokenKey) || (new URLSearchParams(window.location.search).get('tempToken') as string) || ''
}

export function getTokenUrl(url: string) {
    return baseUrl + url + '?Authorization=Bearer ' + getToken()
}

/**
 * 拼接字符串 如果字符串1已/结尾 字符串2开头有/ 则去掉字符串2的/
 */
export function joinUrl(url1: string, url2: string) {
    if (url1.endsWith('/') && url2.startsWith('/')) {
        return url1 + url2.substring(1)
    }
    if (!url1.endsWith('/') && !url2.startsWith('/')) {
        return url1 + '/' + url2
    }

    return url1 + url2
}

/**
 * 获取接口基础地址
 */
export function getApiBaseUrl() {
    // 如果 baseurl 是 http 开头
    if (baseUrl.startsWith('http')) {
        return baseUrl
    }
    return window.location.origin + baseUrl
}

/**
 * 获取静态资源路径 如果有cdn 可以这里改造
 */
export const getStaticBaseUrl = () => {
    return import.meta.env.VITE_CDN_PUBLIC_PATH || getBaseUrlPath()
}

export const basePathUrl = import.meta.env?.VITE_BASE_URL

/**
 * 获取基础路径 如果包含了二级路径，需要在路由中配置 这里再获取
 */
export const getBaseUrlPath = () => {
    let url = window.location.protocol + '//' + window.location.host
    let baseUrl = url + basePathUrl
    // 最后一个是/ 移除掉
    if (baseUrl.endsWith('/')) {
        return baseUrl.substring(0, baseUrl.length - 1)
    }
    return baseUrl
}

// 添加基础url
export function addBaseUrl(url: string) {
    return joinUrl(basePathUrl, url)
}
