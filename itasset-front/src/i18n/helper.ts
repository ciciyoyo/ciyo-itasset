import {set} from 'lodash-es'

export type LocaleType = 'zh_CN' | 'en' | 'ru' | 'ja' | 'ko'

export const loadLocalePool: LocaleType[] = []

export function genMessage(langs: Record<string, Record<string, any>>, prefix = 'lang') {
    const obj: any = {}

    Object.keys(langs).forEach((key) => {
        const langFileModule = langs[key].default
        let fileName = key.replace(`./${prefix}/`, '').replace(/^\.\//, '')
        const lastIndex = fileName.lastIndexOf('.')
        fileName = fileName.substring(0, lastIndex)
        const keyList = fileName.split('/')
        const moduleName = keyList.shift()
        const objKey = keyList.join('.')

        if (moduleName) {
            // layout 文件特殊处理,直接展开到根对象,不嵌套
            if (moduleName === 'layout' && !objKey) {
                Object.assign(obj, langFileModule || {})
            } else if (objKey) {
                set(obj, moduleName, obj[moduleName] || {})
                set(obj[moduleName], objKey, langFileModule)
            } else {
                set(obj, moduleName, langFileModule || {})
            }
        }
    })
    return obj
}
